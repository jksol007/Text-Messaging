/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.text.messages.sms.emoji.feature.conversationinfo

import android.content.Context
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import com.bluelinelabs.conductor.RouterTransaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.common.Navigator
import com.text.messages.sms.emoji.common.MessagesChangeHandler
import com.text.messages.sms.emoji.common.base.MessagesController
import com.text.messages.sms.emoji.common.util.extensions.scrapViews
import com.text.messages.sms.emoji.common.widget.TextInputDialog
import com.text.messages.sms.emoji.feature.blocking.BlockingDialog
import com.text.messages.sms.emoji.feature.conversationinfo.injection.ConversationInfoModule
import com.text.messages.sms.emoji.feature.themepicker.ThemePickerController
import com.text.messages.sms.emoji.injection.appComponent
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.conversation_info_controller.*
import javax.inject.Inject

class ConversationInfoController(
    val threadId: Long = 0
) : MessagesController<ConversationInfoView, ConversationInfoState, ConversationInfoPresenter>(),
    ConversationInfoView {

    @Inject
    override lateinit var presenter: ConversationInfoPresenter

    @Inject
    lateinit var blockingDialog: BlockingDialog

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var adapter: ConversationInfoAdapter

    private val nameDialog: TextInputDialog by lazy {
        TextInputDialog(
            activity!!,
            activity!!.getString(R.string.info_name),
            nameChangeSubject::onNext
        )
    }

    private val nameChangeSubject: Subject<String> = PublishSubject.create()
    private val confirmDeleteSubject: Subject<Unit> = PublishSubject.create()

    init {
        appComponent
            .conversationInfoBuilder()
            .conversationInfoModule(ConversationInfoModule(this))
            .build()
            .inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.conversation_info_controller
    }

    override fun onViewCreated() {
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(adapter, activity!!))
        recyclerView.layoutManager = GridLayoutManager(activity, 3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int =
                    if (adapter.getItemViewType(position) == 2) 1 else 3
            }
        }

        themedActivity?.theme
            ?.autoDispose(scope())
            ?.subscribe { recyclerView.scrapViews() }

        if (adapter.data.isNotEmpty()) {
            val item: ConversationInfoItem = adapter.getItem(0)
            when (item) {
                is ConversationInfoItem.ConversationInfoRecipient -> {
                    val recipient = item.value
                    if (recipient.contact == null) {
                        setHasOptionsMenu(false)
                    } else {
                        setHasOptionsMenu(true)
                    }
                }else -> {}
            }
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.info_title)
        showBackButton(true)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        menuReadyIntent.onNext(0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                //recipientClicks()
                val item = adapter.getItem(0) as? ConversationInfoItem.ConversationInfoRecipient
                item?.value?.id?.run(recipientClicks::onNext)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun render(state: ConversationInfoState) {
        if (state.hasError) {
            activity?.finish()
            return
        }

        adapter.data = state.data

        if (adapter.data.isNotEmpty()) {
            val item: ConversationInfoItem = adapter.getItem(0)
            when (item) {
                is ConversationInfoItem.ConversationInfoRecipient -> {
                    val recipient = item.value
                    setHasOptionsMenu(recipient.contact == null)
                }else -> {}
            }
        }
    }

    override fun recipientClicks(): Observable<Long> = adapter.recipientClicks
    override fun recipientLongClicks(): Observable<Long> = adapter.recipientLongClicks
    override fun themeClicks(): Observable<Long> = adapter.themeClicks
    override fun nameClicks(): Observable<*> = adapter.nameClicks
    override fun nameChanges(): Observable<String> = nameChangeSubject
    override fun notificationClicks(): Observable<*> = adapter.notificationClicks
    override fun archiveClicks(): Observable<*> = adapter.archiveClicks
    override fun blockClicks(): Observable<*> = adapter.blockClicks
    override fun deleteClicks(): Observable<*> = adapter.deleteClicks
    override fun confirmDelete(): Observable<*> = confirmDeleteSubject
    override fun mediaClicks(): Observable<Long> = adapter.mediaClicks
    override val menuReadyIntent: Subject<Long> = PublishSubject.create()
    override fun showNameDialog(name: String) = nameDialog.setText(name).show()
    override val recipientClicks: Subject<Long> = PublishSubject.create()

    override fun showThemePicker(recipientId: Long) {
        router.pushController(
            RouterTransaction.with(ThemePickerController(recipientId))
                .pushChangeHandler(MessagesChangeHandler())
                .popChangeHandler(MessagesChangeHandler())
        )
    }

    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
        blockingDialog.show(activity!!, conversations, block)
        Log.i("TAG", "showBlockingDialog: block:- $block")
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog(activity!!)
    }

    override fun showDeleteDialog() {
        /*AlertDialog.Builder(activity!!)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(resources?.getQuantityString(R.plurals.dialog_delete_message, 1))
            .setPositiveButton(R.string.button_delete) { _, _ -> confirmDeleteSubject.onNext(Unit) }
            .setNegativeButton(R.string.button_cancel, null)
            .show()*/
        val context: Context = ContextThemeWrapper(activity, R.style.AppTheme2)
        MaterialAlertDialogBuilder(
            context,
            R.style.MaterialAlertDialog_rounded
        ).setTitle(R.string.dialog_delete_title)
            .setMessage(resources?.getQuantityString(R.plurals.dialog_delete_message, 1))
            .setPositiveButton(R.string.button_delete) { _, _ -> confirmDeleteSubject.onNext(Unit) }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }


}