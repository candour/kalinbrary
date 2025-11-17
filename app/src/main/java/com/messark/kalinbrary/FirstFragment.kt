package com.messark.kalinbrary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.messark.kalinbrary.data.StoryRepository
import com.messark.kalinbrary.databinding.FragmentFirstBinding
import com.messark.kalinbrary.ui.storylist.StoryListAdapter

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stories = StoryRepository.getStories()
        val storyListAdapter = StoryListAdapter(stories) {
            val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(it.title)
            findNavController().navigate(action)
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.story_list)
        recyclerView.adapter = storyListAdapter

        view.findViewById<FloatingActionButton>(R.id.fab_add_story).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_AddStoryFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        // This is a simple way to refresh the list when we return to it.
        // A better approach would use LiveData or a similar observable pattern.
        (view?.findViewById<RecyclerView>(R.id.story_list)?.adapter)?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}