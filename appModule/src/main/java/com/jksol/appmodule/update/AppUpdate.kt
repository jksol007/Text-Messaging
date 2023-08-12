package com.jksol.appmodule.update

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.gson.Gson
import com.jksol.appmodule.BuildConfig
import com.jksol.appmodule.R
import com.jksol.appmodule.api.APIClient
import com.jksol.appmodule.api.APIInterface
import com.jksol.appmodule.api.ResponseData
import com.jksol.appmodule.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess


class AppUpdate(var context: Activity) {

    var newVersionCode: Int = 0
    var appLink = ""

    private fun showUpdateDialog(isNotNow: Boolean) {
        try {
            val rateUsDialog = Dialog(context)
            rateUsDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            rateUsDialog.setContentView(R.layout.dialog_update_app)
            rateUsDialog.setCancelable(true)
            rateUsDialog.setCanceledOnTouchOutside(true)
            val btnNotNow = rateUsDialog.findViewById<TextView>(R.id.btn_not_now)
            val btnUpdate = rateUsDialog.findViewById<TextView>(R.id.btn_update)
            val appIcon = rateUsDialog.findViewById<ImageView>(R.id.appIcon)
            val titleTxt = rateUsDialog.findViewById<TextView>(R.id.titleTxt)
            try {
                val icon: Drawable = context.packageManager.getApplicationIcon(context.packageName)
                appIcon.setImageDrawable(icon)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            btnNotNow.setOnClickListener { v: View? ->
                rateUsDialog.dismiss()
//                Constants.isUpdateDialogVisible = false
                if (!isNotNow) {
                    exitProcess(0)
                }
            }

            btnUpdate.setOnClickListener { v: View? ->
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(appLink)
                context.startActivity(i)
                rateUsDialog.dismiss()
//                Constants.isUpdateDialogVisible = false
            }
            val sharedPreferences = context.getSharedPreferences(
                Constants.appName,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().putBoolean("updateShown", true).apply()
            if (!Constants.isUpdateDialogVisible) {
                Constants.isUpdateDialogVisible = true
                rateUsDialog.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showUpdateDialog1(
        isNotNow: Boolean,
        title: String,
        desc: String,
        isAppLive: Boolean,
        isAppRejected: Boolean
    ) {
        try {
            val rateUsDialog = Dialog(context)
            rateUsDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            rateUsDialog.setContentView(R.layout.dialog_update_app)
            rateUsDialog.setCancelable(true)
            rateUsDialog.setCanceledOnTouchOutside(true)
            val btnNotNow = rateUsDialog.findViewById<TextView>(R.id.btn_not_now)
            val btnUpdate = rateUsDialog.findViewById<TextView>(R.id.btn_update)
            val appIcon = rateUsDialog.findViewById<ImageView>(R.id.appIcon)
            val titleTxt = rateUsDialog.findViewById<TextView>(R.id.titleTxt)
            val descTxt = rateUsDialog.findViewById<TextView>(R.id.descTxt)
            titleTxt.text = title
            descTxt.text = desc
            try {
                val icon: Drawable = context.packageManager.getApplicationIcon(context.packageName)
                appIcon.setImageDrawable(icon)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            btnNotNow.setOnClickListener { v: View? ->
                rateUsDialog.dismiss()
                Constants.isUpdateDialogVisible = false
                if (!isNotNow) {
                    exitProcess(0)
                }
            }

            btnUpdate.setOnClickListener { v: View? ->
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(appLink)
                context.startActivity(i)
                rateUsDialog.dismiss()
                Constants.isUpdateDialogVisible = false
            }
            val sharedPreferences = context.getSharedPreferences(
                Constants.appName,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().putBoolean("updateShown", true).apply()
            if (!Constants.isUpdateDialogVisible) {
                Constants.isUpdateDialogVisible = true
                rateUsDialog.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun checkForUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(context)

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo


        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            Log.e("TAG153", "appUpdateInfoTask: "+(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE))
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val newVersionCode = appUpdateInfo.availableVersionCode()
                this.newVersionCode = newVersionCode
                try {
                    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pInfo.longVersionCode.toInt()
                    } else {
                        pInfo.versionCode
                    }
                    val remain = newVersionCode - version
                    if (remain >= 3) {
                        appLink =
                            "https://play.google.com/store/apps/details?id=${context.packageName}"
                        showUpdateDialog(false)
                    } else {
                        val sharedPreferences = context.getSharedPreferences(
                            Constants.appName,
                            Context.MODE_PRIVATE
                        )
                        val isShown = sharedPreferences.getBoolean("updateShown", false)
                        if (!isShown) {
                            appLink =
                                "https://play.google.com/store/apps/details?id=${context.packageName}"
                            showUpdateDialog(true)
                        }
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    Log.e("TAG183", "checkForUpdate: "+e.toString() )
                }
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            Log.e("check185", "checkForUpdate: " )
//            findAppLink()
        }
    }


    private fun findAppLink() {
        val apiInterface = APIClient.getClient()?.create(APIInterface::class.java)
        val call: Call<String>? = apiInterface!!.doGetListResources()


        call!!.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>?, response: Response<String?>) {
                if (response.raw().isSuccessful) {
                    val resource: String? = response.body()
                    Log.e("check200", "onResponse: "+resource )
                    try {
                        processData(resource)
                    } catch (e: JWTVerificationException) {
                        e.printStackTrace()
                        //Invalid signature/claims
                    }

                    Log.e("dsds", "dsdsf")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable?) {
                Log.e("TAG212", "onFailure: " )
//                findAppLink1()
                call.cancel()
            }
        })
    }

    private fun findAppLink1() {
        Log.e("TAG220", "onFailure: " )
        val apiInterface = APIClient.getClient1()?.create(APIInterface::class.java)
        val call: Call<String>? = apiInterface!!.doGetListResources()


        call!!.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>?, response: Response<String?>) {
                if (response.raw().isSuccessful) {
                    val resource: String? = response.body()
                    try {
                        processData(resource)
                    } catch (e: JWTVerificationException) {
                        e.printStackTrace()
                        //Invalid signature/claims
                    }

                    Log.e("dsds", "dsdsf")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable?) {
                call.cancel()
            }
        })
    }


    private fun processData(resource: String?) {


        val tokenString: String = BuildConfig.API_KEY
        val algorithm: Algorithm = Algorithm.HMAC256(tokenString)
        val verifier: JWTVerifier = JWT.require(algorithm).build()
        val t = verifier.verify(resource)
        val s = t!!.getClaim("string").asString()
        val data = Gson().fromJson(s, ResponseData::class.java)
        if (data.info!!.newAppInfo !=null && data.info!!.newAppInfo.size > 0){
            appLink = data.info!!.newAppInfo[0].link.toString()
        }
        val appname = data.info!!.newAppInfo[0].name.toString()
        val isAppLive = data.info!!.newAppInfo[0].isAppLive
        val isMainAppLive = data.info!!.isMainAppLive
        val desc = "We have updated current app to $appname app and we have updated new features and fix bugs to this app\nPlease download this new app for better user experience."
        val sharedPreferences = context.getSharedPreferences(
            Constants.appName,
            Context.MODE_PRIVATE
        )
        val lastShownTime = sharedPreferences.getLong("lastShownTime", 0L)
        val shownCount = sharedPreferences.getInt("shownCount", 0)
        if (isAppLive && !isMainAppLive) {
            if (shownCount >= 3) {
                showUpdateDialog1(true, appname, desc, isAppLive, isMainAppLive)
            } else {
                if (lastShownTime == 0L) {
                    sharedPreferences.edit().putInt("shownCount", shownCount + 1)
                        .apply()
                    sharedPreferences.edit()
                        .putLong("lastShownTime", System.currentTimeMillis())
                        .apply()
                    showUpdateDialog1(true, appname, desc, isAppLive, isMainAppLive)
                } else {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                    val date = formatter.format(lastShownTime)
                    val todayDate = formatter.format(System.currentTimeMillis())
                    if (!date.equals(todayDate)) {
                        sharedPreferences.edit()
                            .putInt("shownCount", shownCount + 1)
                            .apply()
                        sharedPreferences.edit()
                            .putLong("lastShownTime", System.currentTimeMillis())
                            .apply()
                        showUpdateDialog1(
                            true,
                            appname,
                            desc,
                            isAppLive,
                            isMainAppLive
                        )
                    }
                }
            }
        }
    }

}