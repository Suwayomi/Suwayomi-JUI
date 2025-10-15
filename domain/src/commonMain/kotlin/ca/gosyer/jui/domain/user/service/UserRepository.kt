package ca.gosyer.jui.domain.user.service

import ca.gosyer.jui.domain.user.model.LoginData
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun loginUI(username: String, password: String): Flow<LoginData>

    fun refreshUI(refreshToken: String): Flow<String>

    fun loginSimple(username: String, password: String): Flow<String>
}
