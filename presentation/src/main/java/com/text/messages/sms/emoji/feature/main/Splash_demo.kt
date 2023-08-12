package com.chating.messages.feature.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chating.messages.common.session.SessionManager
import com.jksol.appmodule.utils.Constants
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.ads.AdvertiseHandler
import com.text.messages.sms.emoji.ads.AppUtils
import com.text.messages.sms.emoji.common.PreferencesManager
import com.text.messages.sms.emoji.common.base.MessagesThemedActivity
import com.text.messages.sms.emoji.feature.main.LanguageActivity
import com.text.messages.sms.emoji.feature.main.MainActivity
import com.text.messages.sms.emoji.feature.main.ThreadUtil
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.splashscreen.*

class Splash_demo : MessagesThemedActivity(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {

    var check_Privacy: CheckBox? = null
    var ll_privacy: ConstraintLayout? = null
    var cl_splash: ConstraintLayout? = null
    var btn_GetStarted: Button? = null
    var tv_Policy: TextView? = null
    var txt_icon: TextView? = null
    var txt_sub: TextView? = null


    /* @Inject
     lateinit var advertiseHandler: AdvertiseHandler*/

    lateinit var sm: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

//        getWindow().setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )
        //AppUtils.setLightStatusBarColor(window.decorView, this)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O_MR1) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.splashscreen)
        advertiseHandler = AdvertiseHandler.getInstance()
        advertiseHandler.setAppOpen()
        advertiseHandler.loadNativeAds(
            null,
            resources.getString(R.string.admob_language_native_ads_id),
            this@Splash_demo
        )
        advertiseHandler.isAppStartUpAdsEnabled = true
        advertiseHandler.loadInterstitialAds(this@Splash_demo,
            resources.getString(R.string.admob_splash_interstitial_ads_id),
            object : AdvertiseHandler.Listener() {

                override fun onAdsLoadCompleted() {
                    super.onAdsLoadCompleted()
                    Log.e("TAG69", "onAdsLoadCompleted: ")
                }

            })
        Constants.isSplashScreen = true
//        ExitNativeAdInit(this, resources.getString(R.string.admob_exit_native_ads_id))
//        advertiseHandler.loadInterstitialAds(2, null)
//
//
//
//        advertiseHandler.loadNativeAds(object : AdvertiseHandler.Listener() {
//            override fun onAdsLoadCompleted() {
//
//
//                //advertiseHandler.showNativeAds(
////                    this@LanguageSelectActivity,
////                    native_ads,
////                    true,
////                    R.layout.native_ads_admob
////                )
//            }
//
//        }, resources.getString(R.string.admob_language_native_ads_id))

        check_Privacy = findViewById<CheckBox>(R.id.cbPrivacy)
        ll_privacy = findViewById<ConstraintLayout>(R.id.cl_default)
        cl_splash = findViewById<ConstraintLayout>(R.id.cl_splash)
        btn_GetStarted = findViewById<Button>(R.id.btn_get_start)

        tv_Policy = findViewById<TextView>(R.id.txt_start_privacy)
        txt_icon = findViewById<TextView>(R.id.txt_icon)
        //txt_icon!!.setVisible(false)
        txt_sub = findViewById<TextView>(R.id.txt_sub)
        //txt_icon!!.setVisible(false)
        btn_GetStarted!!.setEnabled(false)
        sm = SessionManager.getInstance(applicationContext)
        tv_Policy!!.setOnClickListener(this)
        btn_get_start!!.setOnClickListener(this)
        animationStart()

        val b: Boolean = PreferencesManager.getStart(this)

        if (!b) {
            Handler(Looper.getMainLooper()).postDelayed({
                ll_privacy!!.setVisibility(View.VISIBLE)
                cl_splash!!.setVisibility(View.GONE)
            }, 1000)


//            ll_privacy!!.setVisibility(View.VISIBLE)
            check_Privacy!!.setVisibility(View.VISIBLE)
            btn_GetStarted!!.setVisibility(View.VISIBLE)

            val spannableString = SpannableString("Privacy Policy")
            val foregroundSpan = ForegroundColorSpan(resources.getColor(R.color.tools_theme))
            spannableString.setSpan(
                foregroundSpan,
                0,
                spannableString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            check_Privacy!!.setOnCheckedChangeListener(this)

        } else {
            ll_privacy!!.setVisibility(View.GONE)
            cl_splash!!.setVisibility(View.VISIBLE)
            gotoNextActivity()
        }

//        gotoNextActivity()

    }

    private fun animationStart() {
        val a: Animation = AnimationUtils.loadAnimation(this, R.anim.scale)
        a.reset()
        txt_sub!!.startAnimation(a);
        txt_icon!!.startAnimation(a);
    }

    private fun onClickStart() {
        PreferencesManager.setStart(this, true)
        /*startActivity(Intent(this@GetStartActivity, SplashScreen::class.java))
        finish()*/
        if (!AppUtils.isEmptyString(PreferencesManager.getLanguage(this))) {
            startActivity(Intent(this@Splash_demo, MainActivity::class.java))
            finish()
        } else {
            startActivity(
                Intent(
                    this@Splash_demo,
                    LanguageActivity::class.java
                ).putExtra("isMain", false)
            )
            finish()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        btn_GetStarted!!.setEnabled(isChecked)
        btn_GetStarted!!.setBackgroundResource(if (isChecked) R.drawable.ads_rounded_corner_bg else R.drawable.rounded_corner_grey_bg)
        btn_GetStarted!!.setTextColor(
            ContextCompat.getColor(
                this,
                if (isChecked) R.color.white else R.color.white
            )
        )
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        advertiseHandler.isAppStartUpAdsEnabled = true
//        advertiseHandler.loadInterstitialAds()
//        gotoNextActivity()
//    }

    private fun gotoNextActivity() {

        if (!PreferencesManager.getStart(this)) {
            ThreadUtil.startTask({
                val i = Intent(applicationContext, LanguageActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                finish()
            }, 1000)
        } else {


//            ThreadUtil.startTask({
//                val i = Intent(applicationContext, MainActivity::class.java)
//                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(i)
//                finish()
//            }, 3000)


            ThreadUtil.startTask({


                Log.e(
                    "TAG204",
                    "gotoNextActivity: " + advertiseHandler.isInterstitialAdsAvailableToShow(this@Splash_demo)
                )
                if (advertiseHandler.isAppStartUpAdsEnabled && advertiseHandler.isInterstitialAdsAvailableToShow(
                        this@Splash_demo
                    )
                ) {
                    advertiseHandler.showInterstitialAds(
                        this@Splash_demo,
                        object : AdvertiseHandler.Listener() {

                            override fun onAdsClosed() {
                                advertiseHandler.isAppStartUpAdsEnabled = false
                                val i = Intent(applicationContext, MainActivity::class.java)
                                i.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(i)
                                finish()
                            }

                            override fun onAdsLoadFailed() {
                                onAdsClosed()
                            }
                        })
                } else {

                    val i = Intent(applicationContext, MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)

                    finish()

                }
            }, 2500)
        }


    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_get_start ->
                onClickStart()
            R.id.txt_start_privacy ->
                AppUtils.openPrivacyPolicy(this)

        }
    }
}