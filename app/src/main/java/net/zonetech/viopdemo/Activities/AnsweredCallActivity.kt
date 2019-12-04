package net.zonetech.viopdemo.Activities

import android.media.AudioManager
import android.os.Bundle
import com.sinch.android.rtc.AudioController.UseSpeakerphone
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallListener
import kotlinx.android.synthetic.main.activity_answered_call.*
import net.zonetech.viopdemo.R
import net.zonetech.viopdemo.Utils.Common
import java.util.*

class AnsweredCallActivity : BaseActivity() {
     var callId:String?=null
    var timerTask:DurationTimerTask?=null
    var timer:Timer?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answered_call)
        callId=intent.getStringExtra(Common.CALL_ID)
        setListeners()
    }

    override fun onServiceConnected() {
     val call=getSinchServiceInterface()?.getCall(callId)
           callStatus.text=call?.state.toString()
           callerName.text=intent.getStringExtra(Common.RECIPIENT_NAME)
           call?.addCallListener(SinchCallListener())

    }


    private fun setListeners() {
     hangupBtn.setOnClickListener {
         endCall()
     }
    }
    inner class SinchCallListener:CallListener{
        override fun onCallEstablished(p0: Call?) {
            volumeControlStream=AudioManager.STREAM_VOICE_CALL
            callStatus.text="Connected"
            callerName.text=p0?.remoteUserId
         var audioManager=getSinchServiceInterface()?.audioController
            audioManager?.disableSpeaker()
            audioManager?.enableAutomaticAudioRouting(true, UseSpeakerphone.SPEAKERPHONE_AUTO)
        }

        override fun onCallProgressing(p0: Call?) {
        }

        override fun onShouldSendPushNotification(p0: Call?, p1: MutableList<PushPair>?) {
        }


        override fun onCallEnded(p0: Call?) {
            volumeControlStream=AudioManager.USE_DEFAULT_STREAM_TYPE
            endCall()
        }
    }

    inner class DurationTimerTask: TimerTask() {
        override fun run() {
            runOnUiThread {
               run {
                    updateCallDuration()
                }
            }
        }
    }
    private fun updateCallDuration() {
        var call=getSinchServiceInterface()?.getCall(callId)
        if(call!=null){
            callDuration.text=formatTime(call.details.duration)
        }
    }
    private fun endCall() {
        getSinchServiceInterface()?.getCall(callId)?.hangup()
        finish()
    }

    override fun onBackPressed() {
    }

    override fun onResume() {
        super.onResume()
        timer=Timer()
        timerTask=DurationTimerTask()
        timer!!.schedule(timerTask,0,500)
    }

    override fun onPause() {
        super.onPause()
        timerTask!!.cancel()
        timer!!.cancel()

    }

    private fun formatTime(d:Int):String{
        var sec=d%60
        var min=d/60
        return "$min:$sec"
    }
}
