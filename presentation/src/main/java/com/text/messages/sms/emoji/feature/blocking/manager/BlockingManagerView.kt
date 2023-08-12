package com.text.messages.sms.emoji.feature.blocking.manager

import com.text.messages.sms.emoji.common.base.MessagesViewContract
import io.reactivex.Observable
import io.reactivex.Single

interface BlockingManagerView : MessagesViewContract<BlockingManagerState> {

    fun activityResumed(): Observable<*>
    fun qksmsClicked(): Observable<*>
    fun callBlockerClicked(): Observable<*>
    fun callControlClicked(): Observable<*>
    fun siaClicked(): Observable<*>

    fun showCopyDialog(manager: String): Single<Boolean>

}
