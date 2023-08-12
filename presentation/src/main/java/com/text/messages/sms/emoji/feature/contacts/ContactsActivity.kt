/*
 * Copyright (C) 2019 Moez Bhatti <moez.bhatti@gmail.com>
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
package com.text.messages.sms.emoji.feature.contacts

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActions
import com.jakewharton.rxbinding2.widget.textChanges
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.common.ViewModelFactory
import com.text.messages.sms.emoji.common.base.MessagesThemedActivity
import com.text.messages.sms.emoji.common.util.extensions.hideKeyboard
import com.text.messages.sms.emoji.common.util.extensions.resolveThemeColor
import com.text.messages.sms.emoji.common.util.extensions.setBackgroundTint
import com.text.messages.sms.emoji.common.util.extensions.showKeyboard
import com.text.messages.sms.emoji.common.widget.MessagesDialog
import com.text.messages.sms.emoji.extensions.Optional
import com.text.messages.sms.emoji.feature.compose.editing.ComposeItem
import com.text.messages.sms.emoji.feature.compose.editing.ComposeItemAdapter
import com.text.messages.sms.emoji.feature.compose.editing.PhoneNumberAction
import com.text.messages.sms.emoji.feature.compose.editing.PhoneNumberPickerAdapter
import com.text.messages.sms.emoji.feature.main.MainActivity
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.contacts_activity.*
import javax.inject.Inject

class ContactsActivity : MessagesThemedActivity(), ContactsContract {

    companion object {
        const val SharingKey = "sharing"
        const val ChipsKey = "chips"
    }

    @Inject
    lateinit var contactsAdapter: ComposeItemAdapter
    @Inject
    lateinit var phoneNumberAdapter: PhoneNumberPickerAdapter
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val queryChangedIntent: Observable<CharSequence> by lazy { search.textChanges() }
    override val queryClearedIntent: Observable<*> by lazy { cancel.clicks() }
    override val queryEditorActionIntent: Observable<Int> by lazy { search.editorActions() }
    override val composeItemPressedIntent: Subject<ComposeItem> by lazy { contactsAdapter.clicks }
    override val composeItemLongPressedIntent: Subject<ComposeItem> by lazy { contactsAdapter.longClicks }
    override val phoneNumberSelectedIntent: Subject<Optional<Long>> by lazy { phoneNumberAdapter.selectedItemChanges }
    override val phoneNumberActionIntent: Subject<PhoneNumberAction> = PublishSubject.create()

    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[ContactsViewModel::class.java]
    }

    private val phoneNumberDialog by lazy {
        MessagesDialog(this).apply {
            titleRes = R.string.compose_number_picker_title
            adapter = phoneNumberAdapter
            positiveButton = R.string.compose_number_picker_always
            positiveButtonListener = { phoneNumberActionIntent.onNext(PhoneNumberAction.ALWAYS) }
            negativeButton = R.string.compose_number_picker_once
            negativeButtonListener = { phoneNumberActionIntent.onNext(PhoneNumberAction.JUST_ONCE) }
            cancelListener = { phoneNumberActionIntent.onNext(PhoneNumberAction.CANCEL) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contacts_activity)
        showBackButton(true)
        viewModel.bindView(this)

        contacts.adapter = contactsAdapter

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            search.setBackgroundTint(resolveThemeColor(R.attr.bubbleColor))
        }
    }

    override fun render(state: ContactsState) {
        cancel.isVisible = state.query.length > 1

        contactsAdapter.data = state.composeItems

        if (state.selectedContact != null && !phoneNumberDialog.isShowing) {
            phoneNumberAdapter.data = state.selectedContact.numbers
            phoneNumberDialog.subtitle = state.selectedContact.name
            phoneNumberDialog.show()
        } else if (state.selectedContact == null && phoneNumberDialog.isShowing) {
            phoneNumberDialog.dismiss()
        }
    }

    override fun clearQuery() {
        search.text = null
    }

    override fun openKeyboard() {
        search.postDelayed({
            search.showKeyboard()
        }, 200)
    }

    override fun finish(result: HashMap<String, String?>) {
        search.hideKeyboard()
        val intent = Intent().putExtra(ChipsKey, result)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode === KeyEvent.KEYCODE_BACK) {
            val intent: Intent = Intent(
                this@ContactsActivity,
                MainActivity::class.java
            )
            finish()
            startActivity(intent)
        }
        return true
        return super.onKeyDown(keyCode, event)
    }
}