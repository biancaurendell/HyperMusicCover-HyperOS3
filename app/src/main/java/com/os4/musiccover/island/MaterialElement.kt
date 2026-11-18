// SPDX-License-Identifier: Apache-2.0
package com.os4.musiccover.island
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RectF
import android.view.ViewOutlineProvider
import android.widget.ImageView
import kotlin.math.min

/**
 * The element a pill or a disc wears the card's material on, keeping the outline it is given.
 *
 * The glass draws its rim along the element's own outline. NotificationUtil.applyElementViewBlend
 * starts with setRoundRect, which puts the notification card's corner on whatever view it is
 * handed - 24dp, 72px here - and the full-screen AOD's dim runs it on these views every frame
 * (MiniPlayerRuntime.dimView), with nothing putting ours back when the wake settles at the
 * card's own look. A 72px corner on a view whose frame is cut round at 81px has its rim cut off
 * toward the diagonals: thinner there, and the cut is the frame's outline clip, jagged (#7, #21).
 * So the outline is [shape], whoever sets it.
 */
internal class MaterialElement(context: Context) : ImageView(context) {
    /** The only outline this element takes; null lets any be set, as a plain ImageView. */
    var shape: ViewOutlineProvider? = null
        set(value) {
            field = value
            if (value != null) {
                super.setOutlineProvider(value)
                invalidateOutline()
            }
        }

    private val rimOutline = Outline()
    private val rimRect = RectF()
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0x34FFFFFF
    }

    init {
        setWillNotDraw(false)
    }

    override fun setOutlineProvider(provider: ViewOutlineProvider?) {
        super.setOutlineProvider(shape ?: provider)
    }

    override fun invalidateOutline() {
        super.invalidateOutline()
        if (IslandMaterial.os3GlassRimActive()) invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!IslandMaterial.os3GlassRimActive()) return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 2f || h <= 2f) return
        val stroke = (resources.displayMetrics.density * 0.85f).coerceAtLeast(1.5f)
        val half = stroke * 0.5f
        val r = runCatching {
            outlineProvider?.getOutline(this, rimOutline)
            rimOutline.radius
        }.getOrDefault(-1f).let { if (it > 0f) it else min(w, h) * 0.5f }
        rimPaint.strokeWidth = stroke
        rimRect.set(half, half, w - half, h - half)
        val rr = (r - half).coerceAtLeast(0f)
        canvas.drawRoundRect(rimRect, rr, rr, rimPaint)
    }
}
