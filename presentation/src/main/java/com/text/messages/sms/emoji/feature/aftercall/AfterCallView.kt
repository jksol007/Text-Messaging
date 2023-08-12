package com.chating.messages.feature.aftercall

import com.text.messages.sms.emoji.common.base.MessagesView
import com.text.messages.sms.emoji.model.Conversation
import io.reactivex.Observable

interface AfterCallView : MessagesView<AfterCallState> {

    val callIntent: Observable<*>
    val messageIntent: Observable<*>

    fun makePhoneCall(phoneNo: String)
    fun sendMessage(conversation: Conversation?)
}