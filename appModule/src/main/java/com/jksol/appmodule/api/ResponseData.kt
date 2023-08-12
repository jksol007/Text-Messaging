package com.jksol.appmodule.api

import com.google.gson.annotations.SerializedName

class ResponseData {

    @SerializedName("info")
    var info: Info? = null


    class Datum {

        @SerializedName("name")
        var name: String? = ""

        @SerializedName("icon")
        var icon: String? = ""

        @SerializedName("link")
        var link: String? = ""

        @SerializedName("isAppLive")
        var isAppLive: Boolean = true

    }

    class Info {
        @SerializedName("newAppInfo")
        var newAppInfo: List<Datum> = ArrayList()

        @SerializedName("isMainAppLive")
        var isMainAppLive: Boolean = false
    }
}