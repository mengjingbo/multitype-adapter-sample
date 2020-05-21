package com.multitype.adapter.sample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GridLayoutDecorationDivider extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int dividerWidth;
    private int dividerWidthTop;
    private int dividerWidthBot;

    private Paint dividerPaint;

    /**
     * @param spanCount gridLayoutManager 列数
     * @param dividerWidthDp 分割块宽高,单位:dp
     */
    public GridLayoutDecorationDivider(Context context, int spanCount, int dividerWidthDp) {
        this.spanCount = spanCount;
        this.dividerPaint = new Paint();
        this.dividerPaint.setColor(Color.BLUE);
        this.dividerWidth = dpToPx(context, dividerWidthDp);
        this.dividerWidthTop = dividerWidth / 2;
        this.dividerWidthBot = dividerWidth - dividerWidthTop;
    }

    @Override
    public void getItemOffsets(Rect outRect, View child, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, child, parent, state);
        int pos = parent.getChildAdapterPosition(child);
        int column = (pos) % spanCount;// 计算这个child 处于第几列
        outRect.top = dividerWidthTop;
        outRect.bottom = dividerWidthBot;
        outRect.left = (column * dividerWidth / spanCount);
        outRect.right = dividerWidth - (column + 1) * dividerWidth / spanCount;
    }

    private int dpToPx(Context context, float value) {
        if (value <= 0) return 0;
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
