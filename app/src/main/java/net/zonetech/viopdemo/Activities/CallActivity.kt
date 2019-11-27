package net.zonetech.viopdemo.Activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.sinch.android.rtc.SinchClient
import kotlinx.android.synthetic.main.activity_call.*
import net.zonetech.viopdemo.Sinch.Client
import net.zonetech.viopdemo.R

const val REQUEST_CODE=2019
class CallActivity : AppCompatActivity() {
   private lateinit var client:SinchClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        askPermission()
        getClient()
        setListeners()

    }

    private fun askPermission() {
      if(ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
          ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
              REQUEST_CODE
          )
      }
    }

    private fun setListeners() {
        callBtn.setOnClickListener {
            client.callClient.callUser("Esraa")
        }
    }

    private fun  getClient() {
        client= Client(this).getClient("Hagar")
        client.setSupportCalling(true)
        client.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode== REQUEST_CODE){
            if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                // we have permission
            }
            else {
                finish()
            }
        }
    }
}
