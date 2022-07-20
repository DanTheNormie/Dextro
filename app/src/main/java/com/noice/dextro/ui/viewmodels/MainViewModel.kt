package com.noice.dextro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.noice.dextro.data.firebase.FirestorePagingSource

class MainViewModel: ViewModel() {

    var flow = Pager(PagingConfig(pageSize = 20, enablePlaceholders = true)){
        FirestorePagingSource(FirebaseFirestore.getInstance())
    }.flow.cachedIn(viewModelScope)
}