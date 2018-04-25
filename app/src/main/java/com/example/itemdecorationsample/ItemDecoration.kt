package com.example.itemdecorationsample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import kotlin.math.roundToInt

class ItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    // This could be a 9-patch card drawable or similar for example
    private val groupBackground: Drawable = ContextCompat.getDrawable(context, R.drawable.bg_group)!!

    private val groupMargin: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics)
        .roundToInt()

    // This is needed to draw corners outside of the screen when appropriate
    private val backgroundCornerRadius: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.resources.displayMetrics)
        .roundToInt()

    private val tmpChildList: MutableList<View> = mutableListOf()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val viewHolder = parent.getChildViewHolder(view)

        outRect.set(groupMargin, 0, groupMargin, 0)
        when (viewHolder) {
            is Adapter.ViewHolder.GroupHeader -> outRect.top = groupMargin
            is Adapter.ViewHolder.GroupFooter -> outRect.bottom = groupMargin
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // We sort all the views by their y position, so they're in increasing order
        // There are no guarantees on the order of the children within the RV. This simplifies our algorithm.

        for (i in 0 until parent.childCount) {
            tmpChildList.add(parent.getChildAt(i))
        }
        tmpChildList.sortBy { it.y }

        var hasDrawnBackground = false
        var header: Adapter.ViewHolder.GroupHeader? = null
        for (child in tmpChildList) {
            val vh = parent.getChildViewHolder(child)

            if (vh is Adapter.ViewHolder.GroupHeader) {
                header = vh
            } else if (vh is Adapter.ViewHolder.GroupFooter) {
                // We found a footer. Let's draw a group starting from the header to the footer.
                // If we have no footer, it was off-screen vertically, so let's draw all the way up to 0
                // (correcting for the border radius).
                val topPosition = header?.itemView?.y?.toInt()
                    ?: 0 - backgroundCornerRadius
                groupBackground.setBounds(vh.itemView.left, topPosition, vh.itemView.right, (vh.itemView.y + vh.itemView.height).toInt())
                groupBackground.draw(c)
                header = null
                hasDrawnBackground = true
            }
        }

        header?.let {
            // If header was not null here, it means we didn't find the footer (off-screen), so let's draw to
            // the parent's height (correcting for the corner radius again).
            groupBackground.setBounds(it.itemView.left, it.itemView.y.toInt(), it.itemView.right, parent.height + backgroundCornerRadius)
            groupBackground.draw(c)
            hasDrawnBackground = true
        }

        if (!hasDrawnBackground) {
            // No header or footer was visible. If not empty, it means we only saw GroupItems, so draw a full size bg
            if (tmpChildList.size > 0) {
                val view = tmpChildList.first()
                groupBackground.setBounds(view.left, -backgroundCornerRadius, view.right, parent.height + backgroundCornerRadius)
                groupBackground.draw(c)
            }
        }

        tmpChildList.clear()
    }
}