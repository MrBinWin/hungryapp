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

package com.mrbinwin.hungryapp.fragments

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.R
import com.mrbinwin.hungryapp.data.models.Recipe
import com.mrbinwin.hungryapp.di.components.DaggerFragmentComponent
import com.mrbinwin.hungryapp.mvp.contracts.RecipeDetailsViewContract
import com.mrbinwin.hungryapp.mvp.presenters.RecipeDetailsPresenter
import com.mrbinwin.hungryapp.utils.extension.formatToTime
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates


class RecipeDetailsFragment: BaseFragment(), RecipeDetailsViewContract {

    @BindView(R.id.card_recipe_cooking_time) lateinit var cardCookingTime: CardView
    @BindView(R.id.card_recipe_directions) lateinit var cardDirections: CardView
    @BindView(R.id.card_recipe_ingredients) lateinit var cardIngredients: CardView
    @BindView(R.id.button_add_to_shopping_list) lateinit var buttonAddToShoppingList: FloatingActionButton
    @BindView(R.id.image_recipe_picture) lateinit var imageRecipePicture: ImageView
    @BindView(R.id.text_recipe_ingredients) lateinit var textRecipeIngredients: TextView
    @BindView(R.id.text_recipe_cooking_time) lateinit var textRecipeCookingTime: TextView
    @BindView(R.id.text_recipe_directions) lateinit var textRecipeDirections: TextView

    @Inject lateinit var applicationContext: Context
    @Inject lateinit var presenter: RecipeDetailsPresenter

    override var addIngredientsToShoppingListClickEvent: Observable<String>? = null
    override var recipeDeleteClickEvent: Observable<Unit>? = null
    override val isSupportUpButtonShown: Boolean = true
    override var recipeId: Int by Delegates.notNull()

    private var recipe: Recipe? = null
    private val addIngredientsToShoppingListClickSubject = PublishSubject.create<String>()
    private val recipeDeleteClickSubject = PublishSubject.create<Unit>()

    companion object {
        const val ARGUMENT_RECIPE_ID = "ARGUMENT_RECIPE_ID"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        DaggerFragmentComponent.builder().appComponent(App.component).build().inject(this)
        presenter.attachView(this)
        val view: View = inflater.inflate(R.layout.fragment_recipe_details, container, false)
        ButterKnife.bind(this, view)

        val bundle = this.arguments
        bundle?.let {
            recipeId = bundle.getInt(ARGUMENT_RECIPE_ID)
        }
        setHasOptionsMenu(true)
        addIngredientsToShoppingListClickEvent = addIngredientsToShoppingListClickSubject.hide()
        recipeDeleteClickEvent = recipeDeleteClickSubject.hide()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonAddToShoppingList.setOnClickListener {
            recipe?.let { _recipe ->
                addIngredientsToShoppingListClickSubject.onNext(_recipe.ingredients)
            }
        }
        presenter.viewIsReady()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.recipe_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_edit_recipe -> {
                val action = RecipeDetailsFragmentDirections.actionRecipeDetailsFragmentToRecipeEditFragment(recipeId)
                findNavController().navigate(action)
                true
            }
            R.id.menu_delete_recipe -> {
                context?.let { _context ->
                    recipe?.let { _recipe ->
                        val title = _context.getString(
                            R.string.recipes_delete_confirmation_title,
                            resources.getQuantityString(R.plurals.recipe, 1,1)
                        )
                        AlertDialog.Builder(_context)
                            .setTitle(title)
                            .setMessage("- ${_recipe.title}")
                            .setNegativeButton(R.string.no, null)
                            .setPositiveButton(R.string.yes) { _, _ -> recipeDeleteClickSubject.onNext(Unit) }
                            .show()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun loadData(recipe: Recipe) {
        this.recipe = recipe
        activity?.title = recipe.title

        if (!TextUtils.isEmpty(recipe.ingredients)) {
            textRecipeIngredients.text = recipe.ingredients
        } else {
            cardIngredients.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(recipe.cookingTime)) {
            textRecipeCookingTime.text = recipe.cookingTime.formatToTime(applicationContext)
        } else {
            cardCookingTime.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(recipe.directions)) {
            textRecipeDirections.text = recipe.directions
        } else {
            cardDirections.visibility = View.GONE
        }

        updatePicture(recipe.picture)
    }

    override fun showMessageProductsAddedToShoppingList(ingredients: Int) {
        context?.let { _context ->
            val message = _context.getString(
                R.string.recipe_message_products_added,
                resources.getQuantityString(R.plurals.product_added, ingredients, ingredients)
            )
            Toast.makeText(App.context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePicture(path: String?) {
        if (path == null || TextUtils.isEmpty(path)) {
            imageRecipePicture.setImageDrawable(null)
            imageRecipePicture.visibility = View.GONE
        } else {
            Glide.with(this)
                .load(path)
                .centerCrop()
                .into(imageRecipePicture)
            imageRecipePicture.visibility = View.VISIBLE
        }
    }

    override fun showRecipes() {
        findNavController().navigate(R.id.action_global_recipes_fragment)
    }

    override fun showShoppingList() {
        findNavController().navigate(R.id.action_global_shopping_list_fragment)
    }
}