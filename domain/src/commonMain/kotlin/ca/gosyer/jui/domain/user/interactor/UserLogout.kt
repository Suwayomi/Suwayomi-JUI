package ca.gosyer.jui.domain.user.interactor

import ca.gosyer.jui.domain.user.service.UserPreferences
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject

@Inject
class UserLogout(
    private val userPreferences: UserPreferences,
) {
    suspend fun await(
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow()
        .catch {
            onError(it)
            log.warn(it) { "Failed to logout user" }
        }
        .singleOrNull()

    fun asFlow() = flow {
        userPreferences.uiRefreshToken().set("")
        userPreferences.uiAccessToken().set("")
        userPreferences.simpleSession().set("")
        emit(Unit)
    }

    companion object {
        private val log = logging()
    }
}
