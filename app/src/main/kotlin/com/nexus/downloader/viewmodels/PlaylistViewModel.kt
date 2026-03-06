package com.nexus.downloader.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.downloader.domain.model.PlaylistItem
import com.nexus.downloader.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _currentPlaylistUrl = MutableStateFlow("")

    val playlistItems: StateFlow<List<PlaylistItem>> = _currentPlaylistUrl
        .flatMapLatest { url ->
            if (url.isBlank()) flowOf(emptyList())
            else playlistRepository.getPlaylistItems(url)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun loadPlaylist(url: String) {
        _currentPlaylistUrl.value = url
    }
}
