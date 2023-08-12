package com.text.messages.sms.emoji.feature.main

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chating.messages.feature.main.Splash_demo
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.ads.AdvertiseHandler
import com.text.messages.sms.emoji.ads.AppUtils
import com.text.messages.sms.emoji.common.PreferencesManager
import com.text.messages.sms.emoji.common.base.MessagesThemedActivity
import dagger.android.AndroidInjection


class LanguageActivity : MessagesThemedActivity(),
    View.OnClickListener, LifecycleOwner {

    private var txt_toolbar_title: TextView? = null
    private var rv_lang_list: RecyclerView? = null

    private lateinit var img_toolbar_back: ImageView
    private var img_toolbar_done: ImageView? = null
    private var bannerAdLayout: LinearLayout? = null

    //lateinit var advertiseHandler: AdvertiseHandler
    private var language_list: ArrayList<LanguageEntity>? = null
    private var languageAdapter: LanguageSelectionAdapter? = null

    //private var languageViewModel : LanguageViewModel? = null
    var night_mode = false
    var firstTime = false
    var nightModeFlags = 0
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isMain: Boolean = false

    companion object {
        var languageChanged: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        /*contentView.layoutTransition = LayoutTransition().apply {
            disableTransitionType(LayoutTransition.CHANGING)
        }*/

        handler = Handler(Looper.myLooper()!!)
        runnable = Runnable { }

        init()
        UIComponent()
    }

    private fun init() {
        language_list = ArrayList()
        if (intent.extras != null) {
            isMain = intent.extras!!.getBoolean("isMain", false)
        }
    }

    private fun UIComponent() {
        //AppUtils.setLightStatusBarColor(window.decorView, this)
        nightModeFlags =
            application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == 32 && !night_mode) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        } else if (nightModeFlags == 16 && !night_mode && firstTime) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        changeScreenLanguage()
        txt_toolbar_title = findViewById(R.id.txt_toolbar_title)
        rv_lang_list = findViewById(R.id.rv_lang_list)
        img_toolbar_done = findViewById(R.id.img_toolbar_done)
        img_toolbar_back = findViewById(R.id.img_toolbar_back)
        bannerAdLayout = findViewById(R.id.bannerAdLayout)
        loadBottomBannerAdd()
        advertiseHandler.loadInterstitialAds(this@LanguageActivity, resources.getString(R.string.admob_langage_interstitial_ads_id),null)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_lang_list!!.setLayoutManager(linearLayoutManager)

        if (intent.getBooleanExtra("isMain", false)) {
            img_toolbar_back.visibility = View.VISIBLE
            //showBackButton(true)
        } else {
            //showBackButton(false)
            img_toolbar_back.visibility = View.GONE
        }

        addDatatoList()

        if (AppUtils.isEmptyString(PreferencesManager.getLanguage(this))) {
            language_list!![0].selected = true
        } else {
            var lang: String = PreferencesManager.getLanguage(this)
            if (!AppUtils.isEmptyString(lang)) {
                for (i in language_list!!) {
                    if (lang.equals(i.code)) {
                        i.selected = true
                    } else {
                        i.selected = false
                    }
                }
            } else {
                language_list!!.get(0).selected = true
            }
        }

        setAdapter()
        /*languageViewModel = LanguageViewModel()
        languageViewModel = ViewModelProviders.of(this).get(LanguageViewModel::class.java)

        languageViewModel!!.getLangMutableLiveData().observe(this ,langListUpdateObserver)*/

        img_toolbar_done!!.setOnClickListener(this)
        img_toolbar_back!!.setOnClickListener(this)
        //loadInterstitialAdd()
    }

    /* var  langListUpdateObserver: Observer<ArrayList<LanguageEntity>> =
        object : Observer<ArrayList<LanguageEntity>> {
            override fun onChanged(t: ArrayList<LanguageEntity>?) {
                languageAdapter!!.updateLangList(t);
            }
        }*/

    fun setAdapter() {
        languageAdapter = LanguageSelectionAdapter(this, language_list)
        rv_lang_list!!.setAdapter(languageAdapter)

    }

    private fun loadBottomBannerAdd() {
        //advertiseHandler.showInterstitialAds(this, listener)
        advertiseHandler.showNativeAds(
            this@LanguageActivity,
            bannerAdLayout,
            true,
            R.layout.native_ads_admob, null
        )
    }

    private fun loadInterstitialAdd() {
        //advertiseHandler.showInterstitialAds(this, listener)
        advertiseHandler.showInterstitialAds(
            this@LanguageActivity, false,
            object : AdvertiseHandler.Listener() {
                override fun onAdsClosed() {
                    advertiseHandler.isAppStartUpAdsEnabled = false
                    startActivity(
                        Intent(this@LanguageActivity, MainActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    )
//                    setResult(Activity.RESULT_OK)
                    finish()
                }
                override fun onAdsLoadFailed() {
                    onAdsClosed()
                }
            }, true
        )
    }

    private fun changeScreenLanguage() {
        if (AppUtils.isEmptyString(PreferencesManager.getLanguage(this))) {
            AppUtils.setLocale(this, "en")
        } else {
            AppUtils.setLocale(this, PreferencesManager.getLanguage(this))
        }
    }

    private fun addDatatoList() {
        language_list!!.clear()
        language_list!!.add(
            LanguageEntity(
                R.color.color_united_states,
                R.string.english_lang,
                "en",
                false
            )
        )
        language_list!!.add(LanguageEntity(R.color.color_spain, R.string.spanish_lang, "es", false))
        language_list!!.add(LanguageEntity(R.color.color_france, R.string.france_lang, "fr", false))
        language_list!!.add(
            LanguageEntity(
                R.color.color_united_kingdom,
                R.string.korean_lang,
                "ko",
                false
            )
        )
        language_list!!.add(
            LanguageEntity(
                R.color.color_germany,
                R.string.german_lang,
                "de",
                false
            )
        )
        language_list!!.add(LanguageEntity(R.color.color_italy, R.string.italian_lang, "it", false))
        language_list!!.add(
            LanguageEntity(
                R.color.color_portugal,
                R.string.portuguese_lang,
                "pt",
                false
            )
        )
    }

    override fun onClick(v: View?) {
        if (v!!.id.equals(R.id.img_toolbar_done)) {
            var lang_code: String = languageAdapter!!.getSelectedItem()!!.code
            if (!AppUtils.isEmptyString(lang_code)) {
                PreferencesManager.setLanguage(this, lang_code)
                updateLanguage(this)
                languageChanged = true
            } else {
                PreferencesManager.setLanguage(this, "en")
                languageChanged = true
            }
            if (isMain) {
                loadInterstitialAdd()
            } else {
                startActivity(
                    Intent(
                        this@LanguageActivity,
                        MainActivity::class.java
                    )
                )
                finish()
            }

        } else if (v!!.id.equals(R.id.img_toolbar_back)) {
            onBackPressed()
            finish()
        }
    }

    fun gotoMain() {
        startActivity(
            Intent(
                this@LanguageActivity,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        if (isMain) {
            startActivity(Intent(this@LanguageActivity, MainActivity::class.java))
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            startActivity(
                Intent(
                    this@LanguageActivity,
                    Splash_demo::class.java
                ).putExtra("isDefault", true)
            )
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // API 5+ solution
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

