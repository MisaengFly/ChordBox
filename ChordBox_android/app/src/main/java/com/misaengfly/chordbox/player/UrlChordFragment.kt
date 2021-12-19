package com.misaengfly.chordbox.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.misaengfly.chordbox.ReadAssets
import com.misaengfly.chordbox.databinding.FragmentUrlChordBinding

class UrlChordFragment : Fragment() {
    private var urlChordBinding: FragmentUrlChordBinding? = null
    private val binding get() = urlChordBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        urlChordBinding = FragmentUrlChordBinding.inflate(LayoutInflater.from(context))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var adapter = UrlChordAdapter()
        adapter.data = ReadAssets().makeChordMap(resources)
        binding.urlMusicChordContainer.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        urlChordBinding = null
    }
}