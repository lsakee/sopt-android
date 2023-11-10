/*
 * MIT License
 * Copyright 2023 SOPT - Shout Our Passion Together
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.sopt.official.data.mypage.repository

import org.sopt.official.domain.mypage.repository.UserRepository
import org.sopt.official.data.mypage.local.SoptampDataStore
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val remote: org.sopt.official.data.mypage.source.UserDataSource,
    private val local: SoptampDataStore
) : UserRepository {
    override val nickname: String
        get() = local.nickname

    override suspend fun checkNickname(nickname: String) = runCatching { remote.checkNickname(nickname) }
    override suspend fun logout(): Result<Unit> = runCatching { local.clear() }

    override suspend fun getUserInfo() = runCatching {
        remote.getUserInfo().toDomain()
    }.onSuccess {
        local.nickname = it.nickname
        local.profileMessage = it.profileMessage
    }

    override suspend fun updateProfileMessage(profileMessage: String) = runCatching {
        remote.updateProfileMessage(profileMessage)
    }.onSuccess {
        local.profileMessage = profileMessage
    }

    override suspend fun updateNickname(nickname: String): Result<Unit> = runCatching {
        remote.updateNickname(nickname)
    }.onSuccess {
        local.nickname = nickname
    }

    override fun updateLocalUserInfo(profileMessage: String) {
        local.apply {
            this.profileMessage = profileMessage
        }
    }

    override fun fetchUserId() = local.userId
    override fun getIsOnboardingSeen() = local.isOnboardingSeen
    override fun updateOnboardingSeen(value: Boolean) {
        local.isOnboardingSeen = value
    }
}
