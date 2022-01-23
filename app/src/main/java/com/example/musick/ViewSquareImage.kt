package com.example.musick

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView

class ViewSquareImage @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : CardView(context, attrs) {


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}