package com.misaengfly.chordbox

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.misaengfly.chordbox.databinding.FragmentChordBinding

class ChordFragment : Fragment() {

    private lateinit var chordBinding: FragmentChordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chordBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_chord,
            container,
            false
        )

        chordBinding.musicPlayBtn.setOnClickListener {
            Intent(requireContext(), MusicService::class.java).run {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) requireActivity().startForegroundService(
                    this
                )
                else requireActivity().startService(this)
            }
        }

        chordBinding.musicFastForwardBtn.setOnClickListener {
            Intent(requireContext(), MusicService::class.java).run {
                requireActivity().stopService(this)
            }
        }

        return chordBinding.root
    }
}