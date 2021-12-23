package com.misaengfly.chordbox.dialog

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.misaengfly.chordbox.databinding.BottomSheetYoutubeUrlBinding
import com.misaengfly.chordbox.network.FileApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendUrlBottomSheet : BottomSheetDialogFragment() {

    private var youtubeUrlBinding: BottomSheetYoutubeUrlBinding? = null

    private val viewModel: SendUrlBottomViewModel by lazy {
        val viewModelFactory =
            SendUrlBottomViewModel.Factory(requireActivity().application)
        ViewModelProvider(this, viewModelFactory).get(SendUrlBottomViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetYoutubeUrlBinding.inflate(inflater, container, false)
        youtubeUrlBinding = binding

        return binding.root
    }

    companion object {
        fun newInstance(): SendUrlBottomSheet {
            return SendUrlBottomSheet()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        youtubeUrlBinding!!.sendUrlBtn.setOnClickListener {
            var urlString = youtubeUrlBinding!!.sendUrlEt.text

            // TODO (이미 있는 url인 경우 처리해줘야 함)
            if (urlString.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Please Input the Youtube url", Toast.LENGTH_SHORT)
                    .show()
            } else if (!Patterns.WEB_URL.matcher(urlString).matches()) {
                Toast.makeText(requireContext(), "Please Input correct url", Toast.LENGTH_SHORT)
                    .show()
            } else { // 올바른 url format 입력
                // 1. DB에 저장
                viewModel.insertUrlToDB(urlString.toString())

                // 2. 서버에 저장
                FileApi.retrofitService.sendYoutubeUrl(urlString.toString())
                    .enqueue(object : Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            Log.d("Send URL cb success : ", response.message())
                            dismiss()
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Log.d("Send URL cb failure", t.toString())
                            dismiss()
                        }
                    })
            }
        }
    }

    override fun onDestroyView() {
        youtubeUrlBinding = null
        super.onDestroyView()
    }
}