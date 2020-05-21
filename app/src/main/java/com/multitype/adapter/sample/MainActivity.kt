package com.multitype.adapter.sample

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.multitype.adapter.MultiTypeAdapter
import com.multitype.adapter.binder.MultiTypeBinder
import com.multitype.adapter.callback.OnViewClickListener
import com.multitype.adapter.createMultiTypeAdapter
import com.multitype.adapter.sample.binder.*
import com.multitype.adapter.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnViewClickListener {

    private lateinit var mAdapter: MultiTypeAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.setVariable(BR.data, this)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        mAdapter = createMultiTypeAdapter(binding.multiTypeRecycler, LinearLayoutManager(this))
        setRecyclerViewContent()
    }

    private fun setRecyclerViewContent() {
        mAdapter.notifyAdapterChanged(mutableListOf<MultiTypeBinder<*>>().apply {
            add(TopBannerBinder().apply {
                setOnClickListener(this@MainActivity::onClick)
            })
            add(CategoryContainerBinder(listOf("男装", "女装", "鞋靴", "内衣内饰", "箱包", "美妆护肤", "洗护", "腕表珠宝", "手机", "数码").map {
                CategoryItemBinder(it).apply {
                    setOnClickListener(this@MainActivity::onClick)
                }
            }))
            add(RecommendContainerBinder((1..8).map { RecommendGoodsBinder().apply {
                setOnClickListener(this@MainActivity::onClick)
            } }))
            add(HorizontalScrollBinder((0..11).map { HorizontalItemBinder("$it").apply {
                setOnClickListener(this@MainActivity::onClick)
            } }))
            add(GoodsGridContainerBinder((1..20).map { GoodsBinder(it).apply {
                setOnClickListener(this@MainActivity::onClick)
            } }))
        })
    }

    override fun onClick(view: View, any: Any?) {
        when(view.id) {
            R.id.top_banner -> {
                any as TopBannerBinder
                toast(view, "点击Banner")
            }
            R.id.category_tab -> {
                any as CategoryItemBinder
                toast(view,"点击分类+${any.title}")
            }
            R.id.recommend_goods -> {
                any as RecommendGoodsBinder
                toast(view, "点击精选会场Item")
            }
            R.id.theme_index -> {
                any as HorizontalItemBinder
                toast(view, "点击主题会场${any.index}")
            }
            R.id.goods_container -> {
                any as GoodsBinder
                toast(view, "点击商品${any.index}")
            }
        }
    }
}

fun toast(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
}
