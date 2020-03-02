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
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.features.system_reqs.model.SystemReqsUiModel

class SystemReqsViewModel @AssistedInject constructor(
    @Assisted gameHeader: GameHeader,
    private val getGameStoreInfo: GetGameStoreInfoUseCase,
    private val systemReqsUiModelMapper: SystemReqsUiModelMapper
) : ViewModel() {

    val systemReqs: LiveData<List<SystemReqsUiModel>>
        get() = _systemReqs

    private val _systemReqs = MutableLiveData<List<SystemReqsUiModel>>()

    init {
        viewModelScope.launch {
            val gameDetails = getGameStoreInfo(
                GetGameStoreInfoUseCase.Params(gameHeader.appId, false)
            )
            _systemReqs.value = systemReqsUiModelMapper.mapFrom(gameDetails)
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(gameHeader: GameHeader): SystemReqsViewModel
    }
}