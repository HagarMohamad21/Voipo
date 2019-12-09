package net.zonetech.viopdemo.Activities

import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallState
import com.sinch.android.rtc.video.VideoCallListener
import kotlinx.android.synthetic.main.activity_video_call.*
import net.zonetech.viopdemo.R
import net.zonetech.viopdemo.Utils.Common
import java.util.*

class VideoCallActivity : BaseActivity() {
    var callId: String? = null
    var timerTask: DurationTinerTask? = null
    var timer: Timer? = null
    private var mAddedListener = false
    private var mLocalVideoViewAdded = false
    private var mRemoteVideoViewAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        callId = intent.getStringExtra(Common.CALL_ID)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
        if(call!=null){
            if(!mAddedListener){
                call.addCallListener(SinchCallListener())
                mAddedListener=true

            }
            else{
                finish()
            }
        }

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
                setVideoViewVisibility(true,true)
            }
            else{
                setVideoViewVisibility(true,false)
            }
        }
        else{
            setVideoViewVisibility(false,false)
        }
    }
    private fun setVideoViewVisibility(localVideo: Boolean, remoteVideo: Boolean) {
        if (getSinchServiceInterface() == null) return
        if (!mLocalVideoViewAdded) {
            addLocalVideo()
        }
        if (!mRemoteVideoViewAdded) {
            addRemoteVideo()
        }

        val vc = getSinchServiceInterface()?.getVideoController()
        if (vc != null) {
            runOnUiThread {
                run {
                    if (localVideo) {
                        vc.localView.visibility = View.VISIBLE
                    } else if (!localVideo) {
                        vc.localView.visibility = View.GONE
                    }
                    if (remoteVideo) {
                        vc.remoteView.visibility = View.VISIBLE
                    } else if (!remoteVideo) {
                        vc.remoteView.visibility = View.GONE
                    }
                }
            }
        }

    }
    private fun addRemoteVideo() {
        if(mRemoteVideoViewAdded||getSinchServiceInterface()==null) return
        val vc=getSinchServiceInterface()?.getVideoController()
        if(vc!=null){
            runOnUiThread{
                remoteVideo.addView(vc.remoteView)
                mRemoteVideoViewAdded=true

            }
        }
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
                setVideoViewVisibility(localVideo = true, remoteVideo = true)
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

    inner class DurationTinerTask: TimerTask() {
        override fun run() {
            runOnUiThread{
                updateCallDuration()
            }
        }
    }

    private fun updateCallDuration() {
        var call=getSinchServiceInterface()?.getCall(callId)
        callDuration.text=foramatDuration(call?.details?.duration!!)
    }

    private fun foramatDuration(duration: Int): String {
        var min=duration/60
        var sec=duration%60
        return "$min:$sec"
    }

    override fun onBackPressed() {

    }



    override fun onStop() {
        super.onStop()
        timerTask?.cancel()
        timer?.cancel()
        removeVideoViews()
    }

    override fun onStart() {
        super.onStart()
        timer=Timer()
        timerTask=DurationTinerTask()
        timer?.schedule(timerTask,0,500)
    }
    private fun  removeVideoViews(){
        if(getSinchServiceInterface()==null) return
        val vc=getSinchServiceInterface()?.getVideoController()
        if(vc!=null){
            runOnUiThread {
                run{
                    val parent =vc.remoteView.parent as ViewGroup
                    parent.removeView(vc.remoteView)
                    val parent2 =vc.localView.parent as ViewGroup
                    parent2.removeView(vc.localView)
                    mLocalVideoViewAdded = false
                    mRemoteVideoViewAdded = false
                }
            }
        }
    }
}