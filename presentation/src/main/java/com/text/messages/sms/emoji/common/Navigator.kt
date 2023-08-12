package com.text.messages.sms.emoji.common

import android.app.Activity
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import android.webkit.MimeTypeMap
import com.text.messages.sms.emoji.R
import androidx.core.content.FileProvider
import com.text.messages.sms.emoji.BuildConfig
import com.text.messages.sms.emoji.ads.AdvertiseHandler
import com.text.messages.sms.emoji.common.util.RateMeNowDialog
import com.text.messages.sms.emoji.common.util.RateMeNowDialog.MAIL_TO_FEEDBACK
import com.text.messages.sms.emoji.common.util.RateMeNowDialog.PRIVACY_POLICY_LINK
import com.text.messages.sms.emoji.feature.backup.BackupActivity
import com.text.messages.sms.emoji.feature.blocking.BlockingActivity
import com.text.messages.sms.emoji.feature.compose.ComposeActivity
import com.text.messages.sms.emoji.feature.conversationinfo.ConversationInfoActivity
import com.text.messages.sms.emoji.feature.gallery.GalleryActivity
import com.text.messages.sms.emoji.feature.main.LanguageActivity
import com.text.messages.sms.emoji.feature.notificationprefs.NotificationPrefsActivity
import com.text.messages.sms.emoji.feature.plus.PlusActivity
import com.text.messages.sms.emoji.feature.scheduled.ScheduledActivity
import com.text.messages.sms.emoji.feature.settings.SettingsActivity
import com.text.messages.sms.emoji.manager.AnalyticsManager
import com.text.messages.sms.emoji.manager.BillingManager
import com.text.messages.sms.emoji.manager.NotificationManager
import com.text.messages.sms.emoji.manager.PermissionManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor(
    private val context: Context,
    private val analyticsManager: AnalyticsManager,
    private val billingManager: BillingManager,
    private val notificationManager: NotificationManager,
    private val permissions: PermissionManager
) {

    private fun startActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun startActivityExternal(intent: Intent) {
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent.createChooser(intent, null))
        }
    }

    /**
     * @param source String to indicate where this QKSMS+ screen was launched from. This should be
     * one of [main_menu, compose_schedule, settings_night, settings_theme]
     */
    fun showQksmsPlusActivity(source: String) {
        analyticsManager.track("Viewed QKSMS+", Pair("source", source))
        val intent = Intent(context, PlusActivity::class.java)
        startActivity(intent)
    }

    /**
     * This won't work unless we use startActivityForResult
     */
    fun showDefaultSmsDialog(context: Activity) {
        //AdvertiseHandler.getInstance().isNeedOpenAdRequest = false
        AdvertiseHandler.getInstance().disableAppOpenAds()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            context.startActivityForResult(intent, 42389)
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            context.startActivity(intent)
        }
    }

    fun showComposeCheck(body: Boolean? = null) {
        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra("Check", body)

        startActivity(intent)
    }

    fun showCompose(body: String? = null, images: List<Uri>? = null) {
        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra(Intent.EXTRA_TEXT, body)

        images?.takeIf { it.isNotEmpty() }?.let {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(images))
        }

        startActivity(intent)
    }
    fun showLanguage() {
        val intent = Intent(context, LanguageActivity::class.java)
        intent.putExtra("isMain",true)
        startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
    }

    fun feedBackIntent(){
        /*try {
            var str: String =
                "Version Name : " + BuildConfig.VERSION_NAME + "\n" + "Version Code : " + BuildConfig.VERSION_CODE + "\n" + "Device Info : " + BuildConfig.VERSION_CODE + "\n" + "OS version : " + System.getProperty(
                    "os.version"
                ) + "\n" + "API Level : " + Build.VERSION.SDK + "\n" + "Device : " + Build.DEVICE + "\n" + "Model : " + Build.MODEL + "\n" + "Product : " + Build.PRODUCT + "\n"
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "com.text.messages.sms.emoji"))
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name) + " Feedback")
            intent.putExtra(Intent.EXTRA_TEXT, str)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            //TODO smth
        }*/
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(MAIL_TO_FEEDBACK))
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name) + " Support")
        intent.putExtra(Intent.EXTRA_TEXT, StringBuilder("\n\n")
            .append("\n\n--- Tell Us Your Thoughts above this line ---\n\n")
            .append("Package: ${context.packageName}\n")
            .append("Version: ${BuildConfig.VERSION_NAME}\n")
            .append("Version Code: ${BuildConfig.VERSION_CODE}\n")
            .append("Device: ${Build.BRAND} ${Build.MODEL}\n")
            .append("SDK: ${Build.VERSION.SDK_INT}\n")
            .append("Upgraded"
                .takeIf { BuildConfig.FLAVOR != "noAnalytics" }
                .takeIf { billingManager.upgradeStatus.blockingFirst() } ?: "")
            .toString())
        startActivityExternal(intent)
    }

    fun showConversation(threadId: Long, query: String? = null) {
        val intent = Intent(context, ComposeActivity::class.java)
            .putExtra("threadId", threadId)
            .putExtra("query", query)
        startActivity(intent)
    }

    fun showConversationInfo(threadId: Long) {
        val intent = Intent(context, ConversationInfoActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    fun showMedia(partId: Long) {
        val intent = Intent(context, GalleryActivity::class.java)
        intent.putExtra("partId", partId)
        startActivity(intent)
    }

    fun showBackup() {
        analyticsManager.track("Viewed Backup")
        startActivity(Intent(context, BackupActivity::class.java))
    }

    fun showScheduled() {
        analyticsManager.track("Viewed Scheduled")
        val intent = Intent(context, ScheduledActivity::class.java)
        startActivity(intent)
    }

    fun showSettings() {
        val intent = Intent(context, SettingsActivity::class.java)
        startActivity(intent)

        if (context is Activity) {
            context.finish()
        }
    }

    fun showDeveloper() {
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moezbhatti"))
//        startActivityExternal(intent)
    }

    fun showSourceCode() {
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moezbhatti/qksms"))
//        startActivityExternal(intent)
    }

    fun showChangelog() {
//        val intent =
//            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moezbhatti/qksms/releases"))
//        startActivityExternal(intent)
    }

    fun showLicense() {
//        val intent = Intent(
//            Intent.ACTION_VIEW,
//            Uri.parse("https://github.com/moezbhatti/qksms/blob/master/LICENSE")
//        )
//        startActivityExternal(intent)
    }

    fun showBlockedConversations() {
        val intent = Intent(context, BlockingActivity::class.java)
        startActivity(intent)
    }

    fun makePhoneCall(address: String) {
        val action = if (permissions.hasCalling()) Intent.ACTION_CALL else Intent.ACTION_DIAL
        val intent = Intent(action, Uri.parse("tel:$address"))
        startActivityExternal(intent)
    }

    fun showDonation() {
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/QKSMSDonation"))
//        startActivityExternal(intent)
    }

    fun showRating() {
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.moez.QKSMS"))
//            .addFlags(
//                Intent.FLAG_ACTIVITY_NO_HISTORY
//                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
//                        or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
//            )
//
//        try {
//            startActivityExternal(intent)
//        } catch (e: ActivityNotFoundException) {
//            val url = "http://play.google.com/store/apps/details?id=com.moez.QKSMS"
//            startActivityExternal(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
//        }
    }

    /**
     * Launch the Play Store and display the Call Blocker listing
     */
    fun installCallBlocker() {
        val url = "https://play.google.com/store/apps/details?id=com.cuiet.blockCalls"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivityExternal(intent)
    }

    /**
     * Launch the Play Store and display the Call Control listing
     */
    fun installCallControl() {
        val url =
            "https://play.google.com/store/apps/details?id=com.flexaspect.android.everycallcontrol"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivityExternal(intent)
    }

    /**
     * Launch the Play Store and display the Should I Answer? listing
     */
    fun installSia() {
        val url = "https://play.google.com/store/apps/details?id=org.mistergroup.shouldianswer"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivityExternal(intent)
    }

    fun showSupport() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(MAIL_TO_FEEDBACK))
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name) + "Support")
        intent.putExtra(Intent.EXTRA_TEXT, StringBuilder("\n\n")
            .append("\n\n--- Please write your message above this line ---\n\n")
            .append("Package: ${context.packageName}\n")
            .append("Version: ${BuildConfig.VERSION_NAME}\n")
            .append("Device: ${Build.BRAND} ${Build.MODEL}\n")
            .append("SDK: ${Build.VERSION.SDK_INT}\n")
            .append("Upgraded"
                .takeIf { BuildConfig.FLAVOR != "noAnalytics" }
                .takeIf { billingManager.upgradeStatus.blockingFirst() } ?: "")
            .toString())
        startActivityExternal(intent)
    }

    fun showInvite() {
        var i: Intent? = Intent(Intent.ACTION_SEND)
        i?.type = "text/plain"
        i?.flags = FLAG_ACTIVITY_NEW_TASK
        i?.putExtra(
            Intent.EXTRA_SUBJECT,
            context.applicationContext.resources.getString(R.string.app_name)
        )
        var sAux: String? =
            ("Let me recommend you this application. Click to blue link and download \n\n" + context.getString(
                R.string.app_name
            ) +
                    "\n" + RateMeNowDialog.PLAY_STORE_LINK + context.packageName)
        i?.putExtra(Intent.EXTRA_TEXT, sAux)

        val chooserIntent = Intent.createChooser(i, "Share With Friends")
        chooserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooserIntent)
    }

    fun openPrivacyPolicy() {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_LINK))
            browserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        } catch (throwable: Throwable) {
            Log.i("TAG", "openPrivacyPolicy: " + throwable.message)
        }
    }

    fun addContact(address: String) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setType(ContactsContract.Contacts.CONTENT_TYPE)
            .putExtra(ContactsContract.Intents.Insert.PHONE, address)

        startActivityExternal(intent)
    }

    fun showContact(lookupKey: String) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey))

        startActivityExternal(intent)
    }

    fun viewFile(file: File) {
        val data = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.name.split(".").last())
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(data, type)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivityExternal(intent)
    }

    fun shareFile(file: File) {
        val data = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.name.split(".").last())
        val intent = Intent(Intent.ACTION_SEND)
            .setType(type)
            .putExtra(Intent.EXTRA_STREAM, data)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivityExternal(intent)
    }

    fun showNotificationSettings(threadId: Long = 0) {
        val intent = Intent(context, NotificationPrefsActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }


    fun showNotificationChannel(threadId: Long = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (threadId != 0L) {
                notificationManager.createNotificationChannel(threadId)
            }

            val channelId = notificationManager.buildNotificationChannelId(threadId)
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            startActivity(intent)
        }
    }

    fun openLanguage(threadId: Long = 0) {
        val intent = Intent(context, LanguageActivity::class.java)
        intent.putExtra("threadId", threadId)
        intent.putExtra("isMain",true)
        startActivity(intent)
    }
}
