package com.multitype.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * date          : 2019/5/31
 * author        : 秦川·小将
 * description   :
 */

/**
 * 创建一个MultiTypeGeneralAdapter
 */
fun createMultiTypeAdapter(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager): MultiTypeAdapter {
    recyclerView.layoutManager = layoutManager
    val mMultiTypeAdapter = MultiTypeAdapter()
    recyclerView.adapter = mMultiTypeAdapter
    // 处理RecyclerView的触发回调
    recyclerView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(v: View?) {
            mMultiTypeAdapter.onDetachedFromRecyclerView(recyclerView)
        }
        override fun onViewAttachedToWindow(v: View?) { }
    })
    return mMultiTypeAdapter
}

/**
 * MultiTypeGeneralAdapter扩展函数，重载MultiTypeGeneralAdapter类，使用invoke操作符调用MultiTypeGeneralAdapter内部函数。
 */
inline operator fun MultiTypeAdapter.invoke(block: MultiTypeAdapter.() -> Unit): MultiTypeAdapter {
    this.block()
    return this
}

/**
 * Layout converter ViewDataBinding
 */
fun <T : ViewDataBinding> ViewGroup.inflateDataBinding(layoutId: Int): T = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, this, false)!!

