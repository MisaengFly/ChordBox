package com.misaengfly.chordbox.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.FragmentChordBinding
import com.misaengfly.chordbox.record.SEEK_OVER_AMOUNT
import com.misaengfly.chordbox.record.convertLongToDateTime
import org.json.JSONObject
import java.io.File
import kotlin.math.sqrt

class ChordFragment : Fragment() {

    private var chordBinding: FragmentChordBinding? = null
    private lateinit var player: AudioPlayer

    private lateinit var filePath: String

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

        chordList = getChordList()
        timeList = getTimeList()

        return binding.root
    }

    // assets에 있는 파일 읽기
    private lateinit var chordList: List<String>
    private lateinit var timeList: List<String>

    private fun getChordList(): List<String> {
        val assetsManger = resources.assets
        val inputStream = assetsManger.open("example.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jObject = JSONObject(jsonString)
        val chordArray = jObject.getJSONArray("chordList").toString()

        return GsonBuilder().create().fromJson(chordArray, Array<String>::class.java).toList()
    }

    private fun getTimeList(): List<String> {
        val assetsManger = resources.assets
        val inputStream = assetsManger.open("example.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jObject = JSONObject(jsonString)
        val timeArray = jObject.getJSONArray("timeList").toString()

        return GsonBuilder().create().fromJson(timeArray, Array<String>::class.java).toList()
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
            musicPlayBtn.setImageResource(R.drawable.ic_stop)
        }
        musicForwardBtn.setOnClickListener { playerVisualizer.seekOver(SEEK_OVER_AMOUNT) }
        musicBackwardBtn.setOnClickListener { playerVisualizer.seekOver(-SEEK_OVER_AMOUNT) }

        lifecycleScope.launchWhenCreated {
            val amps = player.loadAmps()
            playerVisualizer.setWaveForm(amps, player.tickDuration, chordList)
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