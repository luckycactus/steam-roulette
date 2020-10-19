package ru.luckycactus.steamroulette.presentation.features.games

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.view.MenuItem.SHOW_AS_ACTION_NEVER
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_library.*
import kotlinx.android.synthetic.main.fragment_library_filter.filterSheet
import kotlinx.android.synthetic.main.fullscreen_progress.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.ui.GridSpaceDecoration
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.ui.widget.DataLoadingViewHolder
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.*

@AndroidEntryPoint
class LibraryFragment : BaseFragment(), MessageDialogFragment.Callbacks {

    override val layoutResId = R.layout.fragment_library

    private lateinit var adapter: LibraryAdapter
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var spaceDecoration: GridSpaceDecoration

    private var actionMode: ActionMode? = null
    private lateinit var selectionTracker: SelectionTracker<Long>
    private var inSelectionMode = false

    private lateinit var clearHiddenMenuItem: MenuItem
    private lateinit var searchMenuItem: MenuItem
    private lateinit var changeScaleMenuItem: MenuItem

    private lateinit var filtersBehavior: BottomSheetBehavior<*>
    private lateinit var filtersFragment: LibraryFilterFragment

    private lateinit var dataLoadingViewHolder: DataLoadingViewHolder

    private lateinit var placeholderTransition: Transition

    private val viewModel: LibraryViewModel by viewModels()

