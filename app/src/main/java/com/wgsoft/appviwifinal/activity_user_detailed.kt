package com.wgsoft.appviwifinal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class activity_user_detailed : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detailed)


        val nameDetalle : TextView = findViewById(R.id.tvDetalle)
        val nameDestino : TextView = findViewById(R.id.tvDestino)
        val nameDistancia : TextView = findViewById(R.id.tvDistancia)

        val bundle : Bundle?= intent.extras
        val name = bundle!!.getString("name")
        val username = bundle!!.getString("descripcionDestino")
        val descripcionObstaculo = bundle!!.getString("descripcionObstaculo")

        nameDetalle.text = username + " " + descripcionObstaculo
        nameDestino.text = name

    }
}