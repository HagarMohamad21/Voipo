package net.zonetech.viopdemo.Sinch

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import com.sinch.android.rtc.*
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallClient
import com.sinch.android.rtc.calling.CallClientListener
import net.zonetech.viopdemo.Activities.IncomingCallActivity
import net.zonetech.viopdemo.Utils.Common.Companion.CALL_ID
import net.zonetech.viopdemo.Utils.Common.Companion.MESSAGE_PERMISSIONS_NEEDED
import net.zonetech.viopdemo.Utils.Common.Companion.MESSENGER
import net.zonetech.viopdemo.Utils.Common.Companion.REQUIRED_PERMISSION

class SinchService :Service() {
    private lateinit var messenger:Messenger
    private  var sinchServiceBinder= SinchServiceBinder()
    private lateinit var client: SinchClient
    private lateinit var mListener: StartFailedListener
    //we should get mUserId from shared pref
    private val mUserId: String? = "Hagar"

    override fun onBind(intent: Intent?): IBinder? {
        messenger=intent!!.getParcelableExtra(MESSENGER)
     return sinchServiceBinder
    }

    override fun onCreate() {
        super.onCreate()
        attemptAutoStart()
    }
    private fun stop(){
      if(client!=null){
          client.terminateGracefully()
      }
    }
    private fun attemptAutoStart() {
      if(messenger!=null){
          start()
      }
    }

    private fun start() {
        var permissionsGranted = true
        createClient()

        try { //mandatory checks
            client.checkManifest()
            // check for bluetooth for automatic audio routing
            if (baseContext.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED
            ) {
                throw MissingPermissionException(Manifest.permission.BLUETOOTH)
            }
        } catch (e: MissingPermissionException) {
            permissionsGranted = false
            if (messenger != null) {
                val message = Message.obtain()
                val bundle = Bundle()
                bundle.putString(REQUIRED_PERMISSION, e.requiredPermission)
                message.data = bundle
                message.what = MESSAGE_PERMISSIONS_NEEDED
                try {
                    messenger.send(message)
                } catch (e1: RemoteException) {
                    e1.printStackTrace()
                }
            }
        }
        if (permissionsGranted) {
            client.start()
        }

    }

    private fun createClient() {
        client= Client(this).getClient(mUserId!!)
        client.setSupportCalling(true)
        client.startListeningOnActiveConnection()
        client.addSinchClientListener(MySinchClientListener())
    }

   inner class SinchServiceBinder() : Binder(){
        fun callPhoneNumber(phoneNumber:String): Call {
            return client.callClient.callPhoneNumber(phoneNumber)
        }

        fun callUserByName(userName:String):Call?{
            if(client==null) return null
            return client.callClient.callUser(userName) }

        fun isStarted():Boolean{
            return   return client != null && client.isStarted
        }
        fun retryStartAfterPermissionGranted(){
         attemptAutoStart()
        }
       fun startClient(){
           start()
       }
       fun setStartListener(listener: StartFailedListener){
           mListener=listener
       }

       fun getCall():Call?{
           if(client==null) return null
           return client.callClient.getCall(CALL_ID)
       }
       fun getAudioController():AudioController?{
           if(!isStarted()){
               return null
           }
           return client.audioController
       }
    }

  inner   class MySinchClientListener : SinchClientListener {
        override fun onClientStarted(p0: SinchClient?) {
          if(mListener!=null){
              mListener.onStarted()
          }
        }

        override fun onClientStopped(p0: SinchClient?) {
        }

        override fun onRegistrationCredentialsRequired(p0: SinchClient?, p1: ClientRegistration?) {
        }

        override fun onLogMessage(p0: Int, p1: String?, p2: String?) {
        }

        override fun onClientFailed(p0: SinchClient?, p1: SinchError?) {
            if(mListener!=null){
                mListener.onStartFailed(p1)
            }
            client.terminate()

        }
    }

  interface StartFailedListener {
      fun onStartFailed(error: SinchError?)

      fun onStarted()
  }

    inner class  SinchCallClientListener : CallClientListener{
        override fun onIncomingCall(p0: CallClient?, call: Call?) {
           Intent(this@SinchService, IncomingCallActivity::class.java).also {
                it.putExtra(CALL_ID,call!!.callId)
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
            }
        }
    }


}