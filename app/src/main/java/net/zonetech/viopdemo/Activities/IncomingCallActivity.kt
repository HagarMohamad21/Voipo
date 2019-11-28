package net.zonetech.viopdemo.Activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sinch.android.rtc.calling.Call
import kotlinx.android.synthetic.main.activity_incoming_call.*
import net.zonetech.viopdemo.R
import net.zonetech.viopdemo.Utils.Common

class IncomingCallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)
        initViews()

    }

    private fun initViews() {
        var callerName=intent.getStringExtra(Common.CALL_ID)
        callerNameTxt.text=callerName
    }
}
