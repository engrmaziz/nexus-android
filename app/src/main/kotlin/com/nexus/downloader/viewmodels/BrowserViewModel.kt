package com.nexus.downloader.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.downloader.domain.model.Bookmark
import com.nexus.downloader.domain.model.BrowserHistory
import com.nexus.downloader.domain.repository.BrowserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val browserRepository: BrowserRepository
) : ViewModel() {

    val history: StateFlow<List<BrowserHistory>> = browserRepository.getBrowserHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val bookmarks: StateFlow<List<Bookmark>> = browserRepository.getBookmarks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addToHistory(url: String, title: String) {
        viewModelScope.launch {
            browserRepository.addToHistory(BrowserHistory(url = url, title = title))
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            browserRepository.clearHistory()
        }
    }

    fun addBookmark(url: String, title: String) {
        viewModelScope.launch {
            browserRepository.addBookmark(Bookmark(url = url, title = title))
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            browserRepository.deleteBookmark(id)
        }
    }
}
