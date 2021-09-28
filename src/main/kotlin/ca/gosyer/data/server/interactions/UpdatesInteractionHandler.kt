package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.MangaAndChapter
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.recentUpdatesQuery
import ca.gosyer.util.lang.withIOContext
import io.ktor.client.request.get
import javax.inject.Inject

class UpdatesInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun getRecentUpdates() = withIOContext {
        client.get<List<MangaAndChapter>>(
            serverUrl + recentUpdatesQuery()
        )
    }
}
