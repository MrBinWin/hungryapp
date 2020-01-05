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
import android.graphics.Color
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrbinwin.hungryapp.R
import com.mrbinwin.hungryapp.data.models.Recipe
import com.mrbinwin.hungryapp.utils.dpToPx


/**
 * The adapter represents recipes list
 *
 */
class RecipesAdapter: SelectableAdapter<RecipesAdapter.RecipeViewHolder>() {

    var eventsListener: EventsListener? = null

    var recipes: ArrayList<Recipe> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        context = parent.context
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        val name: String = recipe.title
        holder.textName.text = name

        val ingredients: String = recipe.ingredients
        val ingredientsLabel: String = context?.getString(R.string.recipe_ingredients) ?: ""
        holder.textDescription.text = context?.getString(R.string.recipes_description_template, ingredientsLabel, ingredients)

        context?.let { _context ->

            if (!TextUtils.isEmpty(recipe.picture)) {
                val imagePadding = dpToPx(16)
                val cornerRadius = dpToPx(8)
                holder.imagePicture.setPadding(imagePadding)

                Glide.with(_context)
                    .load(recipe.picture)
                    .transform(CenterCrop(), RoundedCorners(cornerRadius))
                    .into(holder.imagePicture)
            } else {
                holder.imagePicture.setPadding(0)
                holder.imagePicture.setImageDrawable(ContextCompat.getDrawable(_context, R.drawable.ic_insert_photo_24dp))
            }


            val typedValue = TypedValue()
            val attr = if (isItemSelected(position)) {
                R.attr.colorBackgroundSelected
            } else {
                android.R.attr.colorBackground
            }
            val color = if (_context.theme.resolveAttribute(attr, typedValue,true)) {
                typedValue.data
            } else {
                Color.TRANSPARENT
            }
            holder.layout.setBackgroundColor(color)
        }

    }

    override fun onViewRecycled(holder: RecipeViewHolder) {
        context?.let {
            Glide.with(it).clear(holder.imagePicture)
        }
        super.onViewRecycled(holder)
    }

    fun getSelectedRecipeIds(): ArrayList<Int> {
        val result: ArrayList<Int> = ArrayList()
        getSelectedItems().forEach {
            recipes[it].id?.let { _recipeId ->
                result.add(_recipeId)
            }
        }
        return result
    }

    fun getSelectedRecipes(): ArrayList<Recipe> {
        val result: ArrayList<Recipe> = ArrayList()
        getSelectedItems().forEach {
            recipes[it].let { recipe ->
                result.add(recipe)
            }
        }
        return result
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.image_recipe_picture) lateinit var imagePicture: ImageView
        @BindView(R.id.layout_recipe) lateinit var layout: ViewGroup
        @BindView(R.id.text_recipe_name) lateinit var textName: TextView
        @BindView(R.id.text_recipe_description) lateinit var textDescription: TextView
        init {
            ButterKnife.bind(this, itemView)
            itemView.setOnClickListener {
                eventsListener?.onRecipeClicked(adapterPosition, recipes[adapterPosition])
            }
            itemView.setOnLongClickListener {
                eventsListener?.onRecipeLongClicked(adapterPosition, recipes[adapterPosition]) ?: false
            }
        }
    }

    interface EventsListener {
        fun onRecipeClicked(position: Int, recipe: Recipe)
        fun onRecipeLongClicked(position: Int, recipe: Recipe): Boolean
    }

}