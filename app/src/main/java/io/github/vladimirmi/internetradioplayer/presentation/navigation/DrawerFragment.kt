package io.github.vladimirmi.internetradioplayer.presentation.navigation

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import kotlinx.android.synthetic.main.fragment_drawer.*

/**
 * Created by Vladimir Mikhalev 08.04.2019.
 */

class DrawerFragment : Fragment() {

    private lateinit var adapter: DrawerAdapter

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val menu = PopupMenu(requireContext(), null).menu
        MenuInflater(requireContext()).inflate(R.menu.menu_drawer, menu)

        adapter = DrawerAdapter(menu)
        drawerRv.adapter = adapter
        drawerRv.layoutManager = LinearLayoutManager(requireContext())
        drawerRv.addItemDecoration(DrawerItemDecoration(requireContext()))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_drawer, menu)
    }
}