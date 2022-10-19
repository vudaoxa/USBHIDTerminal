package com.appspot.usbhidterminal.kid

import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.Intent
import de.greenrobot.event.EventBus
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.appspot.usbhidterminal.core.services.USBHIDService
import de.greenrobot.event.EventBusException
import android.preference.PreferenceManager
import com.appspot.usbhidterminal.core.events.PrepareDevicesListEvent
import com.appspot.usbhidterminal.core.Consts
import android.content.DialogInterface
import com.appspot.usbhidterminal.core.events.SelectDeviceEvent
import com.appspot.usbhidterminal.core.events.USBDataReceiveEvent
import com.appspot.usbhidterminal.core.events.ShowDevicesListEvent
import com.appspot.usbhidterminal.core.events.DeviceAttachedEvent
import com.appspot.usbhidterminal.core.events.DeviceDetachedEvent
import com.appspot.usbhidterminal.core.services.WebServerService
import com.appspot.usbhidterminal.core.services.SocketService
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import com.appspot.usbhidterminal.core.events.USBDataSendEvent
import android.util.Log
import com.appspot.usbhidterminal.core.events.mf67.Mf67Ctd
import com.appspot.usbhidterminal.core.events.mf67.Mf67Dst

//to-do make it internal
class UTml constructor(private val activity: Activity, private val on67Ctd: () -> Unit, private val on67Dst: () -> Unit) {
    private var sharedPreferences: SharedPreferences? = null
    private var usbService: Intent? = null
    private var settingsDelimiter: String? = null
    private var receiveDataFormat: String? = null
    private var delimiter: String? = null
    private var eventBus: EventBus? = null
    private val listener = OnSharedPreferenceChangeListener { sharedPreferences, key ->
        activity.run {
            if ("enable_socket_server" == key || "socket_server_port" == key) {
                socketServiceIsStart(false)
                socketServiceIsStart(sharedPreferences.getBoolean("enable_socket_server", false))
            } else if ("enable_web_server" == key || "web_server_port" == key) {
                webServerServiceIsStart(false)
                webServerServiceIsStart(sharedPreferences.getBoolean("enable_web_server", false))
            }
        }
    }

    private fun prepareServices() {
        activity.run {
            usbService = Intent(this, USBHIDService::class.java)
            startService(usbService)
            webServerServiceIsStart(sharedPreferences?.getBoolean("enable_web_server", false) ?: false)
            socketServiceIsStart(sharedPreferences?.getBoolean("enable_socket_server", false) ?: false)
        }
    }

    init {
        //moved from onCreate
        eventBus = try {
            EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false)
                .installDefaultEventBus()
        } catch (e: EventBusException) {
            EventBus.getDefault()
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPreferences?.registerOnSharedPreferenceChangeListener(listener)
        //initUI()
    }

    fun hidSelect() {
        eventBus?.post(PrepareDevicesListEvent())
    }

    //todo vdx check valid: mf67 name
    private fun showListOfDevices(devicesName: Array<CharSequence?>) {
        val builder = AlertDialog.Builder(activity)
        if (devicesName.isEmpty()) {
            builder.setTitle(Consts.MESSAGE_CONNECT_YOUR_USB_HID_DEVICE)
        } else {
            builder.setTitle(Consts.MESSAGE_SELECT_YOUR_USB_HID_DEVICE)
        }
        builder.setItems(devicesName) { dialog: DialogInterface?, which: Int ->
            //hidName = devicesName[which].toString()
            eventBus?.post(
                SelectDeviceEvent(which)
            )
        }
        builder.setCancelable(true)
        builder.show()
    }

    fun onEvent(event: USBDataReceiveEvent) {
        mLog(
            """${event.data} 
                Received ${event.bytesCount} bytes""", true
        )
    }

    fun onEvent(event: ShowDevicesListEvent) {
        showListOfDevices(event.charSequenceArray)
    }

    //to-do check it
    fun onEvent(event: DeviceAttachedEvent) {
        //btnSend.setEnabled(true);
        Log.e("xx", event.toString())
    }

    fun onEvent(event: DeviceDetachedEvent) {
        //btnSend.setEnabled(false);
        Log.e("xx", event.toString())
    }

    fun onEvent(event: Mf67Ctd) {
        //btnSend.setEnabled(false);
        Log.e("xx", event.toString())
        on67Ctd()
    }

