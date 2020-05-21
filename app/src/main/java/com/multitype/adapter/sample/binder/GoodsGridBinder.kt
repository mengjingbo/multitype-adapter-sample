package com.multitype.adapter.sample.binder

import androidx.recyclerview.widget.GridLayoutManager
import com.multitype.adapter.binder.MultiTypeBinder
import com.multitype.adapter.createMultiTypeAdapter
import com.multitype.adapter.invoke
import com.multitype.adapter.sample.GridLayoutDecorationDivider
import com.multitype.adapter.sample.R
import com.multitype.adapter.sample.databinding.ItemGoodsBinding
import com.multitype.adapter.sample.databinding.ItemGoodsGridContainerBinding

class GoodsGridContainerBinder(val goods: List<GoodsBinder>): MultiTypeBinder<ItemGoodsGridContainerBinding>() {

    override fun layoutId(): Int = R.layout.item_goods_grid_container

    override fun areContentsTheSame(other: Any): Boolean = other is GoodsGridContainerBinder && other.goods == goods

    override fun onBindViewHolder(binding: ItemGoodsGridContainerBinding) {
        binding.goodsRecycler.addItemDecoration(GridLayoutDecorationDivider(binding.root.context, 2, 10))
        (createMultiTypeAdapter(binding.goodsRecycler, GridLayoutManager(binding.root.context, 2))) {
            notifyAdapterChanged(goods)
        }
    }
}

class GoodsBinder(val index: Int): MultiTypeBinder<ItemGoodsBinding>() {

    override fun layoutId(): Int = R.layout.item_goods

    override fun areContentsTheSame(other: Any): Boolean = other is GoodsBinder && other.index == index
}