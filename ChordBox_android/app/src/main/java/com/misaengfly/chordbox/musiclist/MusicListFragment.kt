package com.misaengfly.chordbox.musiclist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.FragmentMusicListBinding
import com.misaengfly.chordbox.player.ChordFragment
import com.misaengfly.chordbox.record.RecordActivity

class MusicListFragment : Fragment() {

    private lateinit var musicListBinding: FragmentMusicListBinding
    private lateinit var androidViewModel: MusicListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        musicListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_music_list,
            container,
            false
        )
        musicListBinding.lifecycleOwner = this

        androidViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(MusicListViewModel::class.java)

        return musicListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MusicAdapter(MusicAdapter.MusicItemListener {
            replaceFragment(ChordFragment())
        })
        androidViewModel.musicList.observe(viewLifecycleOwner, {
            adapter.data = it
        })
        musicListBinding.musicListRV.adapter = adapter

        musicListBinding.newMusicFAB.setOnClickListener {
            startActivity(Intent(requireContext(), RecordActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        androidViewModel.updateFiles()
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}