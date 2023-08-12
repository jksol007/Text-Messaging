package com.jksol.appmodule.rate

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jksol.appmodule.R
import com.jksol.appmodule.rate.design.BaseRatingBar
import com.jksol.appmodule.rate.design.RotationRatingBar
import kotlin.system.exitProcess

class RateDialog(var context: AppCompatActivity, var appName: String, var emailId: String) {


    fun showRateUsDialog() {
        val rateUsDialog = Dialog(context)
        rateUsDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        rateUsDialog.setContentView(R.layout.dialog_rate_us)
        rateUsDialog.setCancelable(true)
        rateUsDialog.setCanceledOnTouchOutside(true)
        val btn_not_now = rateUsDialog.findViewById<TextView>(R.id.btn_not_now)
        val btn_rate = rateUsDialog.findViewById<TextView>(R.id.btn_rate)
        val btn_later = rateUsDialog.findViewById<ImageView>(R.id.btn_later)

        val rotationRatingBar: RotationRatingBar =
            rateUsDialog.findViewById(R.id.rotationratingbar_main)
        rotationRatingBar.setOnRatingChangeListener(object :
            BaseRatingBar.OnRatingChangeListener {
            override fun onRatingChange(
                ratingBar: BaseRatingBar?,
                rating: Float,
                fromUser: Boolean
            ) {

            }
        })
        btn_not_now.setOnClickListener { v: View? ->
            rateUsDialog.dismiss()
            context.finishAffinity()
        }
        btn_later.setOnClickListener { v: View? -> rateUsDialog.dismiss() }
        btn_rate.setOnClickListener { v: View? ->
            val rating_count = rotationRatingBar.rating
            if (rating_count >= 4) {
                val sharedPreferences = context.getSharedPreferences(appName, MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("rate_us", false).apply()
                val url =
                    "https://play.google.com/store/apps/details?id=${context.packageName}"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                context.startActivity(i)
                rateUsDialog.dismiss()
                context.finishAffinity()
            } else if (rating_count <= 3 && rating_count > 0) {
                val sharedPreferences = context.getSharedPreferences(appName, MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("rate_us", false).apply()
                val emailIntent = Intent(
                    Intent.ACTION_SENDTO,
                    Uri.parse(
                        "mailto:?subject=$appName&to=" + Uri.encode(
                            emailId
                        )
                    )
                )
                emailIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    appName
                )
                try {
                    context.startActivity(Intent.createChooser(emailIntent, "Send email via..."))
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "There are no email clients installed.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                rateUsDialog.dismiss()
                context.finishAffinity()
            } else {
                Toast.makeText(
                    context,
                    "Please click on 5 Star to give us rating on playstore.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        rateUsDialog.show()
    }
}