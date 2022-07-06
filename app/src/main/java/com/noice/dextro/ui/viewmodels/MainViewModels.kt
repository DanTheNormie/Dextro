package com.noice.dextro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.noice.dextro.data.firebase.FirestorePagingSource

class MainViewModels: ViewModel() {

    val flow = Pager(PagingConfig(20)){
        FirestorePagingSource(FirebaseFirestore.getInstance())
    }.flow.cachedIn(viewModelScope)
}