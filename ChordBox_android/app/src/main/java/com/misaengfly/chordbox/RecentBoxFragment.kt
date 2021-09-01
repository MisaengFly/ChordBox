package com.misaengfly.chordbox

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecentBoxFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_recent_box, container, false)

        rootView.findViewById<FloatingActionButton>(R.id.new_box_FAB).setOnClickListener {
            startActivity(Intent(requireContext(), MusicRecordActivity::class.java))
        }

        return rootView
    }
}