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
package com.text.messages.sms.emoji.feature.blocking

import android.app.Activity
import android.content.Context
import android.view.ContextThemeWrapper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.blocking.BlockingClient
import com.text.messages.sms.emoji.interactor.MarkBlocked
import com.text.messages.sms.emoji.interactor.MarkUnblocked
import com.text.messages.sms.emoji.repository.ConversationRepository
import com.text.messages.sms.emoji.util.Preferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// TODO: Once we have a custom dialog based on conductor, turn this into a controller
class BlockingDialog @Inject constructor(
    private val blockingManager: BlockingClient,
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val prefs: Preferences,
    private val markBlocked: MarkBlocked,
    private val markUnblocked: MarkUnblocked
) {

    fun show(activity: Activity, conversationIds: List<Long>, block: Boolean) = GlobalScope.launch {
        val addresses = conversationIds.toLongArray()
            .let { conversationRepo.getConversations(*it) }
            .flatMap { conversation -> conversation.recipients }
            .map { it.address }
            .distinct()

        blockContact(activity, addresses, conversationIds, block)
    }

    suspend fun blockContact(
        activity: Activity,
        addresses: List<String>,
        conversationIds: List<Long>,
        block: Boolean
    ) {

        if (addresses.isEmpty()) {
            return
        }

        if (blockingManager.getClientCapability() == BlockingClient.Capability.BLOCK_WITHOUT_PERMISSION) {
            // If we can block/unblock in the external manager, then just fire that off and exit
            if (block) {
                markBlocked.execute(
                    MarkBlocked.Params(
                        conversationIds,
                        prefs.blockingManager.get(),
                        null
                    )
                )
                blockingManager.block(addresses).subscribe()
            } else {
                markUnblocked.execute(conversationIds)
                blockingManager.unblock(addresses).subscribe()
            }
        } else if (block == allBlocked(addresses)) {
            // If all of the addresses are already in their correct state in the blocking manager, just marked the
            // conversations blocked and exit
            when (block) {
                true -> markBlocked.execute(
                    MarkBlocked.Params(
                        conversationIds,
                        prefs.blockingManager.get(),
                        null
                    )
                )
                false -> markUnblocked.execute(conversationIds)
            }
        } else {
            // Otherwise, show the UI that lets the users know they need to mark the number as blocked in the client
            showDialog(activity, conversationIds, addresses, block)
        }
    }

    private fun allBlocked(addresses: List<String>): Boolean = addresses.all { address ->
        blockingManager.isBlacklisted(address).blockingGet() is BlockingClient.Action.Block
    }

    private suspend fun showDialog(
        activity: Activity,
        conversationIds: List<Long>,
        addresses: List<String>,
        block: Boolean
    ) = withContext(MainScope().coroutineContext) {
        val res = when (block) {
            true -> R.plurals.blocking_block_external
            false -> R.plurals.blocking_unblock_external
        }

        val manager = context.getString(
            when (prefs.blockingManager.get()) {
                Preferences.BLOCKING_MANAGER_CB -> R.string.blocking_manager_call_blocker_title
                Preferences.BLOCKING_MANAGER_CC -> R.string.blocking_manager_call_control_title
                Preferences.BLOCKING_MANAGER_SIA -> R.string.blocking_manager_sia_title
                else -> R.string.blocking_manager_qksms_title
            }
        )

        val message = context.resources.getQuantityString(res, addresses.size, manager)

        // Otherwise, show a dialog asking the user if they want to be directed to the external
        // blocking manager
        val context: Context = ContextThemeWrapper(activity, R.style.AppTheme2)
        MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog_rounded)
            .setTitle(
                when (block) {
                    true -> R.string.blocking_block_title
                    false -> R.string.blocking_unblock_title
                }
            )
            .setMessage(message)
            .setPositiveButton(R.string.button_continue) { _, _ ->
                if (block) {
                    markBlocked.execute(
                        MarkBlocked.Params(
                            conversationIds,
                            prefs.blockingManager.get(),
                            null
                        )
                    )
                    blockingManager.block(addresses).subscribe()
                } else {
                    markUnblocked.execute(conversationIds)
                    blockingManager.unblock(addresses).subscribe()
                }
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
            .create()
            .show()

        /*AlertDialog.Builder(activity)
            .setTitle(
                when (block) {
                    true -> R.string.blocking_block_title
                    false -> R.string.blocking_unblock_title
                }
            )
            .setMessage(message)
            .setPositiveButton(R.string.button_continue) { _, _ ->
                if (block) {
                    markBlocked.execute(
                        MarkBlocked.Params(
                            conversationIds,
                            prefs.blockingManager.get(),
                            null
                        )
                    )
                    blockingManager.block(addresses).subscribe()
                } else {
                    markUnblocked.execute(conversationIds)
                    blockingManager.unblock(addresses).subscribe()
                }
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
            .create()
            .show()*/
    }

}
