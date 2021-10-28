package com.misaengfly.chordbox.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.network.ApiService
import com.misaengfly.chordbox.network.FileApi
import com.misaengfly.chordbox.network.FileResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class StopDialog : DialogFragment() {
    private lateinit var positiveButton: TextView
    private lateinit var negativeButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_stop, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        positiveButton = view.findViewById(R.id.yes_btn)
        negativeButton = view.findViewById(R.id.no_btn)

        val filePath = arguments?.getString("Path")
        val file = File(filePath)

        positiveButton.setOnClickListener {
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body = MultipartBody.Part.createFormData("audiofile", file.name, requestFile)

            FileApi.retrofitService.sendAudioFile(body).enqueue(object : Callback<FileResponse>{
                override fun onResponse(
                    call: Call<FileResponse>,
                    response: Response<FileResponse>
                ) {
                    Log.d("callback success : ", response.message())
                }

                override fun onFailure(call: Call<FileResponse>, t: Throwable) {
                    Log.d("callback failure", t.toString())
                }
            })
            dismiss()
        }
        negativeButton.setOnClickListener {
            // 파일 삭제 하기
            file.delete()
            dismiss()
        }

        return view
    }
}