package com.misaengfly.chordbox.player

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.ReadAssets
import com.misaengfly.chordbox.databinding.FragmentUrlChordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

    private var timerTask: Timer? = null
    private val seekTime = 5000

    // TODO( 해당 Item 클릭 시 빠르게 이동하도록 수정 필요 )
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
            if (isPlaying) {
                isPlaying = false
                stopPlaying()
            } else {
                isPlaying = true
                startPlaying()
            }
        }

        binding.urlMusicForwardBtn.setOnClickListener {
            if (player != null) {
                val curPosition = player!!.currentPosition
                val millisecond = curPosition + seekTime
                player!!.seekTo(millisecond)
                adapter.selectedPosition = (((millisecond + 400) / 1000))
            }
        }

        binding.urlMusicBackwardBtn.setOnClickListener {
            val curPosition = player!!.currentPosition
            var millisecond = if (curPosition - seekTime < 0) 0 else curPosition - seekTime
            player!!.seekTo(millisecond)
            adapter.selectedPosition = (((millisecond + 400) / 1000))
        }

    }

    private fun startPlaying() {
        if (player != null) {
            stopPlaying()
        }
        player = MediaPlayer.create(requireContext(), R.raw.butter)
        timerTask = timer(period = 500) {
            val second = (((player!!.currentPosition + 400) / 1000))

            Log.i("Timer : ", second.toString())

            requireActivity().runOnUiThread {
                adapter.selectedPosition = second
                binding.urlMusicChordContainer.scrollToPosition(second)
            }
        }

        player!!.start()
        binding.urlMusicPlayBtn.setImageResource(R.drawable.ic_stop)
    }

    private fun stopPlaying() {
        player.let {
            adapter.selectedPosition = 0
            player?.stop()
            player?.release()
        }
        player = null
        timerTask?.cancel()
        binding.urlMusicPlayBtn.setImageResource(R.drawable.ic_play)
    }

    private fun pausePlaying() {
        player.let {
            it?.pause()
        }
        binding.urlMusicPlayBtn.setImageResource(R.drawable.ic_play)
    }

    override fun onDestroyView() {
        stopPlaying()
        super.onDestroyView()
        urlChordBinding = null
    }
}