package ru.luckycactus.steamroulette.presentation.ui.widget

import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import kotlinx.android.synthetic.main.empty_layout.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.utils.extensions.visibility

class DataLoadingViewHolder(
    private val placeholderLayout: ViewGroup,
    private val progress: View,
    private val content: View,
    buttonAction: () -> Unit
) {

    private var state = State.CONTENT
    private val res = placeholderLayout.resources

    init {
        placeholderLayout.btnRetry.setOnClickListener {
            buttonAction()
        }
    }

    fun showContentState(contentState: ContentState) {
        when (contentState) {
            ContentState.Loading -> showLoading()
            ContentState.Success -> showContent()
            is ContentState.Placeholder -> showPlaceholder(contentState)
        }
    }

    fun showLoading() {
        content.visibility = INVISIBLE
        placeholderLayout.visibility(false)
        progress.visibility(true)
        state = State.LOADING
    }


    fun showContent() {
        placeholderLayout.visibility(false)
        progress.visibility(false)
        content.visibility = VISIBLE
        state = State.CONTENT
    }


    fun showPlaceholder(placeholder: ContentState.Placeholder) {
        with(placeholderLayout.tvEmptyTitle) {
            text =
                when (placeholder.titleType) {
                    ContentState.TitleType.DefaultError -> res.getString(R.string.placeholder_error_title)
                    ContentState.TitleType.DefaultEmpty -> res.getString(R.string.placeholder_empty_title)
                    is ContentState.TitleType.Custom -> placeholder.titleType.text
                    ContentState.TitleType.None -> null
                }
            visibility(placeholder.titleType != ContentState.TitleType.None)
        }

        with(placeholderLayout.btnRetry) {
            text = when (placeholder.buttonType) {
                ContentState.ButtonType.Default -> res.getString(R.string.retry)
                ContentState.ButtonType.None -> null
                is ContentState.ButtonType.Custom -> placeholder.buttonType.text
            }
            visibility(placeholder.buttonType != ContentState.ButtonType.None)
        }

        placeholderLayout.tvEmptyDescription.text = placeholder.message
        content.visibility = INVISIBLE
        progress.visibility(false)
        placeholderLayout.visibility(true)
        this.state = State.PLACEHOLDER
    }

    fun hide() {
        placeholderLayout.visibility(false)
        progress.visibility(false)
        content.visibility = INVISIBLE
        state = State.HIDDEN
    }

    enum class State {
        PLACEHOLDER,
        LOADING,
        CONTENT,
        HIDDEN
    }
}