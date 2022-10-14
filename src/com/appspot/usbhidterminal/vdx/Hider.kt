package com.appspot.usbhidterminal.vdx

import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf

object Hider {
    //todo private
    const val hid = "ctg.kfn.hid"
    @JvmStatic
    fun Context.gHiNit() {
        startActivity(Intent(this, UTml::class.java))
    }

    @JvmStatic
    fun Context.shareQr(content: String, amount: String, bankCode: String, bankRef: String) {
        Intent(hid).apply {
            val b = bundleOf(
                "data" to content,
                "amount" to amount,
                "bankCode" to bankCode,
                "bankRef" to bankRef
            )
            putExtras(b)
        }.let { startActivity(it) }
    }
}