package com.example.musick.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musick.Album
import com.example.musick.R
import com.example.musick.activity.AlbumSpringActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_album_home.view.*

class AlbumHomeAdapter(val listAlbum: ArrayList<Album>, val context: Context):
    RecyclerView.Adapter<AlbumHomeAdapter.AlbumHomeViewHolder>() {

    class AlbumHomeViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val cover = view.image_cover
        val name = view.albumsName
        val albumView = view.albumView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHomeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_album_home, parent, false)
        return AlbumHomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumHomeViewHolder, position: Int) {
        holder.name.text = listAlbum[position].name
//
//        io.reactivex.rxjava3.core.Observable.create<String> {
//            it.onNext(listAlbum[position].cover)
//            it.onComplete()
//        }.subscribeOn(Schedulers.newThread())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                Glide.with(context)
//                    .load(it)
//                    .placeholder(R.drawable.music_default)
//                    .error("image error")
//                    .centerCrop()
//                    .into(holder.cover)
//            }, {
//                it.printStackTrace()
//            })

        Glide.with(context)
            .load(listAlbum[position].cover)
            .centerCrop()
            .into(holder.cover)


        holder.albumView.setOnClickListener {
            AlbumSpringActivity.launch(context, listAlbum[position])
        }
    }

    override fun getItemCount(): Int {
        return listAlbum.size
    }
}