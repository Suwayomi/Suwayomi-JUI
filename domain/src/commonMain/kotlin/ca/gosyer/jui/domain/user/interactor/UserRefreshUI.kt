package ca.gosyer.jui.domain.user.interactor

import ca.gosyer.jui.domain.user.service.UserPreferences
import ca.gosyer.jui.domain.user.service.UserRepository
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject

@Inject
class UserRefreshUI(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
) {
    suspend fun await(
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow()
        .catch {
            onError(it)
            log.warn(it) { "Failed to refresh user" }
        }
        .singleOrNull()

    fun asFlow() = userRepository.refreshUI(userPreferences.uiRefreshToken().get())
        .onEach {
            userPreferences.uiAccessToken().set(it)
        }

    companion object {
        private val log = logging()
    }
}
