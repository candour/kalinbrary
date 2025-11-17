package com.messark.kalinbrary.ui.storydetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.messark.kalinbrary.R
import com.messark.kalinbrary.data.ImageElement
import com.messark.kalinbrary.data.StoryElement
import com.messark.kalinbrary.data.TextElement
import com.squareup.picasso.Picasso
import java.io.File

private const val VIEW_TYPE_TEXT = 1
private const val VIEW_TYPE_IMAGE = 2

class StoryContentAdapter(private val storyElements: List<StoryElement>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (storyElements[position]) {
            is TextElement -> VIEW_TYPE_TEXT
            is ImageElement -> VIEW_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.story_text_item, parent, false)
                TextViewHolder(view)
            }
            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.story_image_item, parent, false)
                ImageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextViewHolder -> {
                val element = storyElements[position] as TextElement
                holder.bind(element)
            }
            is ImageViewHolder -> {
                val element = storyElements[position] as ImageElement
                holder.bind(element)
            }
        }
    }

    override fun getItemCount(): Int = storyElements.size

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.story_text)

        fun bind(element: TextElement) {
            textView.text = element.text
        }
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.story_image)

        fun bind(element: ImageElement) {
            if (element.imageUrl.startsWith("/")) {
                Picasso.get().load(File(element.imageUrl)).into(imageView)
            } else {
                Picasso.get().load(element.imageUrl).into(imageView)
            }
        }
    }
}