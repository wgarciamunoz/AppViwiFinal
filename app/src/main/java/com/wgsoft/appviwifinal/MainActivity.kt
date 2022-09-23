package com.wgsoft.appviwifinal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "http://80.241.211.8/appviwiback/rest/"

class MainActivity : AppCompatActivity() {

    private var tts: TextToSpeech? = null
    lateinit var  myAdapter : MyUserAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var recyclerView : RecyclerView
    private val pDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        var hasBluetooh = isBluetoothEnabled()

        if(!hasBluetooh){
            enableBluetooth()
        }

        getMyDAta()
    }

    private fun getMyDAta() {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)
        val retrofitData = retrofitBuilder.getData()
        retrofitData.enqueue(object : Callback<List<BeaconsItem>?> {
            override fun onResponse(
                call: Call<List<BeaconsItem>?>,
                response: Response<List<BeaconsItem>?>
            ) {
                val response = response.body()!!
                Log.d("MainAc", "response: " + response[0].BeaconNombre )
                myAdapter = MyUserAdapter(baseContext, response)
                myAdapter.notifyDataSetChanged()
                recyclerView.adapter = myAdapter

                myAdapter.setOnItemClickListener(object : MyUserAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {
                        //Toast.makeText(this@MainActivity, "hola $position", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, activity_user_detailed::class.java)
                        intent.putExtra("nombreBeacon", response[position].BeaconNombre)
                        intent.putExtra("username", response[position].BeaconUUID)
                        intent.putExtra("descripcionDestino", response[position].BeaconDescripcionDestino)
                        intent.putExtra("descripcionObstaculo", response[position].BeaconDescripcionObstaculo)
                        intent.putExtra("BeaconUUID", response[position].BeaconUUID)
                        startActivity(intent)
                    }


                })

            }

            override fun onFailure(call: Call<List<BeaconsItem>?>, t: Throwable) {
                Log.d("MainAc", "onFailure: " + t.message)

            }
        })
    }
    //method to enable bluetooth
    @SuppressLint("MissingPermission")
    fun enableBluetooth() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()
        }
        val enabled_bluetooh: String = getString(R.string.enabled_bluetooh)
        showSnackBar(enabled_bluetooh, this)
    }


    fun showSnackBar(message: String?, activity: Activity?) {
        if (null != activity && null != message) {
            Snackbar.make(
                activity.findViewById(android.R.id.content),
                message, Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    fun isBluetoothEnabled(): Boolean {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return mBluetoothAdapter.isEnabled
    }

    companion object {
        val TAG = "MainActivity"
        val PERMISSION_REQUEST_BACKGROUND_LOCATION = 0
        val PERMISSION_REQUEST_BLUETOOTH_SCAN = 1
        val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 2
        val PERMISSION_REQUEST_FINE_LOCATION = 3
    }
}