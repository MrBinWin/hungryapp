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

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.MainActivity
import com.mrbinwin.hungryapp.MainActivity.Companion.REQUEST_CODE_IMPORT_BACKUP
import com.mrbinwin.hungryapp.R
import com.mrbinwin.hungryapp.adapters.RecipesAdapter
import com.mrbinwin.hungryapp.data.models.Recipe
import com.mrbinwin.hungryapp.di.components.DaggerFragmentComponent
import com.mrbinwin.hungryapp.mvp.contracts.RecipesViewContract
import com.mrbinwin.hungryapp.mvp.presenters.RecipesPresenter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


class RecipesFragment: BaseFragment(), RecipesViewContract {

    companion object {
        private const val TAG_IS_IN_ACTION_MODE = "is_in_action_mode"
        private const val TAG_SELECTED_ADAPTER_ITEMS = "selected_adapter_items"
    }

    @BindView(R.id.button_create_recipe) lateinit var buttonCreateRecipe: FloatingActionButton
    @BindView(R.id.recipes_recycler) lateinit var recyclerViewRecipes: RecyclerView

    @Inject lateinit var presenter: RecipesPresenter

    override var exportBackupClickEvent: Observable<Unit>? = null
    override var importBackupClickEvent: Observable<Unit>? = null
    override var recipeClickEvent: Observable<Recipe>? = null
    override var recipeCreateClickEvent: Observable<Unit>? = null
    override var recipesDeleteClickEvent: Observable<ArrayList<Int>>? = null

    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionModeCallback = ActionModeCallback()
    private var isInActionMode: Boolean = false
    private lateinit var recipesAdapter: RecipesAdapter

    private val exportBackupClickSubject = PublishSubject.create<Unit>()
    private val importBackupClickSubject = PublishSubject.create<Unit>()
    private val recipeClickSubject = PublishSubject.create<Recipe>()
    private val recipeCreateClickSubject = PublishSubject.create<Unit>()
    private val recipesDeleteClickSubject = PublishSubject.create<ArrayList<Int>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        DaggerFragmentComponent.builder().appComponent(App.component).build().inject(this)
        presenter.attachView(this)
        val view: View = inflater.inflate(R.layout.fragment_recipes, container, false)
        ButterKnife.bind(this, view)
        setHasOptionsMenu(true)

        recyclerViewRecipes.layoutManager = LinearLayoutManager(container?.context)
        recipesAdapter = RecipesAdapter()
        recyclerViewRecipes.adapter = recipesAdapter

        exportBackupClickEvent = exportBackupClickSubject.hide()
        importBackupClickEvent = importBackupClickSubject.hide()
        recipeClickEvent = recipeClickSubject.hide()
        recipeCreateClickEvent = recipeCreateClickSubject.hide()
        recipesDeleteClickEvent = recipesDeleteClickSubject.hide()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.title = getString(R.string.recipes_page_title)

        buttonCreateRecipe.setOnClickListener {
            recipeCreateClickSubject.onNext(Unit)
        }

        recipesAdapter.eventsListener = RecipesAdapterListener()

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(TAG_IS_IN_ACTION_MODE,false)) {
                activity?.let {
                    actionMode = it.startActionMode(actionModeCallback)
                    recipesAdapter.setSelectedItems(savedInstanceState.getIntegerArrayList(TAG_SELECTED_ADAPTER_ITEMS) ?: ArrayList())
                    actionMode?.title = recipesAdapter.getSelectedItemsCount().toString()
                }
            }
        }

        presenter.viewIsReady()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(TAG_IS_IN_ACTION_MODE, isInActionMode)
        recipesAdapter.let {
            outState.putIntegerArrayList(TAG_SELECTED_ADAPTER_ITEMS, it.getSelectedItems())
        }
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.recipes, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.export_backup -> {
                exportBackupClickSubject.onNext(Unit)
                true
            }
            R.id.import_backup -> {
                activity?.let {
                    importBackupClickSubject.onNext(Unit)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun importBackup() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/zip"
        activity?.startActivityForResult(intent, REQUEST_CODE_IMPORT_BACKUP)
    }

    override fun requestReadStoragePermissions() {
        activity?.let {_activity ->
            ActivityCompat.requestPermissions(_activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MainActivity.REQUEST_CODE_PERMISSIONS_READ_EXTERNAL_STORAGE_ADD_BACKUP)
        }
    }

    override fun applyChanges() {
        Toast.makeText(App.context, getString(R.string.changes_applied), Toast.LENGTH_SHORT).show()
        App.appDatabase?.close()
        App.appDatabase = null
        activity?.recreate()
    }

    override fun shareFile(uri: Uri) {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/zip"
        }
        startActivity(shareIntent)
    }

    override fun showErrorNoReadStoragePermissionsGranted() {
        Toast.makeText(App.context, getString(R.string.recipes_error_permissions_storage_read), Toast.LENGTH_SHORT).show()
    }

    override fun showRecipes(recipes: ArrayList<Recipe>) {
        recipesAdapter.recipes = recipes
    }

    override fun showRecipe(recipe: Recipe) {
        recipe.id?.let { _id ->
            val bundle = Bundle()
            bundle.putInt(RecipeDetailsFragment.ARGUMENT_RECIPE_ID, _id)
            findNavController().navigate(R.id.action_recipes_fragment_to_recipe_details_fragment, bundle)
        }
    }

    override fun showCreateRecipe() {
        val action = RecipesFragmentDirections.actionRecipesFragmentToRecipeEditFragment()
        findNavController().navigate(action)
    }

    private fun onRecipeSelected(position: Int) {
        recipesAdapter.toggleItemSelection(position)
        actionMode?.apply {
            val count: Int = recipesAdapter.getSelectedItemsCount()
            if (count == 0) {
                finish()
            } else {
                title = count.toString()
                invalidate()
            }
        }
    }

    private inner class ActionModeCallback : ActionMode.Callback {
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            isInActionMode = true
            actionMode = mode
            mode.menuInflater.inflate(R.menu.recipes_action_mode, menu)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_delete_recipes -> {
                    context?.let { _context ->
                        val recipes = recipesAdapter.getSelectedRecipes()
                        val title = _context.getString(
                            R.string.recipes_delete_confirmation_title,
                            resources.getQuantityString(R.plurals.recipe, recipes.size, recipes.size)
                        )
                        val message = recipes.joinToString(separator = System.getProperty("line.separator") ?: "\n") { "- ${it.title}" }
                        AlertDialog.Builder(_context)
                            .setTitle(title)
                            .setMessage(message)
                            .setNegativeButton(R.string.no) { _, _ ->
                                mode.finish()
                            }
                            .setPositiveButton(R.string.yes) { _, _ ->
                                recipesDeleteClickSubject.onNext(recipesAdapter.getSelectedRecipeIds())
                                mode.finish()
                            }
                            .show()
                    }
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            recipesAdapter.clearSelection()
            actionMode = null
            isInActionMode = false
        }
    }

    private inner class RecipesAdapterListener : RecipesAdapter.EventsListener {
        override fun onRecipeClicked(position: Int, recipe: Recipe) {
            if (actionMode == null) {
                recipeClickSubject.onNext(recipe)
            } else {
                onRecipeSelected(position)
            }
        }

        override fun onRecipeLongClicked(position: Int, recipe: Recipe): Boolean {
            if (actionMode == null) {
                activity?.startActionMode(actionModeCallback)
            }
            onRecipeSelected(position)
            return true
        }

    }
}