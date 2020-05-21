package com.multitype.adapter.sample.binder

import androidx.recyclerview.widget.GridLayoutManager
import com.multitype.adapter.binder.MultiTypeBinder
import com.multitype.adapter.createMultiTypeAdapter
import com.multitype.adapter.invoke
import com.multitype.adapter.sample.GridLayoutDecorationDivider
import com.multitype.adapter.sample.R
import com.multitype.adapter.sample.databinding.ItemRecommendContainerBinding
import com.multitype.adapter.sample.databinding.ItemRecommendGoodsBinding

class RecommendContainerBinder(val goods: List<RecommendGoodsBinder>): MultiTypeBinder<ItemRecommendContainerBinding>() {

    override fun layoutId(): Int = R.layout.item_recommend_container

    override fun areContentsTheSame(other: Any): Boolean = other is RecommendContainerBinder && other.goods == goods

    override fun onBindViewHolder(binding: ItemRecommendContainerBinding) {
        binding.recommendGoodsRecycler.addItemDecoration(GridLayoutDecorationDivider(binding.root.context, 4, 10))
        (createMultiTypeAdapter(binding.recommendGoodsRecycler, GridLayoutManager(binding.root.context, 4))) {
            notifyAdapterChanged(goods)
        }
    }
}

class RecommendGoodsBinder: MultiTypeBinder<ItemRecommendGoodsBinding>() {

    override fun layoutId(): Int = R.layout.item_recommend_goods

    override fun areContentsTheSame(other: Any): Boolean = other is RecommendGoodsBinder
}