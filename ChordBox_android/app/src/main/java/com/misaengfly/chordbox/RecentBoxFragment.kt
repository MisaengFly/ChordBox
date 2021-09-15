package com.misaengfly.chordbox

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.misaengfly.chordbox.databinding.FragmentRecentBoxBinding

class RecentBoxFragment : Fragment() {

    private lateinit var recentBoxBinding: FragmentRecentBoxBinding
    private lateinit var androidViewModel: RecentBoxViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recentBoxBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_recent_box,
            container,
            false
        )
        recentBoxBinding.lifecycleOwner = this

        androidViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(RecentBoxViewModel::class.java)

        return recentBoxBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = RecentBoxAdapter(RecentBoxAdapter.MusicItemListener {
            replaceFragment(ChordFragment())
        })
        androidViewModel.musicList.observe(viewLifecycleOwner, {
            adapter.data = it
        })
        recentBoxBinding.musicRV.adapter = adapter

        recentBoxBinding.newBoxFAB.setOnClickListener {
            startActivity(Intent(requireContext(), MusicRecordActivity::class.java))
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