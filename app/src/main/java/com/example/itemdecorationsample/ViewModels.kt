package com.example.itemdecorationsample

data class Group(
    val groupId: String,
    val header: String,
    val items: List<String>
)

sealed class ListItem : DiffUtilComparable<ListItem> {
    data class Header(
        val groupId: String,
        val header: String
    ) : ListItem() {
        override fun isSameAs(other: ListItem): Boolean = other is Header && other.groupId == groupId

        override fun hasSameContents(other: ListItem): Boolean = other == this
    }

    data class Footer(
        val groupId: String
    ) : ListItem() {
        override fun isSameAs(other: ListItem): Boolean = other is Footer && other.groupId == groupId

        override fun hasSameContents(other: ListItem): Boolean = true
    }

    data class GroupItem(
        val groupId: String,
        val item: String
    ) : ListItem() {
        override fun isSameAs(other: ListItem): Boolean = other == this

        override fun hasSameContents(other: ListItem): Boolean = true
    }
}

interface DiffUtilComparable<T : DiffUtilComparable<T>> {
    fun isSameAs(other: T): Boolean
    fun hasSameContents(other: T): Boolean
}