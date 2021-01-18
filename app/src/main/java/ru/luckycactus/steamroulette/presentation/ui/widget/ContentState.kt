package ru.luckycactus.steamroulette.presentation.ui.widget

sealed class ContentState {
    object Loading : ContentState()

    object Success : ContentState()

    data class Placeholder constructor(
        val message: String,
        val titleType: TitleType,
        val buttonType: ButtonType
    ) : ContentState() {

        class Builder {
            var message: String? = null
            var titleType: TitleType = TitleType.None
            var buttonType: ButtonType = ButtonType.None

            fun build(): Placeholder {
                checkNotNull(message) { "You should set non null message before build()" }
                return Placeholder(message!!, titleType, buttonType)
            }
        }
    }

    sealed class TitleType {
        object DefaultError : TitleType()

        object DefaultEmpty : TitleType()

        object None : TitleType()

        data class Custom(
            val text: String
        ) : TitleType()
    }

    sealed class ButtonType {
        object Default : ButtonType()

        object None : ButtonType()

        data class Custom(
            val text: String
        ) : ButtonType()
    }

    companion object {
        fun errorPlaceholder(
            message: String,
            title: String? = null,
            buttonText: String? = null
        ) = Placeholder(
                message,
                title?.let { TitleType.Custom(it) } ?: TitleType.DefaultError,
                buttonText?.let { ButtonType.Custom(it) } ?: ButtonType.Default
            )

        fun emptyPlaceholder(
            message: String,
            title: String? = null,
            buttonText: String? = null
        ) = Placeholder(
                message,
                title?.let { TitleType.Custom(it) } ?: TitleType.DefaultEmpty,
                buttonText?.let { ButtonType.Custom(it) } ?: ButtonType.Default
            )
    }
}