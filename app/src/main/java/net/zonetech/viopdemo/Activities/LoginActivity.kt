package net.zonetech.viopdemo.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.sinch.android.rtc.SinchError
import kotlinx.android.synthetic.main.activity_login.*
import net.zonetech.viopdemo.R
import net.zonetech.viopdemo.Sinch.SinchService

class LoginActivity : BaseActivity() ,SinchService.StartFailedListener{
    private val TAG = "LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginBtn.isEnabled=false
        setListeners()
    }

    private fun setListeners() {
        loginBtn.setOnClickListener {
            Log.d(TAG, "setListeners: ")
            if(!nameEditTxt.text.isNullOrEmpty()){
                var userName=nameEditTxt.text.toString()
                loginClicked(userName)
        }
            else{
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show()
            }
    }}

    override fun onServiceConnected() {
        loginBtn.isEnabled = true
        getSinchServiceInterface()!!.setStartListener(this)
    }



    override fun onStartFailed(error: SinchError?) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onStarted() {
        openPlaceCallActivity()
    }


    private fun loginClicked(userName: String) {

        if (!getSinchServiceInterface()!!.isStarted) {
            getSinchServiceInterface()!!.startClient(userName)
        } else {
            openPlaceCallActivity()
        }
    }

    private fun openPlaceCallActivity() {
        val mainActivity = Intent(this, CallActivity::class.java)
        startActivity(mainActivity)
    }


}
