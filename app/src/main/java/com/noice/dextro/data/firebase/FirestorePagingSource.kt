package com.noice.dextro.data.firebase

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.noice.dextro.data.model.User
import kotlinx.coroutines.tasks.await

class FirestorePagingSource(
    private val db:FirebaseFirestore
) : PagingSource<QuerySnapshot,User>(){
    override fun getRefreshKey(state: PagingState<QuerySnapshot, User>): QuerySnapshot? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, User> {
        return try {
            val currentPage = params.key ?: db.collection("users")
                .limit(10)
                .get()
                .await()

            val lastDocumentSnapshot = currentPage.documents[currentPage.size() -1]

            val nextPage = db.collection("users")
                .limit(10).
                startAfter(lastDocumentSnapshot)
                .get()
                .await()

            LoadResult.Page(
                data = currentPage.toObjects(User::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }


    }
}