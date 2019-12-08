package net.zonetech.viopdemo.Sinch

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import com.sinch.android.rtc.*
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallClient
import com.sinch.android.rtc.calling.CallClientListener
import com.sinch.android.rtc.video.VideoController
import net.zonetech.viopdemo.Activities.IncomingCallActivity
import net.zonetech.viopdemo.Utils.Common.Companion.CALL_ID
import net.zonetech.viopdemo.Utils.Common.Companion.MESSAGE_PERMISSIONS_NEEDED
import net.zonetech.viopdemo.Utils.Common.Companion.MESSENGER
import net.zonetech.viopdemo.Utils.Common.Companion.REQUIRED_PERMISSION

class SinchService :Service() {
    private val APP_KEY = "b9ea54f0-21dc-4f9e-b435-c312f2e14c38"
      private val APP_SECRET = "5nLsTY7IqkSzBAYdzXL+Bg=="
    private val ENVIRONMENT = "clientapi.sinch.com"
    private var messenger: Messenger? = null
    private val TAG = "SinchService"
    private val mSinchServiceInterface = SinchServiceInterface()
    private var mSinchClient: SinchClient? = null
    private var mUserId: String? = null

    private var mListener: StartFailedListener? = null
    private var mSettings: PersistedSettings? = null

    override fun onCreate() {
        super.onCreate()
        mSettings = PersistedSettings(applicationContext)
        attemptAutoStart()
    }

    private fun attemptAutoStart() {
        val userName = mSettings!!.username!!
        Log.d(TAG, "attemptAutoStart: "+userName)
        if (!userName.isEmpty() && messenger != null) {
            start(userName)
        }
    }

    override fun onDestroy() {
        if (mSinchClient != null && mSinchClient!!.isStarted) {
            mSinchClient!!.terminateGracefully()
        }
        super.onDestroy()
    }

    private fun start(userName: String) {
        var permissionsGranted = true
        if (mSinchClient == null) {
            mSettings!!.username = userName
            mUserId=userName
            createClient(userName)
        }
        try { //mandatory checks
            mSinchClient!!.checkManifest()
            // check for bluetooth for automatic audio routing
            if (baseContext.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "start: throw MissingPermissionException(Manifest.permission.BLUETOOTH) ")
                throw MissingPermissionException(Manifest.permission.BLUETOOTH)

            }
            //auxiliary check
            if (applicationContext.checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                throw MissingPermissionException(Manifest.permission.CAMERA)
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
                    messenger!!.send(message)
                } catch (e1: RemoteException) {
                    e1.printStackTrace()
                }
            }
        }
        if (permissionsGranted) {
            Log.d(TAG, "Starting SinchClient")
            mSinchClient!!.start()
        }
    }

    private fun createClient(userName: String) {
        mUserId = userName
        mSinchClient =
            Sinch.getSinchClientBuilder().context(applicationContext).userId(userName)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT).build()
        mSinchClient!!.setSupportCalling(true)
        mSinchClient!!.startListeningOnActiveConnection()
        mSinchClient!!.addSinchClientListener(MySinchClientListener())
        // Permission READ_PHONE_STATE is needed to respect native calls.
        mSinchClient!!.getCallClient().setRespectNativeCalls(false)
        mSinchClient!!.getCallClient().addCallClientListener(SinchCallClientListener())
    }

    private fun stop() {
        if (mSinchClient != null) {
            mSinchClient!!.terminateGracefully()
            mSinchClient = null
        }
        mSettings!!.username = ""
    }

    private fun isStarted(): Boolean {
        return mSinchClient != null && mSinchClient!!.isStarted
    }

    override fun onBind(intent: Intent): IBinder? {
        messenger = intent.getParcelableExtra(MESSENGER)
        return mSinchServiceInterface
    }

   inner class SinchServiceInterface : Binder() {
        fun callPhoneNumber(phoneNumber: String?): Call {
            return mSinchClient!!.getCallClient().callPhoneNumber(phoneNumber)
        }
       fun getVideoController(): VideoController? {
           return if (!isStarted()) {
               null
           } else mSinchClient!!.videoController
       }
        fun callUser(userId: String?): Call? {
            return if (mSinchClient == null) {
                null
            } else mSinchClient!!.callClient.callUser(userId)
        }
        fun callUserVideo(userId: String?):Call?{
            return if (mSinchClient == null) {
                null
            }
            else mSinchClient!!.callClient.callUserVideo(userId)
        }
        val userName: String
            get() = mUserId!!

        val isStarted: Boolean
            get() = this@SinchService.isStarted()

        fun retryStartAfterPermissionGranted() {
            this@SinchService.attemptAutoStart()
        }

        fun startClient(userName: String?) {
            start(userName!!)
        }

        fun stopClient() {
            stop()
        }

        fun setStartListener(listener: StartFailedListener) {
            mListener = listener
        }

        fun getCall(callId: String?): Call? {
            return if (mSinchClient != null) mSinchClient!!.getCallClient().getCall(callId) else null
        }

        val audioController: AudioController?
            get() = if (!isStarted) {
                null
            } else mSinchClient!!.getAudioController()
    }

    interface StartFailedListener {
        fun onStartFailed(error: SinchError?)
        fun onStarted()
    }

    inner class MySinchClientListener : SinchClientListener {
        private val TAG = "MySinchClientListener"
        override fun onClientFailed(client: SinchClient, error: SinchError) {
            mListener?.onStartFailed(error)
            mSinchClient!!.terminate()
            mSinchClient = null
        }

        override fun onClientStarted(client: SinchClient) {
            Log.d(TAG, "SinchClient started")
            mListener?.onStarted()
        }

        override fun onClientStopped(client: SinchClient) {
            Log.d(TAG, "SinchClient stopped")
        }

        override fun onLogMessage(
            level: Int,
            area: String,
            message: String
        ) {
            when (level) {
                Log.DEBUG -> Log.d(area, message)
                Log.ERROR -> Log.e(area, message)
                Log.INFO -> Log.i(area, message)
                Log.VERBOSE -> Log.v(area, message)
                Log.WARN -> Log.w(area, message)
            }
        }

        override fun onRegistrationCredentialsRequired(
            client: SinchClient,
            clientRegistration: ClientRegistration
        ) {
        }
    }

    inner class SinchCallClientListener :
        CallClientListener {
        override fun onIncomingCall(
            callClient: CallClient,
            call: Call
        ) {
            Log.d(TAG, "Incoming call")
            val intent = Intent(
                this@SinchService,
                IncomingCallActivity::class.java
            )
            intent.putExtra(CALL_ID, call.callId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this@SinchService.startActivity(intent)
        }
    }

    inner class PersistedSettings(context: Context) {
        private val mStore: SharedPreferences
        var username: String?
            get() = mStore.getString("Username", "")
            set(username) {
                val editor = mStore.edit()
                editor.putString("Username", username)
                editor.commit()
            }

        private  val PREF_KEY = "Sinch"
        init {
            mStore = context.getSharedPreferences(
                PREF_KEY,
                Context.MODE_PRIVATE
            )
        }
    }}