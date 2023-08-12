package com.text.messages.sms.emoji.interactor

import com.text.messages.sms.emoji.repository.BackupRepository
import io.reactivex.Flowable
import javax.inject.Inject

class PerformBackup @Inject constructor(
    private val backupRepo: BackupRepository
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> {
        return Flowable.just(params)
                .doOnNext { backupRepo.performBackup() }
    }

}