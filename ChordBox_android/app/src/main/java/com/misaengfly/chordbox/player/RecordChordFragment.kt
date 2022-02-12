package com.misaengfly.chordbox.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.FragmentChordBinding
import com.misaengfly.chordbox.record.SEEK_OVER_AMOUNT
import com.misaengfly.chordbox.record.convertLongToDateTime
import java.io.File
import kotlin.math.sqrt

class RecordChordFragment : Fragment() {
    private var chordBinding: FragmentChordBinding? = null
    private lateinit var player: AudioPlayer

    private lateinit var filePath: String

    private val viewModel: RecordChordViewModel by lazy {
        val viewModelFactory =
            RecordChordViewModel.Factory(requireActivity().application)
        ViewModelProvider(this, viewModelFactory).get(RecordChordViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentChordBinding.inflate(inflater, container, false)
        chordBinding = binding

        val bundle = arguments
        filePath = bundle?.getString("Path").toString()

        binding.musicNameTv.text = File(filePath).name
        binding.musicDateTv.text = File(filePath).lastModified().convertLongToDateTime()

        viewModel.findRecordItem(filePath)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        listenOnPlayerStates()
        initUI()
    }

    override fun onStop() {
        player.release()
        super.onStop()
    }

    private fun initUI() = with(chordBinding) {
        this!!.playerVisualizer.apply {
            ampNormalizer = { sqrt(it.toFloat()).toInt() }
            onStartSeeking = {
                player.pause()
            }
            //onSeeking = { binding.timelineTextView.text = it.formatAsTime() }
            onFinishedSeeking = { time, isPlayingBefore ->
                player.seekTo(time)
                if (isPlayingBefore) {
                    player.resume()
                }
            }
            onAnimateToPositionFinished = { time, isPlaying ->
                updateTime(time, isPlaying)
                player.seekTo(time)
            }
        }
        musicPlayBtn.setOnClickListener {
            player.togglePlay()
            // 땜방
            if (player.isPlaying())
                musicPlayBtn.setImageResource(R.drawable.ic_pause)
            else
                musicPlayBtn.setImageResource(R.drawable.ic_play)

        }
        musicForwardBtn.setOnClickListener { playerVisualizer.seekOver(SEEK_OVER_AMOUNT) }
        musicBackwardBtn.setOnClickListener { playerVisualizer.seekOver(-SEEK_OVER_AMOUNT) }

        lifecycleScope.launchWhenCreated {
            val amps = player.loadAmps()
            val chordMap = viewModel.chordMap
            playerVisualizer.setWaveForm(amps, player.tickDuration, chordMap)
        }
    }

    private fun listenOnPlayerStates() = with(chordBinding) {
        player = AudioPlayer.getInstance(requireContext())
            .init(filePath).apply {
                onProgress = { time, isPlaying ->
                    updateTime(time, isPlaying)
                }
            }
    }

    private fun updateTime(time: Long, isPlaying: Boolean) = with(chordBinding) {
        //timelineTextView.text = time.formatAsTime()
        this?.playerVisualizer?.updateTime(time, isPlaying)
    }

    override fun onDestroyView() {
        chordBinding = null
        super.onDestroyView()
    }
}