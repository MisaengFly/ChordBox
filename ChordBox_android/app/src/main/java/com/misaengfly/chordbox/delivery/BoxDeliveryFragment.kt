package com.misaengfly.chordbox.delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.misaengfly.chordbox.databinding.FragmentBoxDeliveryBinding

class BoxDeliveryFragment : Fragment() {

    private var boxDeliveryBinding: FragmentBoxDeliveryBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentBoxDeliveryBinding.inflate(inflater, container, false)
        boxDeliveryBinding = binding

        val adapter = BoxDeliveryAdapter()
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        binding.deliveryRv.adapter = adapter
        binding.deliveryRv.addItemDecoration(decoration)

        return binding.root
    }

    override fun onDestroyView() {
        boxDeliveryBinding = null
        super.onDestroyView()
    }
}