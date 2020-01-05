/*
 * Copyright (C) 2019 MrBinWin (https://github.com/MrBinWin/),
 *                         Dmitry Kuznetsov <mrbinwin@gmail.com>
 *
 * This file is part of HungryApp.
 *
 * HungryApp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HungryApp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HungryApp.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.mrbinwin.hungryapp.adapters

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.mrbinwin.hungryapp.R
import com.mrbinwin.hungryapp.custom_views.CustomImageView
import com.mrbinwin.hungryapp.data.models.Product
import java.util.*
import kotlin.collections.ArrayList


/**
 * The adapter represents products list
 *
 */
class ProductsAdapter: RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>(), ItemTouchableAdapter {

    var eventsListener: EventsListener? = null

    var products: ArrayList<Product> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        context = parent.context
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.textName.text = product.title

        if (products[position].is_bought) {
            holder.checkboxIsBought.isChecked = true
            holder.textName.paintFlags = holder.textName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.checkboxIsBought.isChecked = false
            holder.textName.paintFlags = holder.textName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    /**
     * Drag&drop sorting
     *
     */
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(products, i, i + 1)
                val rank1 = products[i].rank
                val rank2 = products[i+1].rank
                products[i].rank = rank2
                products[i+1].rank = rank1
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(products, i, i - 1)
                val rank1 = products[i].rank
                val rank2 = products[i-1].rank
                products[i].rank = rank2
                products[i-1].rank = rank1
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    /**
     * Swipe does nothing
     *
     */
    override fun onItemDismiss(position: Int) {}

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.checkbox_product_is_bought) lateinit var checkboxIsBought: CheckBox
        @BindView(R.id.image_product_reorder) lateinit var imageReorder: CustomImageView
        @BindView(R.id.text_product_name) lateinit var textName: TextView
        init {
            ButterKnife.bind(this, itemView)
            itemView.setOnLongClickListener {
                eventsListener?.onProductLongClicked(adapterPosition, products[adapterPosition]) ?: false
            }
            checkboxIsBought.setOnClickListener {
                eventsListener?.onProductChecked(adapterPosition, products[adapterPosition], checkboxIsBought.isChecked)
            }
            imageReorder.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    eventsListener?.onProductStartDrag(this)
                }
                false
            }
        }
    }

    interface EventsListener {
        fun onProductChecked(position: Int, product: Product, checked: Boolean)
        fun onProductLongClicked(position: Int, product: Product): Boolean
        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        fun onProductStartDrag(viewHolder: ProductViewHolder)
    }

}