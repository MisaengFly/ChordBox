package com.misaengfly.chordbox.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.FragmentUrlChordBinding
import java.io.File
import java.util.*
import kotlin.concurrent.timer

class UrlChordFragment : Fragment() {
    private var urlChordBinding: FragmentUrlChordBinding? = null
    private val binding get() = urlChordBinding!!

    private lateinit var url: String
    private lateinit var prefToken: String
    private val viewModel: UrlChordViewModel by lazy {
        val viewModelFactory =
            UrlChordViewModel.Factory(requireActivity().application, url)
        ViewModelProvider(this, viewModelFactory).get(UrlChordViewModel::class.java)
    }

    private lateinit var adapter: UrlChordAdapter

    private var player: MediaPlayer? = null
    private var isPlaying = false
    private var isPausing = false
    private var isRePlaying = false

    private var timerTask: Timer? = null
    private val seekTime = 5000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        urlChordBinding = FragmentUrlChordBinding.inflate(LayoutInflater.from(context))

        val bundle = arguments
        url = bundle?.getString("Url").toString()
        val pref = requireActivity().getSharedPreferences("token", Context.MODE_PRIVATE)
        prefToken = pref.getString("token", "")!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UrlChordAdapter()

        binding.urlMusicNameTv.isSelected = true

        viewModel.urlItem.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.findUrlItem(url, prefToken)

                Log.d("log", it.chordMap.size.toString())

                if (it.urlName.isBlank()) {
                    binding.urlMusicNameTv.text = it.url
                } else {
                    binding.urlMusicNameTv.text = it.urlName.substring(0, it.urlName.length - 4)
                }
                binding.urlMusicDateTv.text = it.lastModified
                adapter.data = it.chordMap
            }
        }
        binding.urlMusicChordContainer.adapter = adapter

        binding.urlMusicPlayBtn.setOnClickListener {
            isPlaying = true
            isPausing = false
            isRePlaying = false
            startPlaying()
        }

        binding.urlMusicStopBtn.setOnClickListener {
            isPlaying = false
            isPausing = false
            isRePlaying = false
            stopPlaying()
        }

        binding.urlMusicPauseBtn.setOnClickListener {
            if (isPlaying && !isPausing) {
                // 실행 중
                isPlaying = false
                isPausing = true
                isRePlaying = false
                pausePlaying()
            } else {
                // 멈춰짐
                isPlaying = true
                isPausing = false
                rePlaying()
            }
        }

        binding.urlMusicForwardBtn.setOnClickListener {
            if (player != null) {
                val curPosition = player!!.currentPosition
                val millisecond = curPosition + seekTime
                player!!.seekTo(millisecond)
                adapter.moveForward(50)
            }
        }

        binding.urlMusicBackwardBtn.setOnClickListener {
            val curPosition = player!!.currentPosition
            val millisecond = if (curPosition - seekTime < 0) 0 else curPosition - seekTime
            player!!.seekTo(millisecond)
            adapter.moveBackward(50)
        }

    }

    private fun startPlaying() {
        binding.urlMusicNameTv.isSelected = false

        if (player != null) {
            stopPlaying()
            player = null
        }

        var fileUrl = ""
        var absolutePath = ""
        viewModel.urlItem.value?.let {
            fileUrl = "${requireContext().getExternalFilesDir(null)}/${it.absolutePath}"
            absolutePath = it.absolutePath
        }

        timerTask?.cancel()
        timerTask = null

        if (absolutePath.isNotBlank() && File(fileUrl).exists()) {
            player = MediaPlayer()
            player?.apply {
                setDataSource(requireContext(), Uri.parse(fileUrl))
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener {
                    timerTask = timer(period = 500) {
//                        val second = (((player!!.currentPosition + 400) / 1000))
                        val second = (((player!!.currentPosition) / 500))
//                        Log.d("Timer : ", second.toString())

                        requireActivity().runOnUiThread {
                            adapter.selectedPosition = (second * 5)
                            binding.urlMusicChordContainer.scrollToPosition(adapter.selectedPosition)
                        }
                    }
                    updateUI()
                    start()
                }
                prepare()
            }
        } else {
            Toast.makeText(requireContext(), "Music can't be executed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        if (isPlaying && !isPausing) {
            if (isRePlaying) {
                binding.urlMusicPauseBtn.setImageResource(R.drawable.ic_pause_outline_60)
            }
            // play
            binding.urlMusicPlayBtn.visibility = GONE
            binding.urlMusicPauseBtn.visibility = VISIBLE
            binding.urlMusicStopBtn.visibility = VISIBLE
        } else if (!isPlaying && !isPausing) {
            // stop
            binding.urlMusicPlayBtn.visibility = VISIBLE
            binding.urlMusicPauseBtn.visibility = GONE
            binding.urlMusicStopBtn.visibility = GONE
        } else {
            // pause
            binding.urlMusicPauseBtn.setImageResource(R.drawable.ic_play)
        }
    }

    private fun stopPlaying() {
        binding.urlMusicNameTv.isSelected = true

        player?.let {
            adapter.selectedPosition = 0
            it.stop()
            it.release()
        }
        timerTask?.cancel()
        timerTask = null
        player = null
        updateUI()
    }

    private fun pausePlaying() {
        binding.urlMusicNameTv.isSelected = false
        player?.pause()
        updateUI()
    }
    private fun rePlaying() {
        binding.urlMusicNameTv.isSelected = false
        isRePlaying = true
        player?.start()
        updateUI()
    }

    override fun onDestroyView() {
        stopPlaying()
        super.onDestroyView()
        urlChordBinding = null
    }
}