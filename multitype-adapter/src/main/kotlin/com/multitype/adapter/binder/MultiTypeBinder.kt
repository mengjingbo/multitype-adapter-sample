package com.multitype.adapter.binder

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.multitype.adapter.BR
import com.multitype.adapter.R

/**
 * date          : 2019/5/31
 * author        : 秦川·小将
 * description   :
 */
abstract class MultiTypeBinder<V : ViewDataBinding> : ClickBinder() {

    /**
     * BR.data
     */
    protected open val variableId = BR.data

    /**
     * 被绑定的ViewDataBinding
     */
    open var binding: V? = null

    /**
     * 给绑定的View设置tag
     */
    private var bindingViewVersion = (0L until Long.MAX_VALUE).random()

    /**
     * 返回LayoutId，供Adapter使用
     */
    @LayoutRes
    abstract fun layoutId(): Int

    /**
     * 两次更新的Binder内容是否相同
     */
    abstract fun areContentsTheSame(other: Any): Boolean

    /**
     * 绑定ViewDataBinding
     */
    fun bindViewDataBinding(binding: V) {
        // 如果此次绑定与已绑定的一至，则不做绑定
        if (this.binding === binding && binding.root.getTag(R.id.bindingVersion) == bindingViewVersion) return
        binding.root.setTag(R.id.bindingVersion, ++bindingViewVersion)
        onUnBindViewHolder()
        this.binding = binding
        binding.setVariable(variableId, this)
        // 给 binding 绑定生命周期，方便观察LiveData的值，进而更新UI。如果不绑定，LiveData的值改变时，UI不会更新
        if (binding.root.context is LifecycleOwner) {
            binding.lifecycleOwner = binding.root.context as LifecycleOwner
        } else {
            binding.lifecycleOwner = AlwaysActiveLifecycleOwner()
        }
        onBindViewHolder(binding)
        // 及时更新绑定数据的View
        binding.executePendingBindings()
    }

    /**
     * 解绑ViewDataBinding
     */
    fun unbindDataBinding() {
        if (this.binding != null) {
            onUnBindViewHolder()
            this.binding = null
        }
    }

    /**
     * 绑定后对View的一些操作，如：赋值，修改属性
     */
    protected open fun onBindViewHolder(binding: V) {

    }

    /**
     * 解绑操作
     */
    protected open fun onUnBindViewHolder() {

    }

    /**
     * 为 Binder 绑定生命周期，在 {@link Lifecycle.Event#ON_RESUME} 时响应
     */
    internal class AlwaysActiveLifecycleOwner : LifecycleOwner {

        override fun getLifecycle(): Lifecycle = object : LifecycleRegistry(this) {
            init {
                handleLifecycleEvent(Event.ON_RESUME)
            }
        }
    }
}