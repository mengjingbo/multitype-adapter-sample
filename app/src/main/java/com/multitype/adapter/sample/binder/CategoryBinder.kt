package com.multitype.adapter.sample.binder

import androidx.recyclerview.widget.GridLayoutManager
import com.multitype.adapter.binder.MultiTypeBinder
import com.multitype.adapter.createMultiTypeAdapter
import com.multitype.adapter.invoke
import com.multitype.adapter.sample.R
import com.multitype.adapter.sample.databinding.ItemCategoryChildBinding
import com.multitype.adapter.sample.databinding.ItemCategoryContainerBinding

class CategoryContainerBinder(val category: List<CategoryItemBinder>): MultiTypeBinder<ItemCategoryContainerBinding>() {

    override fun layoutId(): Int = R.layout.item_category_container

    override fun areContentsTheSame(other: Any): Boolean = other is CategoryContainerBinder && other.category == category

    override fun onBindViewHolder(binding: ItemCategoryContainerBinding) {
        (createMultiTypeAdapter(binding.categoryRecycler, GridLayoutManager(binding.root.context, 5))) {
            notifyAdapterChanged(category)
        }
    }
}

class CategoryItemBinder(val title: String): MultiTypeBinder<ItemCategoryChildBinding>() {

    override fun layoutId(): Int = R.layout.item_category_child

    override fun areContentsTheSame(other: Any): Boolean = other is CategoryItemBinder && other.title == title
}