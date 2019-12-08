package net.zonetech.viopdemo.Activities

import android.media.AudioManager
import android.os.Bundle
import android.view.View
import com.sinch.android.rtc.AudioController
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallState
import com.sinch.android.rtc.video.VideoCallListener
import kotlinx.android.synthetic.main.activity_answered_call.*
import kotlinx.android.synthetic.main.activity_answered_call.callStatus
import kotlinx.android.synthetic.main.activity_answered_call.callerName
import kotlinx.android.synthetic.main.activity_answered_call.hangupBtn
import kotlinx.android.synthetic.main.activity_video_call.*
import net.zonetech.viopdemo.R
import net.zonetech.viopdemo.Utils.Common
import java.util.*

class VideoCallActivity : BaseActivity() {
    var callId: String? = null
    var timerTask: AnsweredCallActivity.DurationTimerTask? = null
    var timer: Timer? = null
    private val mAddedListener = false
    private var mLocalVideoViewAdded = false
    private val mRemoteVideoViewAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        callId = intent.getStringExtra(Common.CALL_ID)
        setListeners()
    }

    private fun setListeners() {
        hangupBtn.setOnClickListener {
            endCall()
        }
    }

    private fun endCall() {
        getSinchServiceInterface()?.getCall(callId)?.hangup()
        finish()
    }


    override fun onServiceConnected() {
        val call = getSinchServiceInterface()?.getCall(callId)
        call?.addCallListener(SinchCallListener())
        updateUI()
    }
    private fun updateUI() {
    if(getSinchServiceInterface()==null) return
        val call=getSinchServiceInterface()?.getCall(callId)
        callStatus.text = call?.state.toString()
        callerName.text = intent.getStringExtra(Common.RECIPIENT_NAME)
        if(call?.details?.isVideoOffered!!){
            if(call.state ==CallState.ESTABLISHED)
            {
                setVideoViewVisiblity(true,true)
            }
            else{
                setVideoViewVisiblity(true,false)
            }
        }
        else{
            setVideoViewVisiblity(false,false)
        }
    }

    private fun setVideoViewVisiblity(localVideo: Boolean, remoteVideo: Boolean) {
        if(getSinchServiceInterface()==null) return
        if(!mLocalVideoViewAdded){
            addLocalVideo()
        }
        if(!mRemoteVideoViewAdded){
            addRemoteVideo()
        }

          val vc=getSinchServiceInterface()?.getVideoController()
          if(vc!=null){
              runOnUiThread {
                  run{
                      if(localVideo){ vc.localView.visibility=View.VISIBLE}
                      else if(!localVideo){vc.localView.visibility=View.GONE}
                      if(remoteVideo){ vc.remoteView.visibility=View.VISIBLE}
                      else if(!remoteVideo){vc.remoteView.visibility=View.GONE}
                  }
              }
          }

    }

    private fun addRemoteVideo() {
    }

    private fun addLocalVideo() {
        if(mLocalVideoViewAdded||getSinchServiceInterface()==null) return
        val vc=getSinchServiceInterface()?.getVideoController()
        if(vc!=null){
         runOnUiThread {
             localVideo.addView(vc.localView)
              mLocalVideoViewAdded=true
         }
        }
    }

    inner class SinchCallListener : VideoCallListener {
        override fun onVideoTrackAdded(p0: Call?) {
        }
        override fun onVideoTrackPaused(p0: Call?) {
        }
        override fun onCallEstablished(p0: Call?) {
            volumeControlStream=AudioManager.STREAM_VOICE_CALL
            callStatus.text="Connected"
            callerName.text=p0?.remoteUserId
            var audioManager=getSinchServiceInterface()?.audioController
            audioManager?.enableSpeaker()
           if(p0?.details?.isVideoOffered!!){
               setVideoViewVisiblity(true,true)
           }
        }

        override fun onVideoTrackResumed(p0: Call?) {

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
}