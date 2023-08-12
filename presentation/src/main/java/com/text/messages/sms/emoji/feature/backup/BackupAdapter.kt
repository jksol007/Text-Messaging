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
package com.text.messages.sms.emoji.feature.backup

import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.common.base.FlowableAdapter
import com.text.messages.sms.emoji.common.base.MessagesViewHolder
import com.text.messages.sms.emoji.common.util.DateFormatter
import com.text.messages.sms.emoji.model.BackupFile
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.backup_list_item.*
import javax.inject.Inject

class BackupAdapter @Inject constructor(
    private val context: Context,
    private val dateFormatter: DateFormatter
) : FlowableAdapter<BackupFile>() {

    val backupSelected: Subject<BackupFile> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.backup_list_item, parent, false)

        return MessagesViewHolder(view).apply {
            view.setOnClickListener { backupSelected.onNext(getItem(adapterPosition)) }
        }
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        val backup = getItem(position)

        val count = backup.messages

        holder.title.text = dateFormatter.getDetailedTimestamp(backup.date)
        holder.messages.text = context.resources.getQuantityString(R.plurals.backup_message_count, count, count)
        holder.size.text = Formatter.formatFileSize(context, backup.size)
    }

}