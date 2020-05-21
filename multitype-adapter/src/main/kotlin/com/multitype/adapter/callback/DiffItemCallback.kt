package com.multitype.adapter.callback

import androidx.recyclerview.widget.DiffUtil
import com.multitype.adapter.binder.MultiTypeBinder

/**
 * date          : 2019/5/31
 * author        : 秦川·小将
 * description   :
 */
class DiffItemCallback<T : MultiTypeBinder<*>> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.layoutId() == newItem.layoutId()
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.hashCode() == newItem.hashCode() && oldItem.areContentsTheSame(newItem)
    }

    override fun getChangePayload(oldItem: T, newItem: T): Any? {
        return super.getChangePayload(oldItem, newItem)
    }
}