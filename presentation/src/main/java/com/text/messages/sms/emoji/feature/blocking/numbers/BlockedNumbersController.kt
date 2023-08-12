package com.text.messages.sms.emoji.feature.blocking.numbers

import android.content.Intent
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.ads.AdvertiseHandler
import com.text.messages.sms.emoji.common.base.MessagesController
import com.text.messages.sms.emoji.common.util.Colors
import com.text.messages.sms.emoji.common.util.extensions.setBackgroundTint
import com.text.messages.sms.emoji.common.util.extensions.setTint
import com.text.messages.sms.emoji.feature.blocking.BlockingDialog
import com.text.messages.sms.emoji.feature.contacts.ContactsActivity
import com.text.messages.sms.emoji.injection.appComponent
import com.text.messages.sms.emoji.model.BlockedNumber
import com.text.messages.sms.emoji.repository.ConversationRepository
import com.text.messages.sms.emoji.util.PhoneNumberUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.blocked_numbers_controller.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class BlockedNumbersController :
    MessagesController<BlockedNumbersView, BlockedNumbersState, BlockedNumbersPresenter>(),
    BlockedNumbersView {

    @Inject
    override lateinit var presenter: BlockedNumbersPresenter

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var phoneNumberUtils: PhoneNumberUtils

    private val adapter = BlockedNumbersAdapter()
    private val saveAddressSubject: Subject<String> = PublishSubject.create()

    @Inject
    lateinit var blockingDialog: BlockingDialog

    @Inject
    lateinit var conversationRepository: ConversationRepository

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.blocked_numbers_controller
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.blocked_numbers_title)
        showBackButton(true)
    }

    override fun onViewCreated() {
        super.onViewCreated()
        //add.setBackgroundTint(colors.theme().theme)
        add.setBackgroundTint(resources!!.getColor(R.color.tools_theme))
        add.setTint(colors.theme().textPrimary,false)
        adapter.emptyView = empty
        numbers.adapter = adapter
    }

    override fun render(state: BlockedNumbersState) {
        adapter.updateData(state.numbers)
    }

    override fun unblockAddress(): Observable<BlockedNumber> = adapter.unblockAddress
    override fun addAddress(): Observable<*> = add.clicks()
    override fun saveAddress(): Observable<String> = saveAddressSubject

    override fun showAddDialog() {
        val intent = Intent(activity, ContactsActivity::class.java)
        startActivityForResult(intent, 1)
    }

    override fun unblockNumber(blockedNumber: BlockedNumber) {
        var con: List<Long> = emptyList()
        val threadID = conversationRepository.getThreadId(blockedNumber.address)
        if (threadID != null) {
            con = listOf(threadID)
        }

        val pnNo = blockedNumber.address
        GlobalScope.launch {
            blockingDialog.blockContact(activity!!, listOf(pnNo), con, block = false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //AdvertiseHandler.getInstance().isNeedOpenAdRequest = false
        AdvertiseHandler.getInstance().disableAppOpenAds()
        if (data != null) {
            val serializable = data.getSerializableExtra(ContactsActivity.ChipsKey)
            if (serializable != null) {
                val hashMap: HashMap<String, String?> = serializable as HashMap<String, String?>
                val pnNo = hashMap.keys.first().toString()

                var con: List<Long> = emptyList()
                val threadID = conversationRepository.getThreadId(pnNo)
                if (threadID != null) {
                    con = listOf(threadID)
                }

                GlobalScope.launch {
                    blockingDialog.blockContact(activity!!, listOf(pnNo), con, block = true)
                }

            }
        }
    }

}
