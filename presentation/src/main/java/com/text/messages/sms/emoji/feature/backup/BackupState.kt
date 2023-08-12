package com.text.messages.sms.emoji.feature.backup

import com.text.messages.sms.emoji.model.BackupFile
import com.text.messages.sms.emoji.repository.BackupRepository

data class BackupState(
    val backupProgress: BackupRepository.Progress = BackupRepository.Progress.Idle(),
    val restoreProgress: BackupRepository.Progress = BackupRepository.Progress.Idle(),
    val lastBackup: String = "",
    val backups: List<BackupFile> = listOf(),
    val upgraded: Boolean = false
)