package com.text.messages.sms.emoji.repository

import com.text.messages.sms.emoji.model.Attachment
import com.text.messages.sms.emoji.model.Message
import com.text.messages.sms.emoji.model.MmsPart
import io.realm.RealmResults
import java.io.File

interface MessageRepository {

    fun getMessages(threadId: Long, query: String = ""): RealmResults<Message>

    fun getMessage(id: Long): Message?

    fun getMessageForPart(id: Long): Message?

    fun getLastIncomingMessage(threadId: Long): RealmResults<Message>

    fun getUnreadCount(): Long

    fun getPart(id: Long): MmsPart?

    fun getPartsForConversation(threadId: Long): RealmResults<MmsPart>

    fun savePart(id: Long): File?

    /**
     * Retrieves the list of messages which should be shown in the notification
     * for a given conversation
     */
    fun getUnreadUnseenMessages(threadId: Long): RealmResults<Message>

    /**
     * Retrieves the list of messages which should be shown in the quickreply popup
     * for a given conversation
     */
    fun getUnreadMessages(threadId: Long): RealmResults<Message>

    fun markAllSeen()

    fun markSeen(threadId: Long)

    fun updateIsEmoji(id: Long, emojiOnly: Boolean)

    fun markRead(vararg threadIds: Long)

    fun markUnread(vararg threadIds: Long)

    fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>,
        delay: Int = 0
    )

    /**
     * Attempts to send the SMS message. This can be called if the message has already been persisted
     */
    fun sendSms(message: Message)

    fun resendMms(message: Message)

    /**
     * Attempts to cancel sending the message with the given id
     */
    fun cancelDelayedSms(id: Long)

    fun insertSentSms(subId: Int, threadId: Long, address: String, body: String, date: Long): Message

    fun insertReceivedSms(subId: Int, address: String, body: String, sentTime: Long): Message

    /**
     * Marks the message as sending, in case we need to retry sending it
     */
    fun markSending(id: Long)

    fun markSent(id: Long)

    fun markFailed(id: Long, resultCode: Int)

    fun markDelivered(id: Long)

    fun markDeliveryFailed(id: Long, resultCode: Int)

    fun deleteMessages(vararg messageIds: Long)

    /**
     * Returns the number of messages older than [maxAgeDays] per conversation
     */
    fun getOldMessageCounts(maxAgeDays: Int): Map<Long, Int>

    /**
     * Deletes all messages older than [maxAgeDays]
     */
    fun deleteOldMessages(maxAgeDays: Int)

}
