package com.misaengfly.chordbox.chord

import android.content.Intent
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.misaengfly.chordbox.MusicService
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.FragmentChordBinding
import kotlin.math.sqrt

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

    private fun getFile(id: Int) {
        val mid = 2
        val filePath = "${requireContext().filesDir?.absolutePath}/musicrecord${mid}.m4a"

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    /**
     * Buffer size
     **/
    private fun initUI() = with(chordBinding) {
        playerVisualizer.apply {
            ampNormalizer = { sqrt(it.toFloat()).toInt() }
            onStartSeeking = {

            }
            onFinishedSeeking = { time, isPlayingBefore ->

            }
            onAnimateToPositionFinished = { time, isPlaying ->

            }
        }

        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource()
        val mediaFormat = MediaFormat()


        lifecycleScope.launchWhenCreated {
            val amps = player.loadAmps()
            playerVisualizer.setWaveForm(amps, player.tickDuration)
        }
    }
}