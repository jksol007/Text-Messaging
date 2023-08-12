package com.jksol.appmodule.exit

import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.jksol.appmodule.R
import com.jksol.appmodule.ads.AdmobAdManager
import com.jksol.appmodule.ads.ExitDialogNative
import com.jksol.appmodule.rate.RateDialog
import com.jksol.appmodule.utils.Constants

class ExitDialog(
    val context: AppCompatActivity,
    var emailId: String,
    private var listener: ExitDialogNative.dialogDismiss
) {


    fun handleExit() {
        val sharedPreferences = context.getSharedPreferences(
            Constants.appName,
            AppCompatActivity.MODE_PRIVATE
        )

        if (sharedPreferences != null && sharedPreferences.getBoolean("rate_us", true)) {
            val rateDialog = RateDialog(context, Constants.appName, emailId)
            rateDialog.showRateUsDialog()
        } else {
//            exitDialog()
            context.finishAffinity()
        }
    }

    private fun exitDialog() {
        if (AdmobAdManager.instance?.mNativeRateAd != null) {
            val exitRateDialog = ExitDialogNative(AdmobAdManager.instance!!.mNativeRateAd) {
                listener.onDismiss(it)
                if (!TextUtils.isEmpty(Constants.nativeId)) {
                    AdmobAdManager.instance?.LoadNativeRateAd(context, Constants.nativeId, null)
                }
            }
            exitRateDialog.show(context.supportFragmentManager, exitRateDialog.tag)
        } else {
            askForExit()
        }
    }

    private fun askForExit() {
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.exit_warning))
            .setCancelable(true)
            .setPositiveButton(
                context.getString(R.string.exit_yes)
            ) { dialog, id ->
                dialog.dismiss()
                context.finishAffinity()
            }.setNegativeButton(
                context.getString(R.string.exit_cancel)
            ) { dialog, which -> dialog.dismiss() }.show()
    }
}