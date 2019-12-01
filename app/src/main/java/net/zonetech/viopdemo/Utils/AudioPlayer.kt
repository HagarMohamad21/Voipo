package net.zonetech.viopdemo.Utils

import android.content.Context
import android.content.res.Resources
import android.media.AudioManager
import android.media.MediaPlayer
import android.widget.Toast
import net.zonetech.viopdemo.R
import java.lang.Exception

class AudioPlayer (var context: Context){
    var mediaPlayer:MediaPlayer?=null

    fun playRingTone(){
      var audioManager=context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when(audioManager.ringerMode){
            AudioManager.RINGER_MODE_NORMAL->{
                var resources:Resources=context.resources
                var assetFileDes=resources.openRawResourceFd(R.raw.ringtone)

                mediaPlayer= MediaPlayer()
                mediaPlayer?.reset()
                mediaPlayer?.setAudioStreamType(AudioManager.STREAM_RING)
                mediaPlayer?.isLooping=true

               try{
                   mediaPlayer?.setDataSource(assetFileDes.fileDescriptor,assetFileDes.startOffset,assetFileDes.declaredLength)
                   mediaPlayer?.prepare()
               }
               catch (e:Exception){
                   Toast.makeText(context,"Couldn't play sound",Toast.LENGTH_LONG).show()
               }
                mediaPlayer?.start()
            }
        }


    }
    fun stopRingTone(){
        if(mediaPlayer!=null){
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer=null
        }
    }

}