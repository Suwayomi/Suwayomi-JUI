package ca.gosyer.jui.data.library

import ca.gosyer.jui.data.ApolloAppClient
import ca.gosyer.jui.data.graphql.SetMangaInLibraryMutation
import ca.gosyer.jui.domain.library.service.LibraryRepository
import ca.gosyer.jui.domain.server.Http
import com.apollographql.apollo.ApolloClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepositoryImpl(
    private val apolloAppClient: ApolloAppClient,
    private val http: Http,
    private val serverUrl: Url,
) : LibraryRepository {
    val apolloClient: ApolloClient
        get() = apolloAppClient.value

    fun setMangaInLibrary(
        mangaId: Long,
        inLibrary: Boolean,
    ): Flow<Unit> =
        apolloClient.mutation(
            SetMangaInLibraryMutation(mangaId.toInt(), inLibrary),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.updateManga!!.clientMutationId
            }

    override fun addMangaToLibrary(mangaId: Long): Flow<Unit> = setMangaInLibrary(mangaId, true)

    override fun removeMangaFromLibrary(mangaId: Long): Flow<Unit> = setMangaInLibrary(mangaId, false)
}
