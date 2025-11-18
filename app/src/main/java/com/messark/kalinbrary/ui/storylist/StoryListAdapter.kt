package com.messark.kalinbrary.ui.storylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.messark.kalinbrary.R
import com.messark.kalinbrary.data.Story
import com.squareup.picasso.Picasso
import java.io.File

class StoryListAdapter(
    private val stories: List<Story>,
    private val onClick: (Story) -> Unit
) : RecyclerView.Adapter<StoryListAdapter.StoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.story_list_item, parent, false)
        return StoryViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

    class StoryViewHolder(itemView: View, val onClick: (Story) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.story_title)
        private val coverImageView: ImageView = itemView.findViewById(R.id.story_cover_image)
        private var currentStory: Story? = null

        init {
            itemView.setOnClickListener {
                currentStory?.let {
                    onClick(it)
                }
            }
        }

        fun bind(story: Story) {
            currentStory = story
            titleTextView.text = story.title
            if (!story.coverImageUrl.isNullOrEmpty()) {
                if (story.coverImageUrl.startsWith("/")) {
                    Picasso.get().load(File(story.coverImageUrl)).into(coverImageView)
                } else {
                    Picasso.get().load(story.coverImageUrl).into(coverImageView)
                }
            }
        }
    }
}