package com.multitype.adapter.sample.binder

import com.multitype.adapter.binder.MultiTypeBinder
import com.multitype.adapter.sample.R
import com.multitype.adapter.sample.databinding.ItemTopBannerBinding

class TopBannerBinder: MultiTypeBinder<ItemTopBannerBinding>() {

    override fun layoutId(): Int = R.layout.item_top_banner

    override fun areContentsTheSame(other: Any): Boolean = other is TopBannerBinder
}