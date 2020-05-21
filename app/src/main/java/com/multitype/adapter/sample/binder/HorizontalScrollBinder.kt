package com.multitype.adapter.sample.binder

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.multitype.adapter.binder.MultiTypeBinder
import com.multitype.adapter.createMultiTypeAdapter
import com.multitype.adapter.invoke
import com.multitype.adapter.sample.R
import com.multitype.adapter.sample.databinding.ItemHorizontalScrollContainerBinding

class HorizontalScrollBinder(val data: List<HorizontalItemBinder>): MultiTypeBinder<ItemHorizontalScrollContainerBinding>() {

    override fun layoutId(): Int = R.layout.item_horizontal_scroll_container

    override fun areContentsTheSame(other: Any): Boolean = other is HorizontalScrollBinder && other.data == data

    override fun onBindViewHolder(binding: ItemHorizontalScrollContainerBinding) {
        (createMultiTypeAdapter(binding.multiTypeScrollRecycler, LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false))) {
           notifyAdapterChanged(data)
        }
    }
}