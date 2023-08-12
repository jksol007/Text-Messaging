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
package com.text.messages.sms.emoji.feature.blocking.numbers

import com.text.messages.sms.emoji.common.base.MessagesPresenter
import com.text.messages.sms.emoji.interactor.MarkUnblocked
import com.text.messages.sms.emoji.repository.BlockingRepository
import com.text.messages.sms.emoji.repository.ConversationRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BlockedNumbersPresenter @Inject constructor(
    private val blockingRepo: BlockingRepository,
    private val conversationRepo: ConversationRepository,
    private val markUnblocked: MarkUnblocked
) : MessagesPresenter<BlockedNumbersView, BlockedNumbersState>(
    BlockedNumbersState(numbers = blockingRepo.getBlockedNumbers())
) {

    override fun bindIntents(view: BlockedNumbersView) {
        super.bindIntents(view)

        view.unblockAddress()
            .doOnNext { view.unblockNumber(it) }
            .subscribeOn(Schedulers.io())
            .autoDispose(view.scope())
            .subscribe()

        view.addAddress()
            .autoDispose(view.scope())
            .subscribe { view.showAddDialog() }

//        view.addAddress()
//                .autoDispose(view.scope())
//                .subscribe {  }

        view.saveAddress()
            .subscribeOn(Schedulers.io())
            .autoDispose(view.scope())
            .subscribe { address -> blockingRepo.blockNumber(address) }
    }

}
