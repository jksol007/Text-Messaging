package com.chating.messages.feature.aftercall

import com.text.messages.sms.emoji.model.Recipient

data class AfterCallState(
    val hasError: Boolean = false,
    val phoneNo: String? = "",
    val recipient: Recipient? = null
)