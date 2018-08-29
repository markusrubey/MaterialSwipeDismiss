package net.rubey.materialswipedismiss

import android.graphics.drawable.Drawable

data class SwipeDismissStyle(
    val colorBackground: Int,
    val colorInnerShadow: Int,
    val colorIcon: Int,
    val colorIconReveal: Int,
    val cornerRadius: Int,
    val iconDrawable: Drawable,
    val iconPadding: Int
)