package com.text.messages.sms.emoji.feature.main

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.text.messages.sms.emoji.feature.widget.GridMenuAdapter

class MenuRecyclerView : RecyclerView {

    private val spanCount = 3

    private val manager = GridLayoutManager(context, spanCount)
    private val adapter = GridMenuAdapter(context)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        setHasFixedSize(true)
        layoutManager = manager
        setAdapter(adapter)
        addItemDecoration(MenuGridDecoration())
    }

    fun addMenuClickListener(listener: GridMenuAdapter.GridMenuListener) {
        adapter.listener = listener
    }

}