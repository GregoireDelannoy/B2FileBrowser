package com.gregoiredelannoy.b2filebrowser

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_item.view.*
import kotlin.math.ln
import kotlin.math.pow

class ListAdapter(val items: List<Node>, val clickListener: (Node) -> Unit) : RecyclerView.Adapter<ListAdapter.FileItemHolder>() {
    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: FileItemHolder, position: Int) {
        val fileItem = items[position]
        holder.bindItem(fileItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemHolder {
        val inflatedView = parent.inflate(R.layout.row_item, false)
        return FileItemHolder(inflatedView, clickListener)
    }

    class FileItemHolder(v: View, val clickListener: (Node) -> Unit) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var item: Node = Node(
            parent = null,
            children = null,
            name = "",
            path = "",
            fileDescription = null
        )

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            this.clickListener(this.item)
        }

        private fun humanReadableByteCount(bytes: Long): String {
            val unit = 1024
            if (bytes < unit) return "$bytes B"
            val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
            val pre = "kMGTPE"[exp - 1]
            return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
        }

        fun bindItem(item: Node){
            this.item = item
            view.itemName.text = item.name
            if(item.children != null) {
                view.itemImageView.setImageResource(R.drawable.ic_folder)
            } else {
                view.itemImageView.setImageResource(R.drawable.ic_file)
                item.fileDescription?.contentLength?.let {
                    view.itemDescription.text = humanReadableByteCount(it)
                }
            }
        }
    }
}