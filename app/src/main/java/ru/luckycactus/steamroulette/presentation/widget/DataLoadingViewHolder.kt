package ru.luckycactus.steamroulette.presentation.widget

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.empty_layout.view.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.utils.visibility

class DataLoadingViewHolder(
    private val emptyLayout: ViewGroup,
    private val progress: View,
    private val content: View,
    buttonAction: () -> Unit
) {

    private var state = State.CONTENT
    private val res = emptyLayout.resources

    init {
        emptyLayout.btnRetry.setOnClickListener {
            buttonAction()
        }
    }

    fun showLoading() {
        progress.visibility(true)
        content.visibility(false)
        emptyLayout.visibility(false)
        state = State.LOADING
    }

    fun showEmpty(title: String? = null, msg: String? = null) {
        showPlaceholder(
            title ?: res.getString(R.string.empty_title),
            msg ?: res.getString(R.string.empty_desription),
            State.EMPTY
        )
    }

    fun showEmptyWithButton(
        title: String? = null,
        msg: String? = null,
        buttonText: String? = null
    ) {
        setupButton(buttonText)
        showEmpty(title, msg)
    }

    fun showError(title: String? = null, msg: String? = null) {
        showPlaceholder(
            title ?: res.getString(R.string.error_title),
            msg ?: res.getString(R.string.error_description),
            State.ERROR
        )
    }

    fun showErrorWithButton(
        title: String? = null,
        msg: String? = null,
        buttonText: String? = null
    ) {
        setupButton(buttonText)
        showError(title, msg)
    }

    fun showContent() {
        emptyLayout.visibility(false)
        progress.visibility(false)
        content.visibility(true)
        state = State.CONTENT
    }

    fun hide() {
        emptyLayout.visibility(false)
        progress.visibility(false)
        content.visibility(false)
        state = State.HIDDEN
    }

    fun isShowingEmpty() = state == State.EMPTY

    fun isShowingError() = state == State.ERROR

    private fun setupButton(buttonText: String?) {
        emptyLayout.btnRetry.text = buttonText ?: res.getString(R.string.retry)
    }

    private fun showPlaceholder(title: String, msg: String, state: State) {
        content.visibility(false)
        progress.visibility(false)

        emptyLayout.tvEmptyTitle.text = title
        emptyLayout.tvEmptyDescription.text = msg
        emptyLayout.visibility(true)
        this.state = state
    }

    private enum class State {
        ERROR,
        EMPTY,
        LOADING,
        CONTENT,
        HIDDEN
    }
}