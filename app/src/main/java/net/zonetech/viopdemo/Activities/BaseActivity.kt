package net.zonetech.viopdemo.Activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import net.zonetech.viopdemo.Sinch.SinchService
import net.zonetech.viopdemo.Utils.Common

class BaseActivity : AppCompatActivity(),ServiceConnection {
   var sinchServiceBinder:SinchService.SinchServiceBinder?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindService()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN
               or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
               or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
               or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
               or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    private var messenger=Messenger(object : Handler() {
        override fun handleMessage(msg: Message?) {
           when(msg!!.what){
               Common.MESSAGE_PERMISSIONS_NEEDED->{
                   var bundle=msg.data
                   var requiredPermission=bundle.getString(Common.REQUIRED_PERMISSION)
                   ActivityCompat.requestPermissions(this@BaseActivity, arrayOf(
                       requiredPermission),2000)
               }
           }

        }
    })

   private fun  bindService(){
        Intent(this,SinchService()::class.java).also {
            it.putExtra(Common.MESSENGER,messenger)
            bindService(it,this, Context.BIND_AUTO_CREATE)

        }

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        if(name!!.className==SinchService::class.qualifiedName){
            sinchServiceBinder= null
            onServiceDisconnected()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
      if(name!!.className==(SinchService::class.qualifiedName)){
          sinchServiceBinder= service as SinchService.SinchServiceBinder
          onServiceConnected()
      }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==2000){
            if(grantResults.isNotEmpty() &&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"You may now place a call",Toast.LENGTH_LONG).show()
                 sinchServiceBinder!!.retryStartAfterPermissionGranted()
            }
        }
    }

    protected fun onServiceConnected() { // for subclasses
    }

    protected fun onServiceDisconnected() { // for subclasses
    }
}
