package com.appspot.usbhidterminal.vdx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appspot.usbhidterminal.core.events.DeviceAttachedEvent
import com.appspot.usbhidterminal.core.events.DeviceDetachedEvent
import com.appspot.usbhidterminal.databinding.ActHlBinding
import com.appspot.usbhidterminal.vdx.Hider.gHiNit
import com.appspot.usbhidterminal.vdx.Hider.shareQr

//demo purpose only, not show in product UI
internal class MainActivity : AppCompatActivity() {
    private val bd by lazy {
        ActHlBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bd.root)
        bd.run {
            hl.setOnClickListener {
                gHiNit()
            }

            btSend.setOnClickListener {
                shareQr()
            }
        }
    }

    //todo
    fun onEvent(event: DeviceAttachedEvent?) {
        //btnSend.setEnabled(true)
    }

    fun onEvent(event: DeviceDetachedEvent?) {
        //btnSend.setEnabled(false)
    }

    private var i = 0
    private fun shareQr() {
        shareQr("fnb-i1234----$i", "10,00${i++}", "VIB", "0025142")
        //shareQr( "fnb-i1234", "10,000", "VIB", "0025142")
    }
}