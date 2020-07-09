package com.multitype.adapter.callback

import android.view.View

/**
 * date          : 2019/5/31
 * author        : 秦川·小将
 * description   :
 */
interface OnViewClickListener {

    // 不需要额外参数事件时，默认转发给带额外参数事件
    fun onClick(view: View) {
        onClick(view, null)
    }

    fun onClick(view: View, any: Any?) {

    }

    fun onLongClick(view: View) {
        onLongClick(view, null)
    }

    fun onLongClick(view: View, any: Any?) {

    }
}
