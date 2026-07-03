package com.example.core.common

import kotlinx.coroutines.flow.*

/**
 * A highly reusable and reactive Repository pattern utility that reads database state first,
 * triggers background network refresh, handles serialization and errors, and returns a cohesive of Flow<Resource<ResultType>>.
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow<Resource<ResultType>> {
    emit(Resource.Loading)
    
    // First, emit what's in cache
    val cachedData = query().firstOrNull()
    if (cachedData != null) {
        emit(Resource.Success(cachedData))
    }

    if (cachedData == null || shouldFetch(cachedData)) {
        try {
            // Fetch online content
            val netResult = fetch()
            // Clear or update local cache
            saveFetchResult(netResult)
            
            // Re-emit latest local state
            query().collect { latestLocal ->
                emit(Resource.Success(latestLocal))
            }
        } catch (throwable: Throwable) {
            // Emitting old cache alongside the loading/network error details
            if (cachedData != null) {
                emit(Resource.Success(cachedData))
            }
            emit(Resource.Error(throwable, "Background sync failed. Showing offline database."))
        }
    } else {
        // Just keep collecting current local stream
        query().collect { latestLocal ->
            emit(Resource.Success(latestLocal))
        }
    }
}
