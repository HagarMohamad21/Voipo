package net.zonetech.viopdemo.Activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import net.zonetech.viopdemo.Sinch.SinchService
import net.zonetech.viopdemo.Utils.Common.Companion.MESSAGE_PERMISSIONS_NEEDED
import net.zonetech.viopdemo.Utils.Common.Companion.MESSENGER
import net.zonetech.viopdemo.Utils.Common.Companion.REQUIRED_PERMISSION

open class BaseActivity : AppCompatActivity(),ServiceConnection {
    private var mSinchServiceInterface: SinchService.SinchServiceInterface? = null
    private val TAG = "BaseActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindService()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(
                 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }



    override fun onServiceConnected(
        componentName: ComponentName,
        iBinder: IBinder?
    ) {
        if (SinchService::class.java.name == componentName.className) {
            mSinchServiceInterface = iBinder as SinchService.SinchServiceInterface?
            onServiceConnected()
        }
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        if (SinchService::class.java.name == componentName.className) {
            mSinchServiceInterface = null
            onServiceDisconnected()
        }
    }

    protected open fun onServiceConnected() { // for subclasses
    }

    protected open fun onServiceDisconnected() { // for subclasses
    }

    protected open fun getSinchServiceInterface(): SinchService.SinchServiceInterface? {
        return mSinchServiceInterface
    }

    private val messenger = Messenger(object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_PERMISSIONS_NEEDED -> {
                    val bundle = msg.data
                    val requiredPermission =
                        bundle.getString(REQUIRED_PERMISSION)
                    ActivityCompat.requestPermissions(
                        this@BaseActivity,
                        arrayOf(requiredPermission),
                        0
                    )
                }
            }
        }
    })



     open fun bindService() {
        val serviceIntent = Intent(this, SinchService::class.java)
        serviceIntent.putExtra(MESSENGER, messenger)
        applicationContext.bindService(
            serviceIntent,
            this,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: ")
        var granted = grantResults.isNotEmpty()
        for (grantResult in grantResults) {
            granted = granted and (grantResult == PackageManager.PERMISSION_GRANTED)
        }
        if (granted) {
            Toast.makeText(this, "You may now place a call", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this,
                "This application needs permission to use your microphone and camera to function properly.",
                Toast.LENGTH_LONG
            ).show()
        }
        mSinchServiceInterface!!.retryStartAfterPermissionGranted()
    }
}
