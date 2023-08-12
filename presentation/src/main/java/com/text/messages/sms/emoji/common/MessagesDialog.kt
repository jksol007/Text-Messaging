/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.text.messages.sms.emoji.common

import android.app.Activity
import android.content.Context
import android.view.ContextThemeWrapper
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.common.util.extensions.dpToPx
import com.text.messages.sms.emoji.common.util.extensions.setPadding
import com.text.messages.sms.emoji.injection.appComponent
import javax.inject.Inject

/**
 * Wrapper around AlertDialog which makes it easier to display lists that use our UI
 */
class MessagesDialog @Inject constructor(private val context: Context, val adapter: MenuItemAdapter) {

    var title: String? = null

    init {
        appComponent.inject(this)
    }

    fun show(activity: Activity) {
        val recyclerView = RecyclerView(activity)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.setPadding(top = 8.dpToPx(context), bottom = 8.dpToPx(context))

        /*val dialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(recyclerView)
            .create()*/

        val context: Context = ContextThemeWrapper(activity, R.style.AppTheme2)
        val dialog = MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog_rounded)
            .setTitle(title)
            .setView(recyclerView)
            .create()

        val clicks = adapter.menuItemClicks
            .subscribe {
                if (dialog.isShowing && !activity.isFinishing) {
                    dialog.dismiss()
                    activity.recreate()
                }
            }

        dialog.setOnDismissListener {
            clicks.dispose()
        }

        if (!activity.isFinishing) {
            try {
                dialog.show()
            } catch (E: Exception) {

            }
        }
    }

    fun setTitle(@StringRes title: Int) {
        this.title = context.getString(title)
    }

}