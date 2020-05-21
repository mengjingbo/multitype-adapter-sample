package com.multitype.adapter.sample.binder

import com.multitype.adapter.binder.MultiTypeBinder
import com.multitype.adapter.sample.R
import com.multitype.adapter.sample.databinding.ItemHorizontalTextBinding

class HorizontalItemBinder(val index: String): MultiTypeBinder<ItemHorizontalTextBinding>() {

    override fun layoutId(): Int = R.layout.item_horizontal_text

    override fun areContentsTheSame(other: Any): Boolean = other is HorizontalItemBinder
}