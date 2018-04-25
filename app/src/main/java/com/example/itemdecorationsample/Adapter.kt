package com.example.itemdecorationsample

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

private const val VIEW_TYPE_HEADER = 1
private const val VIEW_TYPE_FOOTER = 2
private const val VIEW_TYPE_GROUP_ITEM = 3
private val PAYLOAD_EXPANDED_CHANGED = "expanded changed"

class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
    private val data: List<Group> = listOf(
        Group(
            groupId = "1",
            header = "Group 1",
            items = listOf(
                "Item 1",
                "Item 2"
            )
        ),
        Group(
            groupId = "2",
            header = "Group 2",
            items = listOf(
                "Item 3",
                "Item 4",
                "Item 5",
                "Item 6",
                "Item 7",
                "Item 8",
                "Item 9"
            )
        ),
        Group(
            groupId = "3",
            header = "Group 3",
            items = listOf(
                "Item 3",
                "Item 4",
                "Item 5",
                "Item 6",
                "Item 7",
                "Item 8",
                "Item 9",
                "Item 4",
                "Item 5",
                "Item 6",
                "Item 7",
                "Item 8",
                "Item 5",
                "Item 6",
                "Item 7",
                "Item 5",
                "Item 6",
                "Item 7",
                "Item 5",
                "Item 6",
                "Item 7"
            )
        ),
        Group(
            groupId = "4",
            header = "Group 4",
            items = listOf(
                "Item 10",
                "Item 11",
                "Item 12",
                "Item 13",
                "Item 14",
                "Item 15",
                "Item 16"
            )
        )
    )

    private val expandedGroups: MutableSet<String> = mutableSetOf()

    private var items: List<ListItem> = createListItems()

    private fun createListItems(): List<ListItem> {
        return data.flatMap { group ->
            listOf(ListItem.Header(groupId = group.groupId, header = group.header)) +
                if (group.groupId in expandedGroups) {
                    group.items.map { ListItem.GroupItem(groupId = group.groupId, item = it) }
                } else {
                    emptyList()
                } + ListItem.Footer(groupId = group.groupId)
        }
    }

    private fun dispatchUpdates() {
        val oldList = items
        val newList = createListItems()
        items = newList

        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size

            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldList[oldItemPosition].isSameAs(newList[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldList[oldItemPosition].hasSameContents(newList[newItemPosition])
        })
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            VIEW_TYPE_HEADER -> ViewHolder.GroupHeader(parent)
            VIEW_TYPE_FOOTER -> ViewHolder.GroupFooter(parent)
                .also { vh ->
                    vh.expandButton.setOnClickListener {
                        if (vh.adapterPosition == -1) {
                            return@setOnClickListener
                        }
                        if (!expandedGroups.remove(vh.groupId)) {
                            expandedGroups.add(vh.groupId)
                        }
                        // Re-bind button item
                        notifyItemChanged(vh.adapterPosition, PAYLOAD_EXPANDED_CHANGED)
                        dispatchUpdates()
                    }
                }
            VIEW_TYPE_GROUP_ITEM -> ViewHolder.GroupItem(parent)
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.GroupHeader -> holder.bind(items[position] as ListItem.Header)
            is ViewHolder.GroupFooter -> {
                val item = items[position] as ListItem.Footer
                holder.bind(item, item.groupId in expandedGroups)
            }
            is ViewHolder.GroupItem -> holder.bind(items[position] as ListItem.GroupItem)
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is ListItem.Header -> VIEW_TYPE_HEADER
            is ListItem.Footer -> VIEW_TYPE_FOOTER
            is ListItem.GroupItem -> VIEW_TYPE_GROUP_ITEM
        }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class GroupHeader(parent: ViewGroup) : ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_header, parent, false)) {
            private val header: TextView = itemView as TextView

            fun bind(item: ListItem.Header) {
                header.text = item.header
            }
        }

        class GroupFooter(parent: ViewGroup) : ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_footer, parent, false)) {
            val expandButton: Button = itemView.findViewById(R.id.expand_button)
            lateinit var groupId: String

            fun bind(item: ListItem.Footer, expanded: Boolean) {
                groupId = item.groupId
                expandButton.text = if (expanded) "Collapse" else "Expand"
            }
        }

        class GroupItem(parent: ViewGroup) : ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_group_item, parent, false)) {
            private val textView: TextView = itemView as TextView

            fun bind(item: ListItem.GroupItem) {
                textView.text = item.item
            }
        }
    }
}