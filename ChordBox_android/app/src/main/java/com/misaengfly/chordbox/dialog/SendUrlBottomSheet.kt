package com.misaengfly.chordbox.dialog

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.network.FileApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendUrlBottomSheet : BottomSheetDialogFragment() {

    private lateinit var sendBtn: Button
    private lateinit var sendEdit: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_youtube_url, container, false)

        sendBtn = view.findViewById(R.id.send_url_btn)
        sendEdit = view.findViewById(R.id.send_url_et)

        return view
    }

    companion object {
        fun newInstance(): SendUrlBottomSheet {
            return SendUrlBottomSheet()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.send_url_btn).setOnClickListener {
            var urlString = sendEdit.text
            if (urlString.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Please Input the Youtube url", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.WEB_URL.matcher(urlString).matches()) {
                Toast.makeText(requireContext(), "올바른 형식의 URL을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                FileApi.retrofitService.sendYoutubeUrl(urlString.toString()).enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        Log.d("callback success : ", response.message())
                        dismiss()
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Log.d("callback failure", t.toString())
                        dismiss()
                    }
                })
            }
        }
    }
}