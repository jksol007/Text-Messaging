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
package com.text.messages.sms.emoji.injection.android

import com.chating.messages.feature.main.Splash_demo
import com.text.messages.sms.emoji.feature.backup.BackupActivity
import com.text.messages.sms.emoji.feature.blocking.BlockingActivity
import com.text.messages.sms.emoji.feature.compose.ComposeActivity
import com.text.messages.sms.emoji.feature.compose.ComposeActivityModule
import com.text.messages.sms.emoji.feature.contacts.ContactsActivity
import com.text.messages.sms.emoji.feature.contacts.ContactsActivityModule
import com.text.messages.sms.emoji.feature.conversationinfo.ConversationInfoActivity
import com.text.messages.sms.emoji.feature.gallery.GalleryActivity
import com.text.messages.sms.emoji.feature.gallery.GalleryActivityModule
import com.text.messages.sms.emoji.feature.main.*
import com.text.messages.sms.emoji.feature.notificationprefs.NotificationPrefsActivity
import com.text.messages.sms.emoji.feature.notificationprefs.NotificationPrefsActivityModule
import com.text.messages.sms.emoji.feature.plus.PlusActivity
import com.text.messages.sms.emoji.feature.plus.PlusActivityModule
import com.text.messages.sms.emoji.feature.messagesreply.MessagesReplyActivity
import com.text.messages.sms.emoji.feature.messagesreply.MessagesReplyActivityModule
import com.text.messages.sms.emoji.feature.scheduled.ScheduledActivity
import com.text.messages.sms.emoji.feature.scheduled.ScheduledActivityModule
import com.text.messages.sms.emoji.feature.settings.SettingsActivity
import com.text.messages.sms.emoji.injection.scope.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [PlusActivityModule::class])
    abstract fun bindPlusActivity(): PlusActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBackupActivity(): BackupActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ComposeActivityModule::class])
    abstract fun bindComposeActivity(): ComposeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ContactsActivityModule::class])
    abstract fun bindContactsActivity(): ContactsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindConversationInfoActivity(): ConversationInfoActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [GalleryActivityModule::class])
    abstract fun bindGalleryActivity(): GalleryActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [NotificationPrefsActivityModule::class])
    abstract fun bindNotificationPrefsActivity(): NotificationPrefsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [MessagesReplyActivityModule::class])
    abstract fun bindQkReplyActivity(): MessagesReplyActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ScheduledActivityModule::class])
    abstract fun bindScheduledActivity(): ScheduledActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBlockingActivity(): BlockingActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindSplashScreen(): Splash_demo

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindGetStartActivity(): GetStartActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindLanguageActivity(): LanguageActivity

}
