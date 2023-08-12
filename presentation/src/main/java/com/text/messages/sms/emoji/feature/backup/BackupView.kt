package com.text.messages.sms.emoji.feature.backup

import com.text.messages.sms.emoji.common.base.MessagesViewContract
import com.text.messages.sms.emoji.model.BackupFile
import io.reactivex.Observable

interface BackupView : MessagesViewContract<BackupState> {

    fun activityVisible(): Observable<*>
    fun restoreClicks(): Observable<*>
    fun restoreFileSelected(): Observable<BackupFile>
    fun restoreConfirmed(): Observable<*>
    fun stopRestoreClicks(): Observable<*>
    fun stopRestoreConfirmed(): Observable<*>
    fun fabClicks(): Observable<*>

    fun requestStoragePermission()
    fun selectFile()
    fun confirmRestore()
    fun stopRestore()

}