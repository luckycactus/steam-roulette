package ru.luckycactus.steamroulette.presentation.features.system_reqs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.games.GetGameStoreInfoUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements

class SystemReqsViewModel @AssistedInject constructor(
    @Assisted appId: Int,
    private val getGameStoreInfo: GetGameStoreInfoUseCase
) : ViewModel() {

    val game: LiveData<GameStoreInfo>
        get() = _game

    private val _game = MutableLiveData<GameStoreInfo>()

    init {
        viewModelScope.launch {
            _game.value = getGameStoreInfo(
                GetGameStoreInfoUseCase.Params(appId, false)
            )
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(appId: Int): SystemReqsViewModel
    }
}