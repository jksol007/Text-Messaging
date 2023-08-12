package com.text.messages.sms.emoji.feature.main

import java.io.Serializable

class LanguageEntity : Serializable {
    var color: Int = 0
    var name: Int = 0
    var code: String = ""
    var selected: Boolean = false

    constructor(color: Int, name: Int, code: String, selected: Boolean) {
        this.color = color
        this.name = name
        this.code = code
        this.selected = selected
    }
}