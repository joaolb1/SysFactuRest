package com.example.sysfacturest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.android.synthetic.main.borrarcache.view.*
import kotlinx.android.synthetic.main.cambioip.view.*
import kotlinx.android.synthetic.main.cambioip.view.btnCancelar
import kotlinx.android.synthetic.main.toolbar.*


class WebActivity : AppCompatActivity() {
    private var mwebView: WebView? = null

    companion object {
        const val PAGE_URL = "pageUrl"
        const val MAX_PROGRESS = 100

        fun newIntent(context: Context, pageUrl: String): Intent {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra(PAGE_URL, pageUrl)
            return intent
        }
    }

    private lateinit var pageUrl: String
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        //ORIENTACION HORIZONTAL
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //ORIENTACION VERTICAL
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        var nube = false

        cambiarIp.setOnClickListener {
            val pref = applicationContext.getSharedPreferences("MyPref", 0) // 0 - for private mode
            var ipUrl = pref.getString("url_app", "").toString();
            var siteName = pref.getString("site_app", "").toString();
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.cambioip, null)
            mDialogView.txt_ip.setText(ipUrl.toString());

            mDialogView.txt_site.setText(siteName.toString());

            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Configuracion")
            //show dialog
            val  mAlertDialog = mBuilder.show()
            //login button click of custom layout
            mDialogView.btnGuardar.setOnClickListener {
                //dismiss dialog
                var txt_ip = mDialogView.txt_ip.text;
                val pref = applicationContext.getSharedPreferences("MyPref", 0) // 0 - for private mode
                val editor = pref.edit()

                var txt_site = mDialogView.txt_site.text;

                editor.putString("url_app", txt_ip.toString())
                editor.putString("site_app", txt_site.toString())
                editor.commit()

                if(nube == true){
                    val pageUrl = txt_ip;
                    webView.loadUrl(pageUrl.toString());
                } else{
                    val pageUrl = "https://" + txt_ip + ":8081/" + txt_site
                    webView.loadUrl(pageUrl);
                }
                //set the input text in TextView
                Toast.makeText(applicationContext,
                    "Cambiando IP", Toast.LENGTH_SHORT).show();
                mAlertDialog.dismiss();
            }
            //cancel button click of custom layout
            mDialogView.btnCancelar.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss();
            }
        }

        borrarCache.setOnClickListener {
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.borrarcache, null)

            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Configuracion")
            //show dialog
            val  mAlertDialog = mBuilder.show()
            //login button click of custom layout
            mDialogView.btnBorrarCache.setOnClickListener {
                clearCookies(this);
                Toast.makeText(applicationContext,
                    "Borrando cookies y cache", Toast.LENGTH_SHORT).show();
                mAlertDialog.dismiss();
                webView.reload();
            }
            //cancel button click of custom layout
            mDialogView.btnCancelar.setOnClickListener {
                mAlertDialog.dismiss() ;
            }
        }

        imprimir.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
               createWebPrintJob(webView)
           }
        }

        home.setOnClickListener {
            Toast.makeText(applicationContext,
                "actualizando la vista, espere por favor!", Toast.LENGTH_SHORT).show();

            this.mwebView!!.setInitialScale(1);
            webView.reload()
        }

        // get pageUrl from String
        pageUrl = intent.getStringExtra(PAGE_URL)
            ?: throw IllegalStateException("field $PAGE_URL missing in Intent")

        initWebView()

        loadUrl(pageUrl)
        handlePullToRefresh()
    }

    private fun handlePullToRefresh() {
    }

    //@SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(Build.VERSION_CODES.ECLAIR_MR1)
    private fun initWebView() {

        //WebView
        mwebView = findViewById(R.id.webView) as WebView
        val webSettings: WebSettings = this.mwebView!!.getSettings()
        webSettings.javaScriptEnabled = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.setSupportZoom(false);
        //improve webView performance
        mwebView!!.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH)
        //mwebView!!.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK)
        mwebView!!.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE)
        mwebView!!.getSettings().setAppCacheEnabled(true)
        //mwebView!!.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY)
        webSettings.domStorageEnabled = true
        //webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        webSettings.useWideViewPort = true
        //webSettings.savePassword = true
        //webSettings.saveFormData = true
        webSettings.setAppCacheMaxSize(5*1024*1024)
        //webSettings.setEnableSmoothTransition(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        webView.webViewClient = object : WebViewClient() {
            override
            fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            //webView.goBack()
            return true
        }
        // If it wasn't the Back key or there's no web page history, exit the activity)
        return super.onKeyDown(keyCode, event)
    }

    private fun loadUrl(pageUrl: String) {
        webView.loadUrl(pageUrl)
    }

    override fun onBackPressed() {
        //if(webView.canGoBack()){
         //   webView.goBack()
       // }else{
        //    super.onBackPressed()
       // }
    }

    @SuppressWarnings("deprecation")
    fun clearCookies(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } else if (context != null) {
            val cookieSyncManager = CookieSyncManager.createInstance(context)
            cookieSyncManager.startSync()
            val cookieManager: CookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            cookieManager.removeSessionCookie()
            cookieSyncManager.stopSync()
            cookieSyncManager.sync()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun createWebPrintJob(webView: WebView) {
        val printManager = this
            .getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.createPrintDocumentAdapter("MyDocument")
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }
        val jobName = getString(R.string.app_name) +
                " Print Test"
        printManager.print(
            jobName, printAdapter,
            PrintAttributes.Builder().build()
        )
    }
}

