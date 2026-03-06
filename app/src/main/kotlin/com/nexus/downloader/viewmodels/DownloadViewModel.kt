package com.nexus.downloader.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.downloader.domain.model.Download
import com.nexus.downloader.domain.usecase.GetAllDownloadsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val getAllDownloadsUseCase: GetAllDownloadsUseCase
) : ViewModel() {

    val downloads: StateFlow<List<Download>> = getAllDownloadsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
