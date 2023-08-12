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
package com.text.messages.sms.emoji.common.base

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.text.messages.sms.emoji.common.PreferencesManager
import com.text.messages.sms.emoji.common.util.extensions.setVisible
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.*
import timber.log.Timber
import java.util.*

abstract class MessagesRealmAdapter<T : RealmModel> :
    RealmRecyclerViewAdapter<T, MessagesViewHolder>(null, true) {

    /**
     * This view can be set, and the adapter will automatically control the visibility of this view
     * based on the data
     */
    var emptyView: View? = null
        set(value) {
            if (field === value) return

            field = value
            value?.setVisible(data?.isLoaded == true && data?.isEmpty() == true)
        }

    private val emptyListener: (OrderedRealmCollection<T>) -> Unit = { data ->
        emptyView?.setVisible(data.isLoaded && data.isEmpty())
    }

    val selectionChanges: Subject<List<Long>> = BehaviorSubject.create()

    var selection = listOf<Long>()

    /**
     * Toggles the selected state for a particular view
     *
     * If we are currently in selection mode (we have an active selection), then the state will
     * toggle. If we are not in selection mode, then we will only toggle if [force]
     */
    protected fun toggleSelection(id: Long, force: Boolean = true): Boolean {
        if (!force && selection.isEmpty()) return false

        selection = when (selection.contains(id)) {
            true -> selection - id
            false -> selection + id
        }

        selectionChanges.onNext(selection)
        return true
    }

    init {

    }

    protected fun isSelected(id: Long): Boolean {
        return selection.contains(id)
    }

    fun clearSelection() {
        selection = listOf()
        selectionChanges.onNext(selection)
        notifyDataSetChanged()
    }

    fun clearMenuSelection(isSel: Int) {
        if (isSel == 0) {

        }
        selection = listOf()
        selectionChanges.onNext(selection)
        notifyDataSetChanged()
    }

    override fun getItem(index: Int): T? {
        if (index < 0) {
            Timber.w("Only indexes >= 0 are allowed. Input was: $index")
            return null
        }

        return super.getItem(index)
    }

    override fun updateData(data: OrderedRealmCollection<T>?) {
        if (getData() === data) return

        removeListener(getData())
        addListener(data)

        data?.run(emptyListener)

        super.updateData(data)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        updateLanguage(recyclerView.context)
        addListener(data)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        updateLanguage(recyclerView.context)
        removeListener(data)
    }

    private fun addListener(data: OrderedRealmCollection<T>?) {
        when (data) {
            is RealmResults<T> -> data.addChangeListener(emptyListener)
            is RealmList<T> -> data.addChangeListener(emptyListener)
        }
    }

    private fun removeListener(data: OrderedRealmCollection<T>?) {
        when (data) {
            is RealmResults<T> -> data.removeChangeListener(emptyListener)
            is RealmList<T> -> data.removeChangeListener(emptyListener)
        }
    }

    open fun updateLanguage(context: Context): Context? {
        var languageCode: String = PreferencesManager.getLanguage(context)!!
        val resources = context.resources
        val configuration = resources.configuration
        if (languageCode == null || languageCode.trim { it <= ' ' }.isEmpty()) {
            try {
                languageCode = configuration.locale.language
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
        Log.d("TAG", "updateLanguage:1 " + languageCode)
        if (languageCode != null && !languageCode.trim { it <= ' ' }.isEmpty()) {
            languageCode = languageCode.toLowerCase()
            val locale = Locale(languageCode)
            //            Locale.setDefault(locale);
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                return context.createConfigurationContext(configuration)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
            Log.d("TAG", "updateLanguage:2 " + languageCode)


        }
        return context
    }

}