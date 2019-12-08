 package net.zonetech.viopdemo.Activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.sinch.android.rtc.MissingPermissionException
import kotlinx.android.synthetic.main.activity_call.*
import net.zonetech.viopdemo.R
import net.zonetech.viopdemo.Utils.Common

 const val REQUEST_CODE=2019
class CallActivity :BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        videoCallBtn.isEnabled=false
        voicCallBtn.isEnabled=false
        setListeners()

    }

    override fun onServiceConnected() {
        voicCallBtn.isEnabled=true
        videoCallBtn.isEnabled=true
        var name=getSinchServiceInterface()?.userName
        userNameTxt.text = name

    }

    private fun setListeners() {
        voicCallBtn.setOnClickListener {
            if(!nameEditTxt.text.isNullOrEmpty()){
                var userName=nameEditTxt.text.toString()
                try{
                    var call= getSinchServiceInterface()?.callUser(userName)
                    if (call == null) { // Service failed for some reason, show a Toast and abort
                        Toast.makeText(
                            this,
                            "Service is not started. Try stopping the service and starting it again before "
                                    + "placing a call.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }
                    var callId=call?.callId
                    Intent(this,AnsweredCallActivity()::class.java).also {
                        it.putExtra(Common.CALL_ID,callId)
                        it.putExtra(Common.RECIPIENT_NAME,userName)
                        startActivity(it)
                    }
                }
                catch (e:MissingPermissionException){
                    ActivityCompat.requestPermissions(this, arrayOf(e.requiredPermission), 0)



    }
}
 }
        videoCallBtn.setOnClickListener {
            if(!nameEditTxt.text.isNullOrEmpty()){
                var userName=nameEditTxt.text.toString()
                try{
                    var call= getSinchServiceInterface()?.callUserVideo(userName)
                    if (call == null) { // Service failed for some reason, show a Toast and abort
                        Toast.makeText(
                            this,
                            "Service is not started. Try stopping the service and starting it again before "
                                    + "placing a call.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }
                    val callId= call.callId
                    Intent(this,VideoCallActivity()::class.java).also {
                        it.putExtra(Common.CALL_ID,callId)
                        it.putExtra(Common.RECIPIENT_NAME,userName)
                        startActivity(it)
                    }
                }
                catch (e:MissingPermissionException){
                    ActivityCompat.requestPermissions(this, arrayOf(e.requiredPermission), 0)

                }
            }
        }
 }

 }