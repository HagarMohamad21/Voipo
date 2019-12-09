package net.zonetech.viopdemo.Activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.sinch.android.rtc.MissingPermissionException
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallListener
import kotlinx.android.synthetic.main.activity_incoming_call.*
import net.zonetech.viopdemo.R
import net.zonetech.viopdemo.Utils.AudioPlayer
import net.zonetech.viopdemo.Utils.Common

class IncomingCallActivity : BaseActivity() {
    var audioPlayer:AudioPlayer?=null
    var callId:String?=null
    var call:Call?=null
    var isVideo=false
    var activity:Activity?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)
        audioPlayer= AudioPlayer(this)
        audioPlayer?.playRingTone()
        initViews()
       setListeners()
    }

    private fun setListeners() {
        answerBtn.setOnClickListener {
            audioPlayer?.stopRingTone()
            call=getSinchServiceInterface()?.getCall(callId)
            if(call!=null){
                try{
                    call?.answer()
                    Intent(this, activity!!::class.java).also {
                        it.putExtra(Common.CALL_ID, callId)
                        startActivity(it)
                    }

                }
                catch (e:MissingPermissionException){
                    ActivityCompat.requestPermissions(this,
                        arrayOf(e.requiredPermission),2019)
                }
            }
            else{
                finish()
            }
        }
        declineBtn.setOnClickListener {
            audioPlayer?.stopRingTone()
            call=getSinchServiceInterface()?.getCall(callId)
            if(call!=null){
                call?.hangup()
            }
            finish()

        }

    }

    private fun initViews() {
        callId=intent.getStringExtra(Common.CALL_ID)
        isVideo=intent.getBooleanExtra(Common.CALL_TYPE,false)
        if(isVideo){
            callType.text="Incoming video call"
            activity=VideoCallActivity()
        }
        else{
            callType.text="Incoming call"
            activity=AnsweredCallActivity()
        }

    }
    override fun onServiceConnected() {

         call=getSinchServiceInterface()?.getCall(callId)
        if (call != null) {
            call?.addCallListener(SinchCallListener())
            callerNameTxt.text=call?.remoteUserId
        }
        else{
            finish()
        }




    }

    inner class SinchCallListener():CallListener{
        override fun onCallEstablished(p0: Call?) {
        }

        override fun onCallProgressing(p0: Call?) {
        }

        override fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {
        }

        override fun onCallEnded(p0: Call?) {
            audioPlayer?.stopRingTone()
            finish()        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==2019){
            if(grantResults.isNotEmpty()&&grantResults[0]
                ==PackageManager.PERMISSION_GRANTED)
         Toast.makeText(this, "You may now answer the call", Toast.LENGTH_LONG).show()
            else
            Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast.LENGTH_LONG).show()

        }
    }
}
