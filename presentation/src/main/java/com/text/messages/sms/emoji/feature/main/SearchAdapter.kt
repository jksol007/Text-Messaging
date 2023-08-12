package com.text.messages.sms.emoji.feature.main

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.common.Navigator
import com.text.messages.sms.emoji.common.base.MessagesAdapter
import com.text.messages.sms.emoji.common.base.MessagesViewHolder
import com.text.messages.sms.emoji.common.util.Colors
import com.text.messages.sms.emoji.common.util.DateFormatter
import com.text.messages.sms.emoji.common.util.extensions.setVisible
import com.text.messages.sms.emoji.extensions.removeAccents
import com.text.messages.sms.emoji.model.SearchResult
import kotlinx.android.synthetic.main.search_list_item.*
import javax.inject.Inject

class SearchAdapter @Inject constructor(
    colors: Colors,
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator
) : MessagesAdapter<SearchResult>() {

    private val highlightColor: Int by lazy { colors.theme().highlight }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.search_list_item, parent, false)
        return MessagesViewHolder(view).apply {
            view.setOnClickListener {
                val result = getItem(adapterPosition)
                navigator.showConversation(
                    result.conversation.id,
                    result.query.takeIf { result.messages > 0 })
            }
        }
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        val previous = data.getOrNull(position - 1)
        val result = getItem(position)

        holder.resultsHeader.setVisible(result.messages > 0 && previous?.messages == 0)

        val query = result.query
        val title = SpannableString(result.conversation.getTitle())
        var index = title.removeAccents().indexOf(query, ignoreCase = true)

        if (index < query.length) {
            var max = title.length
            val textLength = title.length - 1
            while (index >= 0) {
                max = index + query.length
                if (max > textLength) {
                    max = textLength
                }
                title.setSpan(
                    BackgroundColorSpan(highlightColor),
                    index,
                    (max),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                index = title.indexOf(query, max, true)
            }
        }
        holder.title.text = title

        holder.avatars.recipients = result.conversation.recipients

        when (result.messages == 0) {
            true -> {
                holder.date.setVisible(true)
                holder.date.text = dateFormatter.getConversationTimestamp(result.conversation.date)
                holder.snippet.text = when (result.conversation.me) {
                    true -> context.getString(R.string.main_sender_you, result.conversation.snippet)
                    false -> result.conversation.snippet
                }
            }

            false -> {
                holder.date.setVisible(false)
                holder.snippet.text =
                    context.getString(R.string.main_message_results, result.messages)
            }
        }
    }

    override fun areItemsTheSame(old: SearchResult, new: SearchResult): Boolean {
        return old.conversation.id == new.conversation.id && old.messages > 0 == new.messages > 0
    }

    override fun areContentsTheSame(old: SearchResult, new: SearchResult): Boolean {
        return old.query == new.query && // Queries are the same
                old.conversation.id == new.conversation.id // Conversation id is the same
                && old.messages == new.messages // Result count is the same
    }
}