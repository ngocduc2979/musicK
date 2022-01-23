package com.example.musick

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface RequestAPI {

    @GET()
    fun register_spring_list(@Url subMain: String): Observable<List<Song>>

    @GET()
    fun register_album_list(@Url subMain: String): Observable<List<Album>>
}