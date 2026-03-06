package com.nexus.downloader.domain.usecase

import com.nexus.downloader.domain.model.AppSettings
import com.nexus.downloader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.getSettings()
}
