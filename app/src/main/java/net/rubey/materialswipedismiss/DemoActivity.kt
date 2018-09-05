package net.rubey.materialswipedismiss

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.END
import android.support.v7.widget.helper.ItemTouchHelper.START
import kotlinx.android.synthetic.main.demo_activity.*
import net.rubey.materialswipedismiss.lib.SwipeDismissStyle
import net.rubey.materialswipedismiss.lib.SwipeDismissTouchHelperCallback
import net.rubey.materialswipedismiss.list.DemoListItem
import net.rubey.materialswipedismiss.list.DemoListItemAdapter

class DemoActivity : AppCompatActivity() {

    private val list = DemoListItem.values().toList()

    private val listAdapter = DemoListItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo_activity)

        val swipeDismissStyle = SwipeDismissStyle(
            colorBackground = ContextCompat.getColor(this, R.color.colorBackgroundDark),
            colorInnerShadow = ContextCompat.getColor(this, R.color.colorShadow),
            colorIcon = ContextCompat.getColor(this, R.color.colorIcon),
            colorIconReveal = ContextCompat.getColor(this, R.color.colorIconReveal),
            cornerRadius = resources.getDimensionPixelSize(R.dimen.corner_radius),
            iconDrawable = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!,
            iconPadding = resources.getDimensionPixelSize(R.dimen.padding_normal)
        )
        val swipeDismissCallback = SwipeDismissTouchHelperCallback(swipeDismissStyle, listAdapter, START or END)
        ItemTouchHelper(swipeDismissCallback).attachToRecyclerView(recyclerView)

        recyclerView.adapter = listAdapter
        listAdapter.submitList(list)
    }
}
