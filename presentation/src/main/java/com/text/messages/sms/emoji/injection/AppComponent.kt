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
package com.text.messages.sms.emoji.injection

import com.text.messages.sms.emoji.common.MessagesApplication
import com.text.messages.sms.emoji.common.MessagesDialog
import com.text.messages.sms.emoji.common.util.MessagesChooserTargetService
import com.text.messages.sms.emoji.common.widget.*
import com.text.messages.sms.emoji.feature.backup.BackupController
import com.text.messages.sms.emoji.feature.blocking.BlockingController
import com.text.messages.sms.emoji.feature.blocking.manager.BlockingManagerController
import com.text.messages.sms.emoji.feature.blocking.messages.BlockedMessagesController
import com.text.messages.sms.emoji.feature.blocking.numbers.BlockedNumbersController
import com.text.messages.sms.emoji.feature.compose.editing.DetailedChipView
import com.text.messages.sms.emoji.feature.conversationinfo.injection.ConversationInfoComponent
import com.text.messages.sms.emoji.feature.settings.SettingsController
import com.text.messages.sms.emoji.feature.settings.about.AboutController
import com.text.messages.sms.emoji.feature.settings.swipe.SwipeActionsController
import com.text.messages.sms.emoji.feature.themepicker.injection.ThemePickerComponent
import com.text.messages.sms.emoji.feature.widget.WidgetAdapter
import com.text.messages.sms.emoji.injection.android.ActivityBuilderModule
import com.text.messages.sms.emoji.injection.android.BroadcastReceiverBuilderModule
import com.text.messages.sms.emoji.injection.android.ServiceBuilderModule
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    ActivityBuilderModule::class,
    BroadcastReceiverBuilderModule::class,
    ServiceBuilderModule::class])
interface AppComponent {

    fun conversationInfoBuilder(): ConversationInfoComponent.Builder
    fun themePickerBuilder(): ThemePickerComponent.Builder

    fun inject(application: MessagesApplication)

    fun inject(controller: AboutController)
    fun inject(controller: BackupController)
    fun inject(controller: BlockedMessagesController)
    fun inject(controller: BlockedNumbersController)
    fun inject(controller: BlockingController)
    fun inject(controller: BlockingManagerController)
    fun inject(controller: SettingsController)
    fun inject(controller: SwipeActionsController)

    fun inject(dialog: MessagesDialog)

    fun inject(service: WidgetAdapter)

    /**
     * This can't use AndroidInjection, or else it will crash on pre-marshmallow devices
     */
    fun inject(service: MessagesChooserTargetService)

    fun inject(view: AvatarView)
    fun inject(view: DetailedChipView)
    fun inject(view: PagerTitleView)
    fun inject(view: PreferenceView)
    fun inject(view: RadioPreferenceView)
    fun inject(view: MessagesEditText)
    fun inject(view: MessagesSwitch)
    fun inject(view: MessagesTextView)

}
