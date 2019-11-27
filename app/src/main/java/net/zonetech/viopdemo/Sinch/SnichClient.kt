package net.zonetech.viopdemo.Sinch

import android.content.Context
import com.sinch.android.rtc.Sinch
import com.sinch.android.rtc.SinchClient
import net.zonetech.viopdemo.Utils.Common

class Client(var context: Context) {


         fun getClient(userName:String):SinchClient{
          return Sinch.getSinchClientBuilder()
                .context(context)
                .userId(userName)
                .applicationKey(Common.APP_KEY)
                .applicationSecret(Common.APP_SECRET)
                .environmentHost(Common.ENVIRONMENT)
                .build()

        }



}