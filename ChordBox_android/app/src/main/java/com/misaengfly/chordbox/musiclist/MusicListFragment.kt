package com.misaengfly.chordbox.musiclist

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import com.misaengfly.chordbox.MusicType
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.FragmentMusicListBinding
import com.misaengfly.chordbox.dialog.SelectBottomSheet
import com.misaengfly.chordbox.player.RecordChordFragment
import com.misaengfly.chordbox.player.UrlChordFragment
import java.lang.reflect.Method

class MusicListFragment : Fragment() {

    private lateinit var musicListBinding: FragmentMusicListBinding
    private lateinit var androidViewModel: MusicListViewModel

    private var mLastClickTime = 0L

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

        mLastClickTime = 0L

        return musicListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MusicAdapter(
            MusicAdapter.MusicItemListener {
                when (it.type) {
                    MusicType.RECORD ->
                        replaceChordFragment(it.absolutePath)
                    MusicType.URL ->
                        replaceUrlChordFragment(it.url)
                }
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
            // 중복 클릭 방지
            if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                SelectBottomSheet.newInstance()
                    .show(requireActivity().supportFragmentManager, "SelectBottomSheet")
            }
            mLastClickTime = SystemClock.elapsedRealtime()
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
                when (item.type) {
                    MusicType.RECORD ->
                        androidViewModel.removeFile(item.absolutePath)
                    MusicType.URL ->
                        androidViewModel.removeUrl(item.url)
                }
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

    private fun replaceChordFragment(path: String) {
        val fragment = RecordChordFragment()

        val bundle = Bundle()
        bundle.putString("Path", path)
        fragment.arguments = bundle

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun replaceUrlChordFragment(url: String) {
        val fragment = UrlChordFragment()

        val bundle = Bundle()
        bundle.putString("Url", url)
        fragment.arguments = bundle

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}