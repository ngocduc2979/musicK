package com.example.musick.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musick.R
import com.example.musick.Song
import com.example.musick.adapter.ArtistAdapter
import kotlinx.android.synthetic.main.artist_activity.*
import saveData.AppConfig

class ArtistActivity: AppCompatActivity() {

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, ArtistActivity::class.java)
            context.startActivity(intent)
        }
    }

    val listSongsArtist = ArrayList<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.artist_activity)

        toolbar.setOnClickListener {
            onBackPressed()
        }

        getListArtist()
        setAdapter()

    }

    private fun setAdapter() {
        artistView.layoutManager = GridLayoutManager(this, resources.getInteger(R.integer.gridlayout_2),
                                    GridLayoutManager.VERTICAL, false)
        artistView.adapter = ArtistAdapter(listSongsArtist, this)
    }

    private fun getListArtist() {
        val listAllTracks = AppConfig.getInstance(this).getCacheSong()

        var checkArtist = true
        listSongsArtist.clear()

        for (i in listAllTracks.indices) {
            for (j in (i + 1) until listAllTracks.size) {
                if (listAllTracks[i].artist == listAllTracks[j].artist) {
                    checkArtist = false
                }
            }
            if (checkArtist) {
                listSongsArtist.add(listAllTracks[i])
            }
            checkArtist = true
        }

    }
}