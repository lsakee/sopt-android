/*
 * Copyright 2023 SOPT - Shout Our Passion Together
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sopt.official.stamp.feature.ranking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sopt.official.domain.soptamp.model.RankFetchType
import org.sopt.official.domain.soptamp.repository.RankingRepository
import org.sopt.official.domain.soptamp.user.GetNicknameUseCase
import org.sopt.official.stamp.feature.ranking.model.RankingListUiModel
import org.sopt.official.stamp.feature.ranking.model.toUiModel
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val rankingRepository: RankingRepository,
    private val getNicknameUseCase: GetNicknameUseCase
) : ViewModel() {
    private val _state: MutableStateFlow<RankingState> = MutableStateFlow(RankingState.Loading)
    val state: StateFlow<RankingState> = _state.asStateFlow()
    var isRefreshing by mutableStateOf(false)
    val nickname = getNicknameUseCase()

    fun onRefresh(isCurrent: Boolean) {
        viewModelScope.launch {
            isRefreshing = true
            fetchRanking(isCurrent)
        }
    }

    fun fetchRanking(isCurrent: Boolean) = viewModelScope.launch {
        _state.value = RankingState.Loading
        rankingRepository.getRanking(
            if (isCurrent) RankFetchType.Term() else RankFetchType.All
        ).mapCatching { it.toUiModel() }
            .onSuccess { ranking ->
                if (isRefreshing) {
                    isRefreshing = false
                }
                onSuccessStateChange(ranking)
            }.onFailure {
                _state.value = RankingState.Failure
            }
    }

    private fun onSuccessStateChange(ranking: RankingListUiModel) {
        _state.value = RankingState.Success(ranking, getNicknameUseCase())
    }
}
