package com.multitype.adapter.binder

import android.view.View
import androidx.viewbinding.BuildConfig
import com.multitype.adapter.callback.OnViewClickListener

/**
 * date          : 2019/5/31
 * author        : 秦川·小将
 * description   :
 */
open class ClickBinder: OnViewClickListener {

    protected open var mOnClickListener: ((view: View, any: Any?) -> Unit)? = null

    protected open var mOnLongClickListener: ((view: View, any: Any?) -> Unit)? = null

    /**
     * 设置View点击事件
     */
    open fun setOnClickListener(listener: (view: View, any: Any?) -> Unit): ClickBinder {
        this.mOnClickListener = listener
        return this
    }

    /**
     * 设置View长按点击事件
     */
    open fun setOnLongClickListener(listener: (view: View, any: Any?) -> Unit): ClickBinder {
        this.mOnLongClickListener = listener
        return this
    }

    /**
     * 触发View点击事件时回调，携带参数
     */
    override fun onClick(view: View) {
        onClick(view, this)
    }

    override fun onClick(view: View, any: Any?) {
        if (mOnClickListener != null) {
            mOnClickListener?.invoke(view, any)
        } else {
            if (BuildConfig.DEBUG) throw NullPointerException("OnClick事件未绑定!")
        }
    }

    /**
     * 触发View长按事件时回调，携带参数
     */
    override fun onLongClick(view: View) {
        onLongClick(view, this)
    }

    override fun onLongClick(view: View, any: Any?){
        if (mOnLongClickListener != null) {
            mOnLongClickListener?.invoke(view, any)
        } else {
            throw NullPointerException("OnLongClick事件未绑定!")
        }
    }
}