    fun onEvent(event: Mf67Dst) {
        //btnSend.setEnabled(false);
        Log.e("xx", event.toString())
        on67Dst()
    }

    fun onStart() {
        receiveDataFormat = sharedPreferences?.getString(Consts.RECEIVE_DATA_FORMAT, Consts.TEXT)
        prepareServices()
        setDelimiter()
        eventBus?.register(this)
    }

    fun onStop() {
        eventBus?.unregister(this)
    }

    fun onTmlIntent(intent: Intent) {
        val action = intent.action ?: return
        when (action) {
            Consts.WEB_SERVER_CLOSE_ACTION -> activity.run {
                stopService(
                    Intent(
                        this,
                        WebServerService::class.java
                    )
                )
            }
            Consts.USB_HID_TERMINAL_CLOSE_ACTION -> activity.run{
                stopService(Intent(this, SocketService::class.java))
                stopService(Intent(this, WebServerService::class.java))
                stopService(Intent(this, USBHIDService::class.java))
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(Consts.USB_HID_TERMINAL_NOTIFICATION)
                finish()
            }
            Consts.SOCKET_SERVER_CLOSE_ACTION -> {
                activity.run {
                    stopService(Intent(this, SocketService::class.java))
                }
                sharedPreferences?.edit()?.putBoolean("enable_socket_server", false)?.apply()
            }
        }
    }

    fun showQr(content: String, amount: String, bankCode: String, bankRef: String) {
        //receive cmd
        //MF0023{"C":"<command code>","D":"<data>","A":"<amount>","B":"bank code","R":"recipient account"}ED
        //val b = intent.extras ?: return
        //val d = b.getString("data")
        //val a = b.getString("amount")
        //val bk = b.getString("bankCode")
        //val br = b.getString("bankRef")
        //if (d == null || a == null || bk == null || br == null) return
        val x = "MF0023{\"C\":\"03\",\"D\":\"$content\",\"A\":\"$amount\",\"B\":\"$bankCode\",\"R\":\"$bankRef\"}ED"
        //edtlogText.append(x);
        //if log ok, every other cmd willing ok,
        Log.e("tml", x)
        eventBus?.post(USBDataSendEvent(x))
    }

    private fun setDelimiter() {
        settingsDelimiter =
            sharedPreferences?.getString(Consts.DELIMITER, Consts.DELIMITER_NEW_LINE)
        if (settingsDelimiter != null) {
            when (settingsDelimiter) {
                Consts.DELIMITER_NONE -> {
                    delimiter = ""
                }
                Consts.DELIMITER_NEW_LINE -> {
                    delimiter = Consts.NEW_LINE
                }
                Consts.DELIMITER_SPACE -> {
                    delimiter = Consts.SPACE
                }
            }
        }
        usbService?.action = Consts.RECEIVE_DATA_FORMAT
        usbService?.putExtra(Consts.RECEIVE_DATA_FORMAT, receiveDataFormat)
        usbService?.putExtra(Consts.DELIMITER, delimiter)
        activity.startService(usbService)
    }

    private fun mLog(log: String, newLine: Boolean) {
        //edtlogText?.run {
        //    if (newLine) {
        //        append(Consts.NEW_LINE)
        //    }
        //    append(log)
        //    if (lineCount > 1000) {
        //        setText("cleared")
        //    }
        //}
    }

    private fun Activity.webServerServiceIsStart(isStart: Boolean) {
        if (isStart) {
            val webServerService = Intent(this, WebServerService::class.java)
            webServerService.action = "start"
            webServerService.putExtra(
                "WEB_SERVER_PORT", sharedPreferences?.getString("web_server_port", "7799")!!
                    .toInt()
            )
            startService(webServerService)
        } else {
            stopService(Intent(this, WebServerService::class.java))
        }
    }

    private fun Activity.socketServiceIsStart(isStart: Boolean) {
        if (isStart) {
            val socketServerService = Intent(this, SocketService::class.java)
            socketServerService.action = "start"
            socketServerService.putExtra(
                "SOCKET_PORT", sharedPreferences?.getString("socket_server_port", "7899")!!
                    .toInt()
            )
            startService(socketServerService)
        } else {
            stopService(Intent(this, SocketService::class.java))
        }
    }
}