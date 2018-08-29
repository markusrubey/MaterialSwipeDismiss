package net.rubey.materialswipedismiss.list

import android.support.v7.util.DiffUtil

class DemoListItemCallback : DiffUtil.ItemCallback<DemoListItem>() {

    override fun areItemsTheSame(
        oldItem: DemoListItem,
        newItem: DemoListItem
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: DemoListItem,
        newItem: DemoListItem
    ): Boolean {
        return oldItem == newItem
    }
}
