package com.misaengfly.chordbox

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.misaengfly.chordbox.databinding.FragmentRecentBoxBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecentBoxFragment : Fragment() {

    private lateinit var recentBoxBinding: FragmentRecentBoxBinding

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

        val adapter = RecentBoxAdapter()
        adapter.data = getFileList()
        recentBoxBinding.musicRV.adapter = adapter

        recentBoxBinding.newBoxFAB.setOnClickListener {
            startActivity(Intent(requireContext(), MusicRecordActivity::class.java))
        }

        return recentBoxBinding.root
    }

    private fun getFileList(): ArrayList<MusicItem> {
        // 파일 얻어오기
        val fileList = requireActivity().filesDir.listFiles()
        val musicItemList = arrayListOf<MusicItem>()

        for (file in fileList) {
            musicItemList.add(
                MusicItem(
                    file.absolutePath,
                    file.name,
                    getFileDuration(file),
                    convertLongToDateTime(file.lastModified())
                )
            )
        }

        return musicItemList
    }

    private fun getFileDuration(file: File): String {
        var durationString = ""

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMilliSec = time!!.toLong()
        val duration = timeInMilliSec / 1000
        val hours = duration / 3600
        val minutes = (duration - hours * 3600) / 60
        val seconds = duration - (hours * 3600 + minutes * 60)

        if (hours > 0) {
            durationString += hours.toInt()
            durationString += ":"
        }
        durationString += "${minutes.toInt()}:${seconds.toInt()}"

        return durationString
    }

    private fun convertLongToDateTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }
}