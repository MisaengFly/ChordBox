package com.misaengfly.chordbox.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.BottomSheetSelectBinding
import com.misaengfly.chordbox.record.RecordActivity

class SelectBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSelectBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): SelectBottomSheet {
            return SelectBottomSheet()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.selectRecordIb.setOnClickListener {
            startActivity(Intent(requireContext(), RecordActivity::class.java))
            dialog?.dismiss()
        }

        binding.selectUrlUploadIb.setOnClickListener {
            dialog?.dismiss()
            SendUrlBottomSheet.newInstance().show(requireActivity().supportFragmentManager, "SendUrlBottomSheet")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}