    override val logScreenName: String?
        get() = if (viewModel.onlyHidden) "Hidden Games" else "Library"

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var insetsApplied = false
        val fabInitialMargin = fab.marginBottom
        rvGames.doOnApplyWindowInsets { it, insets, padding ->
            it.updatePadding(
                bottom = padding.bottom + insets.systemWindowInsetBottom + fabInitialMargin + fab.height
            )
            fab.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                updateMargins(bottom = fabInitialMargin + insets.tappableElementInsets.bottom)
            }
            insetsApplied = true
            insets
        }
        filterSheet.doOnApplyWindowInsets { view, insets, initialPadding ->
            filterSheet.doOnLayout {
                filtersBehavior.peekHeight =
                    insets.systemWindowInsetBottom + resources.getDimensionPixelSize(R.dimen.filter_sheet_header_height)
            }
            view.updatePadding(bottom = insets.systemWindowInsetBottom + initialPadding.bottom)
            insets
        }

        fab.hide()
        fab.doOnNextLayout {
            //if insets were applied already but fab isn't laid out yet, then we must set extra padding here
            if (insetsApplied)
                rvGames.updatePadding(bottom = rvGames.paddingBottom + fab.height)
        }

        toolbar.run {
            addSystemTopPadding()
            setNavigationOnClickListener(::onNavigationIconClick)
            setTitle(R.string.my_steam_library)
            inflateMenu(R.menu.menu_library)
            setOnMenuItemClickListener(::onMenuItemClick)
            clearHiddenMenuItem = menu.findItem(R.id.action_clear_all_hidden)
            changeScaleMenuItem = menu.findItem(R.id.action_change_scale)
            searchMenuItem = menu.findItem(R.id.action_search).apply {
                setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                        // searchMenuItem.isActionViewExpanded changes after callback, so we postpone method call
                        viewModel.onSearchStateChanged(true)
                        post { updateOnBackPressedCallbackEnabled() }
                        return true
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                        viewModel.onSearchStateChanged(false)
                        post { updateOnBackPressedCallbackEnabled() }
                        return true
                    }
                })
            }
            (searchMenuItem.actionView as SearchView).run {
                findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                    .filters += AlphaNumSpaceInputFilter()

                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        clearFocus()
                        return onQueryTextChange(query)
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.onSearchQueryChanged(newText)
                        return true
                    }
                })
                queryHint = getString(R.string.library_search_hint)
            }
        }

        fab.run {
            setImageResource(R.drawable.ic_filter_list_24dp)
            backgroundTintList =
                ColorStateList.valueOf(MaterialColors.getColor(fab, R.attr.colorPrimary))
            setOnClickListener {
                filtersFragment.open()
            }
        }

        adapter = LibraryAdapter(false, ::onGameClick)
        rvGames.adapter = adapter
        layoutManager = GridLayoutManager(context, 1)
        rvGames.layoutManager = layoutManager
        spaceDecoration = GridSpaceDecoration(
            1,
            resources.getDimensionPixelSize(R.dimen.spacing_games_library)
        )
        rvGames.addItemDecoration(spaceDecoration)
        rvGames.setHasFixedSize(true)


        val itemKeyProvider = LibraryItemKeyProvider(rvGames)
        selectionTracker = SelectionTracker.Builder(
            "games",
            rvGames,
            itemKeyProvider,
            LibraryDetailsLookup(rvGames),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

        selectionTracker.onRestoreInstanceState(savedInstanceState)
        selectionTracker.addObserver(selectionObserver)
        adapter.tracker = selectionTracker
        updateSelectionMode()

        dataLoadingViewHolder = DataLoadingViewHolder(
            emptyLayout,
            progress,
            rvGames,
            { /*nothing*/ }
        )

        filtersFragment =
            childFragmentManager.findFragmentById(R.id.filterSheet) as LibraryFilterFragment
        filtersBehavior = from(filterSheet)
        filtersBehavior.addBottomSheetCallback(bottomSheetCallback)
        filterSheet.doOnLayout {
            val slideOffset = when (filtersBehavior.state) {
                STATE_EXPANDED -> 1f
                STATE_COLLAPSED -> 0f
                else -> -1f
            }
            updateFiltersScrim(slideOffset)
        }

        filterSheet.setOnClickListener { /* nothing */ }

        scrimView.setOnClickListener {
            filtersFragment.close()
        }

        lifecycleScope.launch {
            viewModel.games.collectLatest {
                adapter.submitData(it)
            }
        }

        placeholderTransition = Fade().apply {
            addTarget(R.id.rvGames)
            addTarget(R.id.progress)
            addTarget(R.id.placeholder)
        }
        val emptyPlaceholder = ContentState.Placeholder(
            getString(R.string.error_filtered_games_not_found),
            ContentState.TitleType.None,
            ContentState.ButtonType.None
        )
        var prevItemCount = -1
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest {
                if (prevItemCount != adapter.itemCount) {
                    TransitionManager.beginDelayedTransition(
                        root_fragment_library,
                        placeholderTransition
                    )
                }
                if (prevItemCount != -1
                    && adapter.itemCount == 0
                    && (it.refresh != LoadState.Loading || prevItemCount == 0)
                ) {
                    dataLoadingViewHolder.showPlaceholder(emptyPlaceholder)
                } else {
                    dataLoadingViewHolder.showContent()
                }

                prevItemCount = adapter.itemCount
            }
        }

        observeEvent(viewModel.filterChangedEvent) {
            actionMode?.finish()
        }

        observe(viewModel.hasAnyFilters, ::updateFiltersUi)

        observe(viewModel.hasSelectedHiddenGames) {
            actionMode?.menu?.run {
                findItem(R.id.action_hide).isVisible = !it
                findItem(R.id.action_unhide).isVisible = it
            }
        }

        observe(viewModel.clearAllHiddenInFront) {
            clearHiddenMenuItem.setShowAsAction(
                if (it)
                    SHOW_AS_ACTION_ALWAYS
                else
                    SHOW_AS_ACTION_NEVER
            )
        }

        observe(viewModel.clearAllHiddenVisibility) {
            clearHiddenMenuItem.isVisible = it
        }

        observe(viewModel.menuItemsVisibility) {
            toolbar.menu.forEach { menuItem ->
                if (menuItem != clearHiddenMenuItem)
                    menuItem.isVisible = it
            }
        }

        if (viewModel.onlyHidden) {
            observe(viewModel.filteredGamesCount) {
                toolbar.title = "${getString(R.string.hidden_games)} ($it)"
            }
        }

        observe(viewModel.spanCount) {
            spaceDecoration.spanCount = it
            layoutManager.spanCount = it
            rvGames.invalidateItemDecorations()
        }
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            updateOnBackPressedCallbackEnabled()
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            updateFiltersScrim(slideOffset)
        }
    }

    private val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionChanged() {
            updateSelectionMode()
        }

        override fun onItemStateChanged(key: Long, selected: Boolean) {
            viewModel.onGameSelectionChanged(key, selected)
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        private var windowCallbackWrapper: WindowCallbackWrapper? = null

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onDestroyActionMode(mode: ActionMode) {
            //after config change and start of new actionmode old actionmodeCallback can get invoked
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
                selectionTracker.clearSelection()
            windowCallbackWrapper?.wrapped?.let {
                activity?.window?.callback = it
                windowCallbackWrapper = null
            }
            actionMode = null
            inSelectionMode = false
            updateOnBackPressedCallbackEnabled()
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_library_action_mode, menu)

            inSelectionMode = true
            // ActionMode handles back press itself.
            // We intercept back press manually when action mode is opened
            windowCallbackWrapper =
                object : WindowCallbackWrapper(requireActivity().window.callback) {
                    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                            requireActivity().onBackPressed()
                            return true
                        }
                        return super.dispatchKeyEvent(event);
                    }
                }
            requireActivity().window.callback = windowCallbackWrapper

            updateOnBackPressedCallbackEnabled()

            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_unhide -> {
                    hideSelectedGames(false)
                    true
                }
                R.id.action_hide -> {
                    hideSelectedGames(true)
                    true
                }
                else -> false
            }
        }
    }

    private fun hideSelectedGames(hide: Boolean) {
        viewModel.hide(selectionTracker.selection.map { it.toInt() }, hide)
        selectionTracker.clearSelection()
    }

    override fun onDestroy() {
        super.onDestroy()
        actionMode?.finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        selectionTracker.onSaveInstanceState(outState)
    }

    override fun onMessageDialogResult(
        dialog: MessageDialogFragment,
        result: MessageDialogFragment.Result
    ) {
        when (dialog.tag) {
            CONFIRM_CLEAR_DIALOG_TAG ->
                if (result == MessageDialogFragment.Result.Positive)
                    viewModel.clearAllHidden()
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        updateOnBackPressedCallbackEnabled()
    }

    private fun updateFiltersUi(hasAnyFilters: Boolean) {
        fab.show(!hasAnyFilters && !viewModel.onlyHidden)

        if (viewModel.onlyHidden) {
            filtersBehavior.isHideable = true
            filtersBehavior.state = STATE_HIDDEN
        } else {
            filtersBehavior.skipCollapsed = !hasAnyFilters
            filtersBehavior.isHideable = !hasAnyFilters
            if (!hasAnyFilters && filtersBehavior.state == STATE_COLLAPSED)
                filtersBehavior.state = STATE_HIDDEN
        }
    }

    private fun updateOnBackPressedCallbackEnabled() {
        onBackPressedCallback.isEnabled = when {
            isHidden -> false
            inSelectionMode -> true
            searchMenuItem.isActionViewExpanded -> true
            else -> when (filtersBehavior.state) {
                STATE_DRAGGING, STATE_EXPANDED, STATE_HALF_EXPANDED -> true
                else -> false
            }
        }
    }

    private fun onBackPressed() {
        if (::filtersBehavior.isInitialized && filtersBehavior.state == STATE_EXPANDED) {
            filtersFragment.close()
            return
        }

        actionMode?.finish()?.also {
            return
        }

        if (searchMenuItem.isActionViewExpanded) {
            searchMenuItem.collapseActionView()
            return
        }

        onBackPressedCallback.isEnabled = false
        requireActivity().onBackPressed()
    }

    private fun updateFiltersScrim(slideOffset: Float) {
        scrimView.alpha = slideOffset.coerceAtLeast(0f)
        scrimView.visibility(slideOffset > 0f)
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all_hidden -> {
                showClearAllConfirmation()
                true
            }
            R.id.action_change_scale -> {
                viewModel.onChangeScaleClick()
                true
            }
            else -> false
        }
    }

    private fun showClearAllConfirmation() {
        MessageDialogFragment.create(
            requireContext(),
            messageResId = R.string.dialog_message_reset_hidden_games,
            negativeResId = R.string.cancel
        ).show(childFragmentManager, CONFIRM_CLEAR_DIALOG_TAG)
    }

    private fun onGameClick(game: LibraryGame, sharedViews: List<View>, imageIsReady: Boolean) {
        reenterTransition = createExitTransition()
        exitTransition = createExitTransition().apply {
            doOnEnd {
                exitTransition = null
            }
        }
        (activity as MainActivity).onGameClick(game.header, sharedViews, imageIsReady)
    }

    private fun onNavigationIconClick(view: View) {
        if (inSelectionMode)
            selectionTracker.clearSelection()
        else
            requireActivity().onBackPressed()
    }

    private fun updateSelectionMode() {
        val selectionSize = selectionTracker.selection.size()
        val enable = selectionSize > 0

        if (inSelectionMode != enable) {
            toolbar?.updateLayoutParams<AppBarLayout.LayoutParams> {
                scrollFlags = if (enable) {
                    val translation = appBarLayout.y.toInt()
                    if (translation < 0) {
                        rvGames.scrollBy(0, -translation)
                    }
                    SCROLL_FLAG_ENTER_ALWAYS
                } else {
                    SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS
                }
            }

            if (enable)
                actionMode = toolbar.startActionMode(actionModeCallback)
            else
                actionMode?.finish()
        }

        actionMode?.title = selectionSize.toString()
    }

    private fun createExitTransition() = transitionSet {
        fade {}
        addListener((activity as MainActivity).touchSwitchTransitionListener)
    }

    private class LibraryDetailsLookup(
        private val recyclerView: RecyclerView
    ) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            return if (view != null)
                (recyclerView.getChildViewHolder(view) as LibraryAdapter.GameViewHolder)
                    .getItemDetails()
            else null
        }
    }

    private class LibraryItemKeyProvider(
        recyclerView: RecyclerView
    ) : ItemKeyProvider<Long>(SCOPE_MAPPED) {
        private val adapter: LibraryAdapter = (recyclerView.adapter as? LibraryAdapter)
            ?: throw IllegalStateException("RecyclerView must have LibraryAdapter set")
        private val lm = (recyclerView.layoutManager as? GridLayoutManager)
            ?: throw IllegalStateException("RecyclerView must have GridLayoutManager set")

        override fun getKey(position: Int): Long? = adapter.getSelectionKeyForPosition(position)

        override fun getPosition(key: Long): Int {
            val last = lm.findLastVisibleItemPosition()
            if (last < 0)
                return RecyclerView.NO_POSITION
            for (i in lm.findFirstVisibleItemPosition()..last) {
                if (key == adapter.getSelectionKeyForPosition(i))
                    return i
            }
            return RecyclerView.NO_POSITION
        }
    }

    companion object {
        private const val CONFIRM_CLEAR_DIALOG_TAG = "CONFIRM_CLEAR_DIALOG"
        fun newInstance(onlyHidden: Boolean = false) = LibraryFragment().apply {
            arguments = bundleOf(LibraryViewModel.ARG_ONLY_HIDDEN to onlyHidden)
        }
    }
}