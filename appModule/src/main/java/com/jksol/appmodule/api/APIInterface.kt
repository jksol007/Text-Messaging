package com.jksol.appmodule.api

import com.jksol.appmodule.BuildConfig
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET


interface APIInterface {

    @GET(BuildConfig.FILEPATH)
    fun doGetListResources(): Call<String>?

}