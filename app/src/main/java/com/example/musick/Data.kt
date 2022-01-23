package com.example.musick

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class Song(var songName: String,
                var artist: String,
                var path: String,
                var album: String,
                var duration: String,
                var thumbnail: String): Serializable, Parcelable

data class Album(var id: String,
                 var name: String,
                 var cover: String): Serializable
