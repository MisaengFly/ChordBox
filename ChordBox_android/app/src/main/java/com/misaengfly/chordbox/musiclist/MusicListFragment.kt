package com.misaengfly.chordbox.musiclist

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.FragmentMusicListBinding
import com.misaengfly.chordbox.dialog.SelectBottomSheet
import com.misaengfly.chordbox.player.ChordFragment
import java.lang.reflect.Method

class MusicListFragment : Fragment() {

    private lateinit var musicListBinding: FragmentMusicListBinding
    private lateinit var androidViewModel: MusicListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        musicListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_music_list,
            container,
            false
        )
        musicListBinding.lifecycleOwner = this

        androidViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(MusicListViewModel::class.java)

        return musicListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MusicAdapter(
            MusicAdapter.MusicItemListener {
                replaceFragment(ChordFragment(), it)
            },
            MusicAdapter.DeleteItemListener { view, item ->
                // Long 클릭 시 해당 Item 지우기
                showPopupMenu(view, item)
                true
            }
        )

        // 녹음 파일 개수 변화 감지
        androidViewModel.chordList.observe(viewLifecycleOwner) {
            androidViewModel.updateMusicList()
        }

        // Youtube 파일 개수 변화 감지
        androidViewModel.urlList.observe(viewLifecycleOwner) {
            androidViewModel.updateMusicList()
        }

        androidViewModel.musicList.observe(viewLifecycleOwner, {
            adapter.data = it
        })
        musicListBinding.musicListRV.adapter = adapter

        musicListBinding.newMusicFAB.setOnClickListener {
            SelectBottomSheet.newInstance()
                .show(requireActivity().supportFragmentManager, "SelectBottomSheet")
        }
    }

    /**
     * pop up menu 보여주는 method
     *
     * [ pop up menu ] 구성
     * 1. 삭제 버튼
     **/
    private fun showPopupMenu(view: View?, item: MusicItem) {
        val contextThemeWrapper =
            ContextThemeWrapper(requireContext(), R.style.PopupMenuStyle)
        val popupBase =
            (view as MaterialCardView).findViewById<TextView>(R.id.pupup_container)
        val popupMenu = PopupMenu(contextThemeWrapper, popupBase)
        popupMenu.menuInflater.inflate(R.menu.pop_up_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { m ->
            if (m.itemId == R.id.list_action_delete) {
                androidViewModel.removeFile(item.absolutePath)
            }
            false
        }

        // Icon 보여주기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        } else {
            try {
                val fields = popupMenu.javaClass.declaredFields
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field[popupMenu]
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcon: Method = classPopupHelper.getMethod(
                            "setForceShowIcon",
                            Boolean::class.javaPrimitiveType
                        )
                        setForceIcon.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        popupMenu.show()
    }

    override fun onStart() {
        super.onStart()
        androidViewModel.updateFiles()
    }

    private fun replaceFragment(fragment: Fragment, path: String) {
        val bundle = Bundle()
        bundle.putString("Path", path)
        fragment.arguments = bundle

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}