package net.zonetech.viopdemo.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*
import net.zonetech.viopdemo.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setListeners()
    }

    private fun setListeners() {
        loginBtn.setOnClickListener {
            if(!nameEditTxt.text.isNullOrEmpty()){

            }
        }
    }
}
