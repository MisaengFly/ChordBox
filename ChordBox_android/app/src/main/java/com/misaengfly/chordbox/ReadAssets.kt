package com.misaengfly.chordbox

import android.content.res.Resources
import com.google.gson.GsonBuilder
import org.json.JSONObject

class ReadAssets {
    // assets에 있는 파일 읽기
    private lateinit var chordList: List<String>
    private lateinit var timeList: List<String>

    private var chordMap: MutableMap<Int, String> = mutableMapOf()

    fun makeChordMap(resources: Resources): Map<Int, String> {
        chordList = getChordList(resources)
        timeList = getTimeList(resources)

        for (i in chordList.indices) {
            chordMap[timeList[i].toInt()] = chordList[i]
        }

        return chordMap.toMap()
    }

    private fun getChordList(resources: Resources): List<String> {
        val assetsManger = resources.assets
        val inputStream = assetsManger.open("example.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jObject = JSONObject(jsonString)
        val chordArray = jObject.getJSONArray("chordList").toString()

        return GsonBuilder().create().fromJson(chordArray, Array<String>::class.java).toList()
    }

    private fun getTimeList(resources: Resources): List<String> {
        val assetsManger = resources.assets
        val inputStream = assetsManger.open("example.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jObject = JSONObject(jsonString)
        val timeArray = jObject.getJSONArray("timeList").toString()

        return GsonBuilder().create().fromJson(timeArray, Array<String>::class.java).toList()
    }
}

