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
package com.text.messages.sms.emoji.feature.themepicker

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.makeramen.roundedimageview.RoundedImageView
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.common.base.MessagesAdapter
import com.text.messages.sms.emoji.common.base.MessagesViewHolder
import com.text.messages.sms.emoji.common.util.Colors
import com.text.messages.sms.emoji.common.util.extensions.setTint
import com.text.messages.sms.emoji.common.util.extensions.setVisible
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.theme_palette_list_item.*
import javax.inject.Inject

class ThemeAdapter @Inject constructor(
    private val context: Context,
    private val colors: Colors
) : MessagesAdapter<List<Int>>() {

    val colorSelected: Subject<Int> = PublishSubject.create()

    var selectedColor: Int = -1
        set(value) {
            val oldPosition = data.indexOfFirst { it.contains(field) }
            val newPosition = data.indexOfFirst { it.contains(value) }

            field = value
            iconTint = colors.textPrimaryOnThemeForColor(value)

            oldPosition.takeIf { it != -1 }?.let { position -> notifyItemChanged(position) }
            newPosition.takeIf { it != -1 }?.let { position -> notifyItemChanged(position) }
        }

    private var iconTint = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.theme_palette_list_item, parent, false)

        return MessagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        val palette = getItem(position)

        (holder.color1 as RoundedImageView).setColorFilter(palette[0])
        (holder.color2 as RoundedImageView).setColorFilter(palette[1])
        (holder.color3 as RoundedImageView).setColorFilter(palette[2])
        (holder.color4 as RoundedImageView).setColorFilter(palette[3])
        (holder.color5 as RoundedImageView).setColorFilter(palette[4])
        (holder.color6 as RoundedImageView).setColorFilter(palette[5])
        (holder.color7 as RoundedImageView).setColorFilter(palette[6])
        (holder.color8 as RoundedImageView).setColorFilter(palette[7])
        (holder.color9 as RoundedImageView).setColorFilter(palette[8])
        (holder.color10 as RoundedImageView).setColorFilter(palette[9])

        holder.check1.setVisible(palette[0] == selectedColor)
        holder.check2.setVisible(palette[1] == selectedColor)
        holder.check3.setVisible(palette[2] == selectedColor)
        holder.check4.setVisible(palette[3] == selectedColor)
        holder.check5.setVisible(palette[4] == selectedColor)
        holder.check6.setVisible(palette[5] == selectedColor)
        holder.check7.setVisible(palette[6] == selectedColor)
        holder.check8.setVisible(palette[7] == selectedColor)
        holder.check9.setVisible(palette[8] == selectedColor)
        holder.check10.setVisible(palette[9] == selectedColor)

        holder.check1.setTint(iconTint,false)
        holder.check2.setTint(iconTint,false)
        holder.check3.setTint(iconTint,false)
        holder.check4.setTint(iconTint,false)
        holder.check5.setTint(iconTint,false)
        holder.check6.setTint(iconTint,false)
        holder.check7.setTint(iconTint,false)
        holder.check8.setTint(iconTint,false)
        holder.check9.setTint(iconTint,false)
        holder.check10.setTint(iconTint,false)

        holder.view1.setOnClickListener { colorSelected.onNext(palette[0]) }
        holder.view2.setOnClickListener { colorSelected.onNext(palette[1]) }
        holder.view3.setOnClickListener { colorSelected.onNext(palette[2]) }
        holder.view4.setOnClickListener { colorSelected.onNext(palette[3]) }
        holder.view5.setOnClickListener { colorSelected.onNext(palette[4]) }
        holder.view6.setOnClickListener { colorSelected.onNext(palette[5]) }
        holder.view7.setOnClickListener { colorSelected.onNext(palette[6]) }
        holder.view8.setOnClickListener { colorSelected.onNext(palette[7]) }
        holder.view9.setOnClickListener { colorSelected.onNext(palette[8]) }
        holder.view10.setOnClickListener { colorSelected.onNext(palette[9]) }

    }

}