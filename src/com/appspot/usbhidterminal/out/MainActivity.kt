package com.appspot.usbhidterminal.out

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appspot.usbhidterminal.databinding.ActHlBinding
import com.appspot.usbhidterminal.kid.UTml

//demo purpose only, not show in product UI
internal class MainActivity : AppCompatActivity() {
    private val bd by lazy {
        ActHlBinding.inflate(layoutInflater)
    }

    private val tml by lazy {
        UTml(this, { on67Ctd() }) { on67Dst() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bd.root)
        bd.run {
            hl.setOnClickListener {
                //go hid init
                //gHiNit()
                tml.hidSelect()
            }

            btSend.setOnClickListener {
                shareQr()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        tml.onTmlIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        tml.onStart()
    }

    override fun onStop() {
        super.onStop()
        tml.onStop()
    }

    private fun on67Ctd() {
        bd.btSend.isEnabled = true
    }

    private fun on67Dst() {
        bd.btSend.isEnabled = false
    }

    private var i = 0
    private fun shareQr() {
        tml.showQr("fnb-i1234----$i", "10,00${i++}", "VIB", "0025142")
        //shareQr("fnb-i1234----$i", "10,00${i++}", "VIB", "0025142")
        //shareQr( "fnb-i1234", "10,000", "VIB", "0025142")
    }
}