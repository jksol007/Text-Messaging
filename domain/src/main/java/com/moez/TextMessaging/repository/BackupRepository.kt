package com.text.messages.sms.emoji.repository

import com.text.messages.sms.emoji.model.BackupFile
import io.reactivex.Observable

interface BackupRepository {

    sealed class Progress(val running: Boolean = false, val indeterminate: Boolean = true) {
        class Idle : Progress()
        class Parsing : Progress(true)
        class Running(val max: Int, val count: Int) : Progress(true, false)
        class Saving : Progress(true)
        class Syncing : Progress(true)
        class Finished : Progress(true, false)
    }

    fun performBackup()

    fun getBackupProgress(): Observable<Progress>

    /**
     * Returns a list of all local backups
     */
    fun getBackups(): Observable<List<BackupFile>>

    fun performRestore(filePath: String)

    fun stopRestore()

    fun getRestoreProgress(): Observable<Progress>

}