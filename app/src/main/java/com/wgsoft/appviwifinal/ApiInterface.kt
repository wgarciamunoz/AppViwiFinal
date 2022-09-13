package com.wgsoft.appviwifinal

import retrofit2.Call
import retrofit2.http.GET


interface ApiInterface {
    @GET("ListaBeacons")
    fun getData(): Call<List<BeaconsItem>>
}