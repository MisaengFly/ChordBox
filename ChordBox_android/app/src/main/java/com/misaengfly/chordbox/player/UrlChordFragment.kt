package com.misaengfly.chordbox.player

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
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import kotlin.concurrent.timer

class UrlChordFragment : Fragment() {
    private var urlChordBinding: FragmentUrlChordBinding? = null
    private val binding get() = urlChordBinding!!

    private val viewModel: UrlChordViewModel by lazy {
        val viewModelFactory =
            UrlChordViewModel.Factory(requireActivity().application)
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
        val url = bundle?.getString("Url").toString()

        runBlocking {
            viewModel.findUrlItem(url)
        }

        return binding.root
    }

    // TODO (노티 안 눌러도 들어왔을 때 파일 다운 x 시 서버 확인 )
    // TODO (화면 드로우와 파일 실행이 맞지 않음 )
    // TODO (version 28 이하부터 seekto 문제 있음 )
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UrlChordAdapter()

        viewModel.urlItem.observe(viewLifecycleOwner) {
            it?.let {
                Log.d("log", it.chordMap.size.toString())
                binding.urlMusicNameTv.text = it.url
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
                adapter.moveForward()
//                adapter.selectedPosition = (((millisecond + 400) / 1000))
            }
        }

        binding.urlMusicBackwardBtn.setOnClickListener {
            val curPosition = player!!.currentPosition
            var millisecond = if (curPosition - seekTime < 0) 0 else curPosition - seekTime
            player!!.seekTo(millisecond)
            adapter.moveBackward()
//            adapter.selectedPosition = (((millisecond + 400) / 1000))
        }

    }

    private fun startPlaying() {
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

        timerTask?.let {
            it.cancel()
        }
        timerTask = null

        if (absolutePath.isNotBlank() && File(fileUrl).exists()) {
            player = MediaPlayer()
            player?.apply {
                setDataSource(requireContext(), Uri.parse(fileUrl))
                setOnPreparedListener {
                    timerTask = timer(period = 500) {
//                        val second = (((player!!.currentPosition + 400) / 1000))

                        requireActivity().runOnUiThread {
                            adapter.moveForward()
                            binding.urlMusicChordContainer.scrollToPosition(adapter.selectedPosition)
//                            adapter.selectedPosition = second
//                            binding.urlMusicChordContainer.scrollToPosition(second)
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
        player?.let {
            it.pause()
        }
        updateUI()
    }

    private fun rePlaying() {
        isRePlaying = true
        player?.let {
            it.start()
        }
        updateUI()
    }

    override fun onDestroyView() {
        stopPlaying()
        super.onDestroyView()
        urlChordBinding = null
    }
}