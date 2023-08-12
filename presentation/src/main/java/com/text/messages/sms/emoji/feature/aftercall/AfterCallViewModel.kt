package com.chating.messages.feature.aftercall

import com.text.messages.sms.emoji.common.base.MessagesViewModel
import com.text.messages.sms.emoji.repository.ConversationRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import javax.inject.Inject
import javax.inject.Named

class AfterCallViewModel @Inject constructor(
    @Named("phoneNo") private val phoneNo: String?,
    private val conversationRepo: ConversationRepository
) : MessagesViewModel<AfterCallView, AfterCallState>(
    AfterCallState(
        phoneNo = phoneNo
    )
) {
    override fun bindView(view: AfterCallView) {
        super.bindView(view)

        if (phoneNo != null && phoneNo.trim().isNotEmpty()) {
            val conversation = conversationRepo.getOrCreateConversation(phoneNo)
            newState { copy(recipient = conversation?.recipients?.first()) }

            view.messageIntent.autoDispose(view.scope())
                .subscribe {
                    view.sendMessage(conversation)
                }
        }

        view.callIntent.autoDispose(view.scope())
            .subscribe {
                phoneNo?.let { it1 -> view.makePhoneCall(it1) }
            }

    }
}