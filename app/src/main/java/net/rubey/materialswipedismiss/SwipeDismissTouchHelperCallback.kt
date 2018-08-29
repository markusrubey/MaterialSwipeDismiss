package net.rubey.materialswipedismiss

import android.animation.ArgbEvaluator
import android.content.res.Resources
import android.graphics.*
import android.graphics.Shader.TileMode.CLAMP
import android.support.v4.view.animation.PathInterpolatorCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper

class SwipeDismissTouchHelperCallback(
    private val style: SwipeDismissStyle,
    private val listener: SwipeDismissListener,
    private val swipeDirections: Int
) : ItemTouchHelper.SimpleCallback(0, swipeDirections) {

    private val dp = Resources.getSystem().displayMetrics.density

    private val shadowPaint = Paint()

    private val shadowTopHeight = 4 * dp / 1.5f
    private val shadowBottomHeight = shadowTopHeight / 2f
    private val shadowWidth = shadowTopHeight * 3f / 4f

    private val shadowShaderTop = LinearGradient(0f, 0f, 0f, shadowTopHeight, style.colorInnerShadow, 0, CLAMP)
    private val shadowShaderBottom = LinearGradient(0f, 0f, 0f, shadowBottomHeight, 0, style.colorInnerShadow, CLAMP)
    private val shadowShaderLeft = LinearGradient(0f, 0f, shadowWidth, 0f, style.colorInnerShadow, 0, CLAMP)
    private val shadowShaderRight = LinearGradient(0f, 0f, -shadowWidth, 0f, style.colorInnerShadow, 0, CLAMP)

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = this@SwipeDismissTouchHelperCallback.style.colorIcon
    }

    private var iconColorFilter: Int = 0

    private val argbInterpolator = PathInterpolatorCompat.create(0.125f, 0f, 0f, 25f)
    private val argbEvaluator = ArgbEvaluator()

    private val clipPath = Path()

    private enum class Direction {
        START_END,
        END_START
    }

    override fun onMove(rv: RecyclerView, source: ViewHolder, target: ViewHolder) = false

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        listener.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun getSwipeDirs(rv: RecyclerView, viewHolder: ViewHolder): Int {
        return makeMovementFlags(0, swipeDirections)
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float) = defaultValue * 5f

    override fun isLongPressDragEnabled() = false

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (dX == 0f) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        val left = viewHolder.itemView.left.toFloat()
        val top = viewHolder.itemView.top.toFloat()
        val right = viewHolder.itemView.right.toFloat()
        val bottom = viewHolder.itemView.bottom.toFloat()

        val width = right - left
        val height = bottom - top

        val saveCount = c.save()

        val direction = when {
            dX > 0 -> Direction.START_END
            else -> Direction.END_START
        }

        val clipLeft = when (direction) {
            Direction.START_END -> left
            Direction.END_START -> Math.max(right + dX - style.cornerRadius, left)
        }

        val clipRight = when (direction) {
            Direction.START_END -> Math.min(left + dX + style.cornerRadius, right)
            Direction.END_START -> right
        }

        clipPath.reset()
        clipPath.addRoundRect(
            RectF(clipLeft, top, clipRight, bottom),
            style.cornerRadius.toFloat(),
            style.cornerRadius.toFloat(),
            Path.Direction.CW
        )
        c.clipPath(clipPath)
        c.drawColor(style.colorBackground)

        val progress = Math.abs(dX) / width

        val swipeThreshold = getSwipeThreshold(viewHolder)
        val thirdThreshold = swipeThreshold / 3f

        var iconColor = style.colorIcon
        var iconOpacity = 1f
        var iconScale = 1f

        var circleRadius = 0f

        when (progress) {
            in 0f..thirdThreshold -> {
                iconOpacity = progress / thirdThreshold
            }
            else -> {
                iconColor = calculateIconColor(progress, swipeThreshold)
                iconScale = calculateIconScale(progress, swipeThreshold, thirdThreshold)

                circleRadius = (progress - swipeThreshold) * width * CIRCLE_ACCELERATION
            }
        }

        val icon = style.iconDrawable
        val iconCenterX = when (direction) {
            Direction.START_END -> left + style.iconPadding + icon.intrinsicWidth / 2f
            Direction.END_START -> right - style.iconPadding - icon.intrinsicWidth / 2f
        }
        val iconCenterY = top + height / 2f
        val iconSizeHalf = icon.intrinsicWidth * iconScale / 2f
        icon.setBounds(
            (iconCenterX - iconSizeHalf).toInt(),
            (iconCenterY - iconSizeHalf).toInt(),
            (iconCenterX + iconSizeHalf).toInt(),
            (iconCenterY + iconSizeHalf).toInt()
        )
        icon.alpha = (iconOpacity * 255f).toInt()
        if (iconColor != iconColorFilter) {
            icon.colorFilter = PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
            iconColorFilter = iconColor
        }
        if (circleRadius > 0f) {
            c.drawCircle(iconCenterX, iconCenterY, circleRadius, circlePaint)
        }
        icon.draw(c)

        shadowShaderTop.setTranslation(y = top)
        shadowPaint.shader = shadowShaderTop
        c.drawRect(left, top, right, top + shadowTopHeight, shadowPaint)

        shadowShaderBottom.setTranslation(y = bottom - shadowBottomHeight)
        shadowPaint.shader = shadowShaderBottom
        c.drawRect(left, bottom - shadowBottomHeight, right, bottom, shadowPaint)

        when (direction) {
            Direction.START_END -> {
                shadowShaderLeft.setTranslation(x = left)
                shadowPaint.shader = shadowShaderLeft
                c.drawRect(left, top, left + shadowWidth, bottom, shadowPaint)
            }
            Direction.END_START -> {
                shadowShaderRight.setTranslation(x = right)
                shadowPaint.shader = shadowShaderRight
                c.drawRect(right - shadowWidth, top, right, bottom, shadowPaint)
            }
        }

        c.restoreToCount(saveCount)
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private companion object {
        const val CIRCLE_ACCELERATION = 3f
    }

    private fun calculateIconScale(progress: Float, swipeThreshold: Float, thirdThreshold: Float): Float {
        val iconPopThreshold = swipeThreshold + 0.125f
        val iconPopFinishedThreshold = iconPopThreshold + 0.125f

        return when (progress) {
            in thirdThreshold..swipeThreshold -> {
                1f - (((progress - thirdThreshold) / (swipeThreshold - thirdThreshold)) * 0.1f)
            }
            in swipeThreshold..iconPopThreshold -> {
                0.9f + ((progress - swipeThreshold) / (iconPopThreshold - swipeThreshold)) * 0.3f
            }
            in iconPopThreshold..iconPopFinishedThreshold -> {
                1.2f - (((progress - iconPopThreshold) / (iconPopFinishedThreshold - iconPopThreshold)) * 0.2f)
            }
            else -> 1f
        }
    }

    private fun calculateIconColor(progress: Float, swipeThreshold: Float): Int {
        val argbProgress = Math.min(1f, argbInterpolator.getInterpolation((progress - swipeThreshold)))
        return argbEvaluator.evaluate(argbProgress, style.colorIcon, style.colorIconReveal) as Int
    }

    private fun Shader.setTranslation(x: Float = 0f, y: Float = 0f) {
        getLocalMatrix(matrix)
        matrix.setTranslate(x, y)
        setLocalMatrix(matrix)
    }

    private val matrix: Matrix by lazy(LazyThreadSafetyMode.NONE) {
        Matrix()
    }
}