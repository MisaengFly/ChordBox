package com.misaengfly.chordbox.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.misaengfly.chordbox.R

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

        positiveButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            dismiss()
            ChooseDialog().show(transaction, "ChooseDialog")
        }
        negativeButton.setOnClickListener {
            dismiss()
        }

        return view
    }
}