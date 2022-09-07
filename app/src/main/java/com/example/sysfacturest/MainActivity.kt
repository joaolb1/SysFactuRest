package com.example.sysfacturest

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {
    //var toolbar: Toolbar? = null
    var nube = false;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //toolbar = findViewById(R.id.toolbar)
        //setSupportActionBar(toolbar)
        val pref = applicationContext.getSharedPreferences("MyPref", 0) // 0 - for private mode
        var ipUrl = pref.getString("url_app", "").toString();
        var siteName = pref.getString("site_app", "").toString();
        if(nube == true){
            if(ipUrl == "" || ipUrl == null){
                var pageUrl = "https://wanvendor.pe/Exotica"
                val editor = pref.edit()
                editor.putString("url_app", pageUrl); // Storing string
                editor.commit(); // commit changes
                val intent = WebActivity.newIntent(this, pageUrl.toString())
                startActivity(intent)
            }else{
                val intent = WebActivity.newIntent(this, "https://wanvendor.pe/Exotica/ReporteVenta/ReporteVentaVista")
                startActivity(intent)
            }
        }else{
            //example : https://192.168.1.100:8081/sitio
            var pageUrl = "https://" + ipUrl + ":8081/"+ siteName
            if(ipUrl == "" || ipUrl == null){
                pageUrl ="Https://192.168.1.100:8081/SysMozo/";
                val editor = pref.edit()
                editor.putString("url_app", "192.168.1.100"); // Storing string
                editor.putString("site_app", "SysMozo"); // Storing string
                editor.commit(); // commit changes
            }
            val intent = WebActivity.newIntent(this, pageUrl.toString())
            startActivity(intent)
        }
    }
}