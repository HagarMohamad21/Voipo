 package net.zonetech.viopdemo.Activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.sinch.android.rtc.MissingPermissionException
import com.sinch.android.rtc.SinchClient
import kotlinx.android.synthetic.main.activity_call.*
import net.zonetech.viopdemo.R
import net.zonetech.viopdemo.Sinch.Client
import net.zonetech.viopdemo.Utils.Common

 const val REQUEST_CODE=2019
class CallActivity :BaseActivity() {

   private lateinit var client:SinchClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        callBtn.isEnabled=false
        setListeners()

    }

    override fun onServiceConnected() {
        callBtn.isEnabled=true
        var name=getSinchServiceInterface()?.userName
        userNameTxt.text = name
        getClient(name!!)


    }

    private fun setListeners() {
        callBtn.setOnClickListener {
            if(!nameEditTxt.text.isNullOrEmpty()){
                var userName=nameEditTxt.text.toString()
                try{
                    var call=  client.callClient.callUser(userName)
                    if(call==null) return@setOnClickListener
                    var callId=call.callId
                    Intent(this,AnsweredCallActivity()::class.java).also {
                        it.putExtra(Common.CALL_ID,callId)
                        startActivity(it)
                    }
                }
                catch (e:MissingPermissionException){
                    ActivityCompat.requestPermissions(this, arrayOf(e.requiredPermission), 0)

                }
            }

        }
    }
    private fun  getClient(name:String) {
        client= Client(this).getClient(name)
        client.setSupportCalling(true)
        client.start()
    }
}
