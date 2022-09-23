package com.wgsoft.appviwifinal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val btnDestino = findViewById<Button>(R.id.btnDestino)
        btnDestino.setOnClickListener {
            val navigate = Intent(this, MainActivity::class.java)
            startActivity(navigate)
        }

        val btnSalir =findViewById<Button>(R.id.btnSalir)
        btnSalir.setOnClickListener{
            finish()
        }
    }
}