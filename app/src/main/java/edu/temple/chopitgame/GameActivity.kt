package edu.temple.chopitgame

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.metrics.Event
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread
import kotlin.random.Random
import android.os.Looper
import android.util.TypedValue
import org.w3c.dom.Text
import kotlin.math.roundToInt


class GameActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccel : Sensor
    private lateinit var sensorMagnet : Sensor

    private var accelData = FloatArray(3)
    private var magnetData = FloatArray(3)

    private lateinit var pitchText : TextView
    private lateinit var azimuthText : TextView
    private lateinit var rollText : TextView

    private val drift = 0.05f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val button = findViewById<ImageView>(R.id.button);
        val layout = findViewById<View>(R.id.layout)
        val butWidth = button.layoutParams.width
        val butHeight = button.layoutParams.height
        val text = findViewById<TextView>(R.id.text)
        val score = findViewById<TextView>(R.id.score)

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
        var size : Float = 80F

        sensorManager = getSystemService((Context.SENSOR_SERVICE)) as SensorManager
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        azimuthText = findViewById(R.id.a_val)
        pitchText = findViewById(R.id.p_val)
        rollText = findViewById(R.id.r_val)

        thread(start = true){
            Thread.sleep(500)
            for(n in 0..49){
                speed = 600F/timeBetween + 0.8F
                event = events[Random.nextInt(0, 6)]
                size += 1F
                Handler(Looper.getMainLooper()).post(Runnable {
                    text.text = event.title.toString()
                    score.text = n.toString()
                    score.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
                })
                soundPool?.play(event.soundId, 1F, 1F, 0, 0, speed)
                //Log.d("Game", event.title)
                //Log.d("Game", "Speed: $speed")
                //Log.d("Game", "TimeBetween: $timeBetween")
                //Log.d("Game", "Textsize: ${score.textSize}")
                Thread.sleep(500)
                event = events[3]
                if(event.title == "Flip It")
                {

                }
                Thread.sleep(timeBetween - 500)
                timeBetween -= 10
            }
        }
    }

    override fun onStart() {
        super.onStart()
        sensorManager.registerListener(this, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStop() {
        super.onStop()

        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var sensorType = event?.sensor?.type

        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelData = event!!.values.clone()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magnetData = event!!.values.clone()
            }
            else -> {
                return
            }
        }

        var rotationMatrix = FloatArray(9)
        var rotOK = SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData)
        var orientationValues = FloatArray(3)

        if(rotOK)
            SensorManager.getOrientation(rotationMatrix, orientationValues)

        var azimuth = orientationValues[0]
        var pitch = orientationValues[1]
        var roll = orientationValues[2]

        azimuthText.text = azimuth.toString()
        pitchText.text = pitch.toString()
        rollText.text = roll.toString()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}