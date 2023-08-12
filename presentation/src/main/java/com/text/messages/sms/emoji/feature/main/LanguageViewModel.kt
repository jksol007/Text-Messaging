package com.text.messages.sms.emoji.feature.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.text.messages.sms.emoji.R


class LanguageViewModel : ViewModel() {

    private var language_list: ArrayList<LanguageEntity>? = null
    private var mut_language_list: MutableLiveData<ArrayList<LanguageEntity>> = MutableLiveData()

    fun LanguageViewModel() {

        // call your Rest API in init method
        init()
    }

    fun getLangMutableLiveData(): MutableLiveData<ArrayList<LanguageEntity>> {
        return mut_language_list
    }

    fun init() {
        populateList()
        mut_language_list.value = language_list
    }

    fun populateList() {
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
}