package com.text.messages.sms.emoji.feature.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.common.util.extensions.setBackgroundTint
import kotlinx.android.synthetic.main.item_menu.view.*

class GridMenuAdapter(context: Context) :
    RecyclerView.Adapter<GridMenuAdapter.MenuViewHolder>() {

    var listener: GridMenuListener? = null

    private val menus = arrayListOf(
        Menu(
            context.getString(R.string.camera),
            R.drawable.ic_attach_camera,
            R.color.cam_color
        ),
        Menu(
            context.getString(R.string.gallery),
            R.drawable.ic_attach_picture,
            R.color.med_color
        ),
        Menu(
            context.getString(R.string.contacts),
            R.drawable.ic_attach_contactt,
            R.color.con_color
        ),
        Menu(
            context.getString(R.string.location),
            R.drawable.ic_attach_location,
            R.color.loc_color
        ),
        Menu(
            context.getString(R.string.schedule_message),
            R.drawable.ic_attach_sch,
            R.color.msg_color
        )
    )

    interface GridMenuListener {
        fun dismissPopup()
        fun menuName(menu: String)
    }

    private val data = ArrayList<Menu>().apply {
        addAll(menus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder.create(
            parent,
            viewType
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(data[position], listener)
    }

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            menu: Menu,
            listener: GridMenuListener?
        ) {
            with(itemView) {
                tvTitle.text = menu.name
                ivIcon.setImageDrawable(ContextCompat.getDrawable(context, menu.drawable))
                ivIcon.setBackgroundTint(ContextCompat.getColor(context, menu.color))
                itemView.setOnClickListener {
                    /*Toast.makeText(it.context, "Menu ${menu.name} clicked", Toast.LENGTH_SHORT)
                        .show()*/
                    listener?.menuName(menu.name)
                    listener?.dismissPopup()
                }
            }
        }

        companion object {
            val LAYOUT = R.layout.item_menu

            fun create(parent: ViewGroup, viewType: Int): MenuViewHolder {
                return MenuViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        LAYOUT,
                        parent,
                        false
                    )
                )
            }
        }
    }

    data class Menu(val name: String, @DrawableRes val drawable: Int, @ColorRes val color: Int) {

    }
}