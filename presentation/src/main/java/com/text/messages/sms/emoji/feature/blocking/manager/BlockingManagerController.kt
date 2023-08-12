package com.text.messages.sms.emoji.feature.blocking.manager

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.ContextThemeWrapper
import android.view.View
import androidx.core.view.isInvisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding2.view.clicks
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.common.base.MessagesController
import com.text.messages.sms.emoji.common.util.Colors
import com.text.messages.sms.emoji.common.util.extensions.resolveThemeColor
import com.text.messages.sms.emoji.injection.appComponent
import com.text.messages.sms.emoji.util.Preferences
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.blocking_manager_controller.*
import kotlinx.android.synthetic.main.blocking_manager_list_option.view.*
import javax.inject.Inject

class BlockingManagerController :
    MessagesController<BlockingManagerView, BlockingManagerState, BlockingManagerPresenter>(),
    BlockingManagerView {

    @Inject
    lateinit var colors: Colors
    @Inject
    override lateinit var presenter: BlockingManagerPresenter

    private val activityResumedSubject: PublishSubject<Unit> = PublishSubject.create()

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.blocking_manager_controller
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.blocking_manager_title)
        showBackButton(true)

        val states = arrayOf(
            intArrayOf(android.R.attr.state_activated),
            intArrayOf(-android.R.attr.state_activated)
        )

        val textTertiary = view.context.resolveThemeColor(android.R.attr.textColorTertiary)
        val imageTintList = ColorStateList(states, intArrayOf(colors.theme().theme, textTertiary))

        qksms.action.imageTintList = imageTintList
        callBlocker.action.imageTintList = imageTintList
        callControl.action.imageTintList = imageTintList
        shouldIAnswer.action.imageTintList = imageTintList
    }

    override fun onActivityResumed(activity: Activity) {
        activityResumedSubject.onNext(Unit)
    }

    override fun render(state: BlockingManagerState) {
        qksms.action.setImageResource(getActionIcon(true))
        qksms.action.isActivated = true
        qksms.action.isInvisible = state.blockingManager != Preferences.BLOCKING_MANAGER_QKSMS

        callBlocker.action.setImageResource(getActionIcon(state.callBlockerInstalled))
        callBlocker.action.isActivated = state.callBlockerInstalled
        callBlocker.action.isInvisible = state.blockingManager != Preferences.BLOCKING_MANAGER_CB
                && state.callBlockerInstalled

        callControl.action.setImageResource(getActionIcon(state.callControlInstalled))
        callControl.action.isActivated = state.callControlInstalled
        callControl.action.isInvisible = state.blockingManager != Preferences.BLOCKING_MANAGER_CC
                && state.callControlInstalled

        shouldIAnswer.action.setImageResource(getActionIcon(state.siaInstalled))
        shouldIAnswer.action.isActivated = state.siaInstalled
        shouldIAnswer.action.isInvisible = state.blockingManager != Preferences.BLOCKING_MANAGER_SIA
                && state.siaInstalled
    }

    private fun getActionIcon(installed: Boolean): Int = when {
        !installed -> R.drawable.ic_chevron_right_black_24dp
        else -> R.drawable.ic_check_white_24dp
    }

    override fun activityResumed(): Observable<*> = activityResumedSubject
    override fun qksmsClicked(): Observable<*> = qksms.clicks()
    override fun callBlockerClicked(): Observable<*> = callBlocker.clicks()
    override fun callControlClicked(): Observable<*> = callControl.clicks()
    override fun siaClicked(): Observable<*> = shouldIAnswer.clicks()

    override fun showCopyDialog(manager: String): Single<Boolean> = Single.create { emitter ->
        /*AlertDialog.Builder(activity)
                .setTitle(R.string.blocking_manager_copy_title)
                .setMessage(resources?.getString(R.string.blocking_manager_copy_summary, manager))
                .setPositiveButton(R.string.button_continue) { _, _ -> emitter.onSuccess(true) }
                .setNegativeButton(R.string.button_cancel) { _, _ -> emitter.onSuccess(false) }
                .setCancelable(false)
                .show()*/
        val context: Context = ContextThemeWrapper(activity, R.style.AppTheme2)
        MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog_rounded)
            .setTitle(R.string.blocking_manager_copy_title)
            .setMessage(resources?.getString(R.string.blocking_manager_copy_summary, manager))
            .setPositiveButton(R.string.button_continue) { _, _ -> emitter.onSuccess(true) }
            .setNegativeButton(R.string.button_cancel) { _, _ -> emitter.onSuccess(false) }
            .setCancelable(false)
            .show()
    }

}
