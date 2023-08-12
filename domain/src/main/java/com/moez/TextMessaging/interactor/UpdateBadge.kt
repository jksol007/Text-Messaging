package com.text.messages.sms.emoji.interactor

import com.text.messages.sms.emoji.manager.ShortcutManager
import com.text.messages.sms.emoji.manager.WidgetManager
import io.reactivex.Flowable
import javax.inject.Inject

class UpdateBadge @Inject constructor(
    private val shortcutManager: ShortcutManager,
    private val widgetManager: WidgetManager
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> {
        return Flowable.just(params)
                .doOnNext { shortcutManager.updateBadge() }
                .doOnNext { widgetManager.updateUnreadCount() }
    }

}