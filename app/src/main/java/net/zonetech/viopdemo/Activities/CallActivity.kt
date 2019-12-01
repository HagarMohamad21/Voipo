 package net.zonetech.viopdemo.Activities

import android.os.Bundle
import com.sinch.android.rtc.SinchClient
import kotlinx.android.synthetic.main.activity_call.*
import net.zonetech.viopdemo.R
import net.zonetech.viopdemo.Sinch.Client

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
                client.callClient.callUser(userName)
            }

        }
    }
    private fun  getClient(name:String) {
        client= Client(this).getClient(name)
        client.setSupportCalling(true)
        client.start()
    }
}
