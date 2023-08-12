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
package com.text.messages.sms.emoji.feature.compose.part

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.text.messages.sms.emoji.common.base.MessagesAdapter
import com.text.messages.sms.emoji.common.base.MessagesViewHolder
import com.text.messages.sms.emoji.common.util.Colors
import com.text.messages.sms.emoji.common.util.extensions.forwardTouches
import com.text.messages.sms.emoji.extensions.isSmil
import com.text.messages.sms.emoji.extensions.isText
import com.text.messages.sms.emoji.feature.compose.BubbleUtils.canGroup
import com.text.messages.sms.emoji.model.Message
import com.text.messages.sms.emoji.model.MmsPart
import com.text.messages.sms.emoji.util.Preferences
import io.reactivex.Observable
import kotlinx.android.synthetic.main.message_list_item_in.*
import javax.inject.Inject

class PartsAdapter @Inject constructor(
    colors: Colors,
    fileBinder: FileBinder,
    mediaBinder: MediaBinder,
    var prefs: Preferences,
    vCardBinder: VCardBinder
) : MessagesAdapter<MmsPart>() {

    init {

    }

    private val partBinders = listOf(mediaBinder, vCardBinder, fileBinder)

    var theme: Colors.Theme = colors.theme()
        set(value) {
            field = value
            partBinders.forEach { binder -> binder.theme = value }
        }

    val clicks: Observable<Long> = Observable.merge(partBinders.map { it.clicks })

    private lateinit var message: Message
    private var previous: Message? = null
    private var next: Message? = null
    private var holder: MessagesViewHolder? = null
    private var bodyVisible: Boolean = true

    fun setData(message: Message, previous: Message?, next: Message?, holder: MessagesViewHolder) {
        this.message = message
        this.previous = previous
        this.next = next
        this.holder = holder
        this.bodyVisible = holder.body.visibility == View.VISIBLE
        this.data = message.parts.filter { !it.isSmil() && !it.isText() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val layout = partBinders.getOrNull(viewType)?.partLayout ?: 0
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        holder?.containerView?.let(view::forwardTouches)
        updateLanguage(parent.context)
        parent.context.setTheme(getActivityThemeRes(prefs.black.get()))
        return MessagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        val part = data[position]
        val canGroupWithPrevious = canGroup(message, previous) || position > 0
        val canGroupWithNext = canGroup(message, next) || position < itemCount - 1 || bodyVisible

        partBinders
            .firstOrNull { it.canBindPart(part) }
            ?.bindPart(holder, part, message, canGroupWithPrevious, canGroupWithNext)
    }

    override fun getItemViewType(position: Int): Int {
        val part = data[position]
        return partBinders.indexOfFirst { it.canBindPart(part) }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        updateLanguage(recyclerView.context)
        recyclerView.context.setTheme(getActivityThemeRes(prefs.black.get()))
    }
}