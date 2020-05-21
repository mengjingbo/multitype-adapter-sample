package com.multitype.adapter

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.multitype.adapter.binder.MultiTypeBinder
import com.multitype.adapter.callback.DiffItemCallback
import com.multitype.adapter.holder.MultiTypeViewHolder

/**
 * date          : 2019/5/31
 * author        : 秦川·小将
 * description   :
 */
class MultiTypeAdapter: RecyclerView.Adapter<MultiTypeViewHolder>() {

    // 使用后台线程通过差异性计算来更新列表
    private val mAsyncListChange by lazy { AsyncListDiffer(this, DiffItemCallback<MultiTypeBinder<*>>()) }

    // 存储 MultiTypeBinder 和 MultiTypeViewHolder Type
    private var mHashCodeViewType = LinkedHashMap<Int, MultiTypeBinder<*>>()

    init {
        setHasStableIds(true)
    }

    fun notifyAdapterChanged(binders: List<MultiTypeBinder<*>>) {
        mHashCodeViewType = LinkedHashMap()
        binders.forEach {
            mHashCodeViewType[it.hashCode()] = it
        }
        mAsyncListChange.submitList(mHashCodeViewType.map { it.value })
    }

    fun notifyAdapterChanged(binder: MultiTypeBinder<*>) {
        mHashCodeViewType = LinkedHashMap()
        mHashCodeViewType[binder.hashCode()] = binder
        mAsyncListChange.submitList(mHashCodeViewType.map { it.value })
    }

    override fun getItemViewType(position: Int): Int {
        val mItemBinder = mAsyncListChange.currentList[position]
        val mHasCode = mItemBinder.hashCode()
        // 如果Map中不存在当前Binder的hasCode，则向Map中添加当前类型的Binder
        if (!mHashCodeViewType.containsKey(mHasCode)) {
            mHashCodeViewType[mHasCode] = mItemBinder
        }
        return mHasCode
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = mAsyncListChange.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiTypeViewHolder {
        try {
            return MultiTypeViewHolder(parent.inflateDataBinding(mHashCodeViewType[viewType]?.layoutId()!!))
        }catch (e: Exception){
            throw NullPointerException("不存在${mHashCodeViewType[viewType]}类型的ViewHolder!")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: MultiTypeViewHolder, position: Int) {
        holder.onBindViewHolder(mAsyncListChange.currentList[position] as MultiTypeBinder<ViewDataBinding>)
    }
}