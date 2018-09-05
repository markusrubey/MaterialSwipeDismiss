package net.rubey.materialswipedismiss.list

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import net.rubey.materialswipedismiss.R
import net.rubey.materialswipedismiss.lib.SwipeDismissListener

class DemoListItemAdapter : ListAdapter<DemoListItem,
        RecyclerView.ViewHolder>(DemoListItemCallback()), SwipeDismissListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.demo_item_view, parent, false)
        return DemoListItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Do nothing.
    }

    override fun onItemDismiss(position: Int) {
        notifyItemRemoved(position)
    }
}