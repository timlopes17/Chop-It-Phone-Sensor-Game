package edu.temple.chopitgame

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.metrics.Event
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        var background = MediaPlayer.create(this, R.raw.background)
        background.start()
        var timeBetween : Long = 3000

        var soundPool : SoundPool = SoundPool.Builder().build()

        val events = arrayOf(
            EventObject("Click It", soundPool.load(baseContext, R.raw.click, 1)),
            EventObject("Chop It", soundPool.load(baseContext, R.raw.chop, 1)),
            EventObject("Answer It", soundPool.load(baseContext, R.raw.answer, 1)),
            EventObject("Flip It", soundPool.load(baseContext, R.raw.flip, 1)),
            EventObject("Twist It", soundPool.load(baseContext, R.raw.twist, 1)),
            EventObject("Spin It", soundPool.load(baseContext, R.raw.spin, 1)),
        )

        var event : EventObject
        var speed : Float

        // 3000 -> 1500
        // 1 -> 1.2
        // y = 600/x + 0.8
        thread(start = true){
            for(n in 0..49){
                speed = 600F/timeBetween + 0.8F
                event = events[Random.nextInt(0, 6)]
                soundPool?.play(event.soundId, 1F, 1F, 0, 0, speed)
                Log.d("Game", event.title)
                Log.d("Game", "Speed: $speed")
                Log.d("Game", "TimeBetween: $timeBetween")
                Thread.sleep(timeBetween)
                timeBetween -= 10
            }
        }

    }
}