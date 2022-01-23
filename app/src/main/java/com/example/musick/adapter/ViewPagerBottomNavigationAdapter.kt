package com.example.musick.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.musick.fragment.FavoriteFragment
import com.example.musick.fragment.HomeFragment
import com.example.musick.fragment.LibraryFragment
import com.example.musick.fragment.SearchFragment

class ViewPagerBottomNavigationAdapter(fm: FragmentManager, var context: Context): FragmentStatePagerAdapter(fm) {


    override fun getCount(): Int {
        return 4
    }

    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            return HomeFragment.newInstance()
        } else if (position == 1){
            return FavoriteFragment.newInstance()
        } else if (position == 2) {
            return SearchFragment.newInstance()
        } else {
            return LibraryFragment.newInstance()
        }
    }
}