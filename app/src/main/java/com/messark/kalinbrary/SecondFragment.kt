package com.messark.kalinbrary

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.messark.kalinbrary.R
import com.messark.kalinbrary.data.StoryRepository
import com.messark.kalinbrary.databinding.FragmentSecondBinding
import com.messark.kalinbrary.ui.storydetail.StoryContentAdapter

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: SecondFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val story = StoryRepository.getStories().find { it.title == args.storyTitle }

        story?.let {
            val storyContentAdapter = StoryContentAdapter(it.content)
            val recyclerView: RecyclerView = view.findViewById(R.id.story_content_list)
            recyclerView.adapter = storyContentAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_second, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_story -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Story")
            .setMessage("Are you sure you want to delete this story?")
            .setPositiveButton("Delete") { _, _ ->
                deleteStory()
                findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteStory() {
        val story = StoryRepository.getStories().find { it.title == args.storyTitle }
        story?.let {
            StoryRepository.deleteStory(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}