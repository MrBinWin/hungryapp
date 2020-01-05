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

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.R
import com.mrbinwin.hungryapp.adapters.ProductsAdapter
import com.mrbinwin.hungryapp.data.models.Product
import com.mrbinwin.hungryapp.di.components.DaggerFragmentComponent
import com.mrbinwin.hungryapp.forms.EditProductForm
import com.mrbinwin.hungryapp.forms.validators.RequiredValidator
import com.mrbinwin.hungryapp.mvp.contracts.ShoppingListViewContract
import com.mrbinwin.hungryapp.mvp.presenters.ShoppingListPresenter
import com.mrbinwin.hungryapp.utils.AppKeyboard
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


class ShoppingListFragment: BaseFragment(), ShoppingListViewContract {

    companion object {
        const val EDIT_PRODUCT_ITEM_POSITION = 0
        const val DELETE_PRODUCT_ITEM_POSITION = 1
    }

    @BindView(R.id.shopping_list_recycler) lateinit var recyclerViewProducts: RecyclerView
    @BindView(R.id.edit_text_shopping_list_new_product) lateinit var editTextNewProduct: EditText

    @Inject lateinit var presenter: ShoppingListPresenter

    override var deleteAllProductsClickEvent: Observable<Unit>? = null
    override var productCreateClickEvent: Observable<EditProductForm>? = null
    override var productDeleteClickEvent: Observable<Int>? = null
    override var productMovedEvent: Observable<List<Product>>? = null
    override var productUpdateClickEvent: Observable<Product>? = null
    override var sortProductsClickEvent: Observable<Unit>? = null

    private lateinit var productContextMenuItems: Map<Int, String>
    private var editProductForm: EditProductForm = EditProductForm()
    private lateinit var productsAdapter: ProductsAdapter
    private val touchHelper = ItemTouchHelper(ItemTouchCallback())

    private val deleteAllProductsClickSubject = PublishSubject.create<Unit>()
    private val productCreateClickSubject = PublishSubject.create<EditProductForm>()
    private val productDeleteClickSubject = PublishSubject.create<Int>()
    private val productMovedSubject = PublishSubject.create<List<Product>>()
    private val productUpdateClickSubject = PublishSubject.create<Product>()
    private val sortProductsClickSubject = PublishSubject.create<Unit>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        DaggerFragmentComponent.builder().appComponent(App.component).build().inject(this)
        presenter.attachView(this)
        val view: View = inflater.inflate(R.layout.fragment_shopping_list, container, false)
        ButterKnife.bind(this, view)
        setHasOptionsMenu(true)

        productContextMenuItems = mapOf(
            EDIT_PRODUCT_ITEM_POSITION to getString(R.string.shopping_list_dialog_edit_product),
            DELETE_PRODUCT_ITEM_POSITION to getString(R.string.shopping_list_dialog_delete_product)
        )

        recyclerViewProducts.layoutManager = LinearLayoutManager(container?.context)
        productsAdapter = ProductsAdapter()
        recyclerViewProducts.adapter = productsAdapter

        deleteAllProductsClickEvent = deleteAllProductsClickSubject.hide()
        productCreateClickEvent = productCreateClickSubject.hide()
        productDeleteClickEvent = productDeleteClickSubject.hide()
        productMovedEvent = productMovedSubject.hide()
        productUpdateClickEvent = productUpdateClickSubject.hide()
        sortProductsClickEvent = sortProductsClickSubject.hide()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.title = getString(R.string.shopping_list_page_title)
        productsAdapter.eventsListener = ProductsAdapterListener()
        touchHelper.attachToRecyclerView(recyclerViewProducts)

        editProductForm.apply {
            setTitleField(editTextNewProduct, listOf(RequiredValidator()))
        }
        
        editTextNewProduct.setOnEditorActionListener { view, actionId, _ ->
            val productName: String = view.text.trim().toString()
            if (actionId == EditorInfo.IME_ACTION_DONE && !TextUtils.isEmpty(productName)) {
                productCreateClickSubject.onNext(editProductForm)
                activity?.apply {
                    AppKeyboard.hide(this)
                }
                return@setOnEditorActionListener true
            }
            false
        }
        
        presenter.viewIsReady()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.shopping_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_shopping_list_sort_alphabetically -> {
                sortProductsClickSubject.onNext(Unit)
                true
            }
            R.id.menu_shopping_list_clear -> {
                context?.let { _context ->
                    AlertDialog.Builder(_context)
                        .setTitle(R.string.shopping_list_menu_clear_confirmation)
                        .setNegativeButton(R.string.no, null)
                        .setPositiveButton(R.string.yes) { _, _ -> deleteAllProductsClickSubject.onNext(Unit) }
                        .show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun clearProductForm() {
        editTextNewProduct.setText("")
    }

    override fun showProducts(products: ArrayList<Product>) {
        productsAdapter.products = products
    }

    private fun showEditProductDialog(product: Product, positionInRecyclerView: Int) {
        context?.let { _context ->
            MaterialDialog(_context).show {
                title(R.string.shopping_list_dialog_edit_product_title)
                input(hintRes=R.string.shopping_list_new_product, prefill=product.title) { _, text ->
                    product.title = text.toString()
                    productUpdateClickSubject.onNext(product)
                    productsAdapter.products[positionInRecyclerView] = product
                    productsAdapter.notifyItemChanged(positionInRecyclerView)
                }
                negativeButton(R.string.cancel, null)
                positiveButton(R.string.ok, null)

            }
        }
    }

    inner class ItemTouchCallback : ItemTouchHelper.Callback() {
        private var dragFrom = -1
        private var dragTo = -1

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            if(dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                onMoveFinished()
            }
            dragFrom = -1
            dragTo = -1
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = 0
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }

        override fun isLongPressDragEnabled(): Boolean {
            return false
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            if (dragFrom == -1) {
                dragFrom =  viewHolder.adapterPosition
            }
            dragTo = target.adapterPosition
            productsAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        private fun onMoveFinished() {
            productMovedSubject.onNext(productsAdapter.products)
        }
    }

    inner class ProductsAdapterListener : ProductsAdapter.EventsListener {
        override fun onProductChecked(position: Int, product: Product, checked: Boolean) {
            product.is_bought = checked
            productsAdapter.products[position] = product
            productsAdapter.notifyItemChanged(position)
            productUpdateClickSubject.onNext(product)
        }

        override fun onProductLongClicked(position: Int, product: Product): Boolean {
            context?.let { _context ->
                AlertDialog.Builder(_context)
                    .setItems(productContextMenuItems.values.toTypedArray()) {_, menuItemPosition ->
                        when (menuItemPosition) {
                            EDIT_PRODUCT_ITEM_POSITION -> product.id?.let { showEditProductDialog(product, position) }
                            DELETE_PRODUCT_ITEM_POSITION -> product.id?.let {
                                productsAdapter.products.removeAt(position)
                                productsAdapter.notifyItemRemoved(position)
                                productDeleteClickSubject.onNext(it)
                            }
                        }
                    }
                    .show()
            }
            return true
        }

        override fun onProductStartDrag(viewHolder: ProductsAdapter.ProductViewHolder) {
            touchHelper.startDrag(viewHolder)
        }
    }

}