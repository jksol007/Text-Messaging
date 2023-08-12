package com.text.messages.sms.emoji.feature.conversationinfo

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.Lifecycle
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.common.Navigator
import com.text.messages.sms.emoji.common.PreferencesManager
import com.text.messages.sms.emoji.common.base.MessagesPresenter
import com.text.messages.sms.emoji.common.util.ClipboardUtils
import com.text.messages.sms.emoji.common.util.extensions.makeToast
import com.text.messages.sms.emoji.extensions.asObservable
import com.text.messages.sms.emoji.extensions.mapNotNull
import com.text.messages.sms.emoji.feature.conversationinfo.ConversationInfoItem.ConversationInfoMedia
import com.text.messages.sms.emoji.feature.conversationinfo.ConversationInfoItem.ConversationInfoRecipient
import com.text.messages.sms.emoji.interactor.DeleteConversations
import com.text.messages.sms.emoji.interactor.MarkArchived
import com.text.messages.sms.emoji.interactor.MarkUnarchived
import com.text.messages.sms.emoji.manager.PermissionManager
import com.text.messages.sms.emoji.model.Conversation
import com.text.messages.sms.emoji.repository.ConversationRepository
import com.text.messages.sms.emoji.repository.MessageRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.Realm
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ConversationInfoPresenter @Inject constructor(
    @Named("threadId") threadId: Long,
    messageRepo: MessageRepository,
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val deleteConversations: DeleteConversations,
    private val markArchived: MarkArchived,
    private val markUnarchived: MarkUnarchived,
    private val navigator: Navigator,
    private val permissionManager: PermissionManager
) : MessagesPresenter<ConversationInfoView, ConversationInfoState>(ConversationInfoState(threadId = threadId)) {

    private val conversation: Subject<Conversation> = BehaviorSubject.create()

    init {
        changeLanguage()
        disposables += conversationRepo.getConversationAsync(threadId)
            .asObservable()
            .filter { conversation -> conversation.isLoaded }
            .doOnNext { conversation ->
                if (!conversation.isValid) {
                    newState { copy(hasError = true) }
                }
            }
            .filter { conversation -> conversation.isValid }
            .filter { conversation -> conversation.id != 0L }
            .subscribe(conversation::onNext)

        disposables += markArchived
        disposables += markUnarchived
        disposables += deleteConversations

        disposables += Observables
            .combineLatest(
                conversation, messageRepo.getPartsForConversation(threadId).asObservable()
            ) { conversation, parts ->
                val data = mutableListOf<ConversationInfoItem>()

                // If some data was deleted, this isn't the place to handle it
                if (!conversation.isLoaded || !conversation.isValid || !parts.isLoaded || !parts.isValid) {
                    return@combineLatest
                }

                data += conversation.recipients.map(::ConversationInfoRecipient)
                data += ConversationInfoItem.ConversationInfoSettings(
                    name = conversation.name,
                    recipients = conversation.recipients,
                    archived = conversation.archived,
                    blocked = conversation.blocked
                )
                data += parts.map(::ConversationInfoMedia)

                newState { copy(data = data) }
            }
            .subscribe()
    }

    override fun bindIntents(view: ConversationInfoView) {
        super.bindIntents(view)

        // Add or display the contact
        view.recipientClicks()
            .mapNotNull(conversationRepo::getRecipient)
            .doOnNext { recipient ->
                recipient.contact?.lookupKey?.let(navigator::showContact)
                    ?: navigator.addContact(recipient.address)
            }
            .autoDispose(view.scope(Lifecycle.Event.ON_DESTROY)) // ... this should be the default
            .subscribe()

        // Copy phone number
        view.recipientLongClicks()
            .mapNotNull(conversationRepo::getRecipient)
            .map { recipient -> recipient.address }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(view.scope())
            .subscribe { address ->
                ClipboardUtils.copy(context, address)
                context.makeToast(R.string.info_copied_address)
            }

        // Show the theme settings for the conversation
        view.themeClicks()
            .autoDispose(view.scope())
            .subscribe(view::showThemePicker)

        // Show the conversation title dialog
        view.nameClicks()
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .map { conversation -> conversation.name }
            .autoDispose(view.scope())
            .subscribe(view::showNameDialog)

        // Set the conversation title
        view.nameChanges()
            .withLatestFrom(conversation) { name, conversation ->
                conversationRepo.setConversationName(conversation.id, name)
            }
            .autoDispose(view.scope())
            .subscribe()

        // Show the notifications settings for the conversation
        view.notificationClicks()
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .autoDispose(view.scope())
            .subscribe { conversation -> navigator.showNotificationSettings(conversation.id) }

        // Toggle the archived state of the conversation
        view.archiveClicks()
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .autoDispose(view.scope())
            .subscribe { conversation ->
                when (conversation.archived) {
                    true -> markUnarchived.execute(listOf(conversation.id))
                    false -> markArchived.execute(listOf(conversation.id))
                }
            }

        // Toggle the blocked state of the conversation
        view.blockClicks()
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .autoDispose(view.scope())
            .subscribe { conversation ->
                view.showBlockingDialog(listOf(conversation.id), !conversation.blocked)
                Log.i(
                    "TAG",
                    "bindIntents: conversation.id :- " + conversation.id + " ---------->  conversation.blocked :- " + conversation.blocked
                )
            }

        // Show the delete confirmation dialog
        view.deleteClicks()
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .autoDispose(view.scope())
            .subscribe { view.showDeleteDialog() }

        // Delete the conversation
        view.confirmDelete()
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .autoDispose(view.scope())
            .subscribe { conversation -> deleteConversations.execute(listOf(conversation.id)) }

        // Media
        view.mediaClicks()
            .autoDispose(view.scope())
            .subscribe(navigator::showMedia)

        view.menuReadyIntent
            .mapNotNull(conversationRepo::getRecipient)
            .doOnNext { recipient ->
                recipient.contact?.lookupKey?.let(navigator::showContact)
                    ?: navigator.addContact(recipient.address)
            }
            .autoDispose(view.scope(Lifecycle.Event.ON_DESTROY)) // ... this should be the default
            .subscribe()

        view.recipientClicks
            .mapNotNull(conversationRepo::getRecipient)
            .doOnNext { recipient ->
                recipient.contact?.lookupKey?.let(navigator::showContact)
                    ?: navigator.addContact(recipient.address)
            }
            .autoDispose(view.scope(Lifecycle.Event.ON_DESTROY)) // ... this should be the default
            .subscribe()
    }

    open fun changeLanguage() {
        try {
            var languageCode: String = PreferencesManager.getLanguage(context)
            val resources = context.resources
            val configuration = resources.configuration
            if (languageCode == null || languageCode.trim { it <= ' ' }.isEmpty()) {
                try {
                    languageCode = configuration.locale.language
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

            }
            Log.d("TAG", "updateLanguage:1 " + languageCode)
            if (languageCode != null && !languageCode.trim { it <= ' ' }.isEmpty()) {
                languageCode = languageCode.toLowerCase()
                val locale = Locale(languageCode)
                Locale.setDefault(locale)
                val config: Configuration = context.getResources().getConfiguration()
                config.setLocale(locale)
                context.getResources().updateConfiguration(
                    config,
                    Realm.getApplicationContext()!!.getResources().getDisplayMetrics()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}