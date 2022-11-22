package edu.temple.chopitgame

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import android.view.MotionEvent
import org.w3c.dom.Text
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class GameActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccel : Sensor
    private lateinit var sensorMagnet : Sensor
    private lateinit var sensorLight : Sensor

    private var accelData = FloatArray(3)
    private var magnetData = FloatArray(3)
    private var lightData = FloatArray(1)

    private lateinit var pitchText : TextView
    private lateinit var azimuthText : TextView
    private lateinit var rollText : TextView
    private lateinit var lightText : TextView

    private var minPitch : Double = 180.0
    private var maxPitch : Double = -180.0
    private var minRoll : Double = 180.0
    private var maxRoll : Double = -180.0
    private var minAzimuth : Double = 180.0
    private var maxAzimuth : Double = -180.0

    private var record : Int = -1
    private var n: Int = 0
    private var size : Float = 80F
    private lateinit var soundPool : SoundPool
    private var correctID : Int = 0
    private lateinit var background : MediaPlayer
    private var doubleMove = 0
    private var myScore = 0

    private val drift = 0.05f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val button = findViewById<ImageView>(R.id.button);
        val layout = findViewById<View>(R.id.layout)
        val butWidth = button.layoutParams.width
        val butHeight = button.layoutParams.height
        val text = findViewById<TextView>(R.id.text)
        val score = findViewById<TextView>(R.id.score)

        background = MediaPlayer.create(this, R.raw.background)
        background.start()
        var timeBetween : Long = 3000

        soundPool = SoundPool.Builder().build()

        val launchIntent = Intent(this, LoseActivity::class.java)

        val events = arrayOf(
            EventObject("Click It", soundPool.load(baseContext, R.raw.click, 1)),
            EventObject("Chop It", soundPool.load(baseContext, R.raw.chop, 1)),
            EventObject("Answer It", soundPool.load(baseContext, R.raw.answer, 1)),
            EventObject("Flip It", soundPool.load(baseContext, R.raw.flip, 1)),
            EventObject("Twist It", soundPool.load(baseContext, R.raw.twist, 1)),
            EventObject("Spin It", soundPool.load(baseContext, R.raw.spin, 1)),
        )

        button.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        button.layoutParams.height = butHeight - 30
                        button.layoutParams.width = butWidth - 30
                        button.requestLayout()
                        button.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        if(record == 0){
                            correct()
                        }
                        else
                        {
                            launchIntent.putExtra("score", myScore)
                            startActivity(launchIntent)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        button.layoutParams.height = butHeight
                        button.layoutParams.width = butWidth
                        button.requestLayout()
                        button.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    }
                }
                return false
            }
        })

        correctID = soundPool.load(baseContext, R.raw.correct, 1)

        var event : EventObject
        var speed : Float

        sensorManager = getSystemService((Context.SENSOR_SERVICE)) as SensorManager
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        //Used for testing sensors
        azimuthText = findViewById(R.id.a_val)
        pitchText = findViewById(R.id.p_val)
        rollText = findViewById(R.id.r_val)
        lightText = findViewById(R.id.l_val)

        azimuthText.visibility = View.INVISIBLE
        pitchText.visibility = View.INVISIBLE
        rollText.visibility = View.INVISIBLE
        lightText.visibility = View.INVISIBLE

        var oldN : Int

        thread(start = true) {
            Thread.sleep(500)
            while (n != 50 && n != -17) {
                oldN = n
                speed = 600F / timeBetween + 0.8F
                event = events[2]
                //event = events[Random.nextInt(0, 6)]
                Handler(Looper.getMainLooper()).post(Runnable {
                    text.text = event.title.toString()
                })
                soundPool?.play(event.soundId, 1F, 1F, 0, 0, speed)
                //Log.d("Game", event.title)
                //Log.d("Game", "Speed: $speed")
                //Log.d("Game", "TimeBetween: $timeBetween")
                //Log.d("Game", "Textsize: ${score.textSize}")
                Thread.sleep(250)
                if (event.title == "Flip It" || event.title == "Chop It" ||
                    event.title == "Twist It" || event.title == "Spin It") {
                    sensorManager.registerListener(
                        this,
                        sensorAccel,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                    sensorManager.registerListener(
                        this,
                        sensorMagnet,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                    if(event.title == "Chop It")
                        record = 1
                    else if(event.title == "Flip It")
                        record = 3
                    else if(event.title == "Twist It")
                        record = 4
                    else
                        record = 5
                }
                else if(event.title == "Click It")
                    record = 0
                else {
                    sensorManager.registerListener(
                        this,
                        sensorLight,
                        SensorManager.SENSOR_DELAY_FASTEST
                    )
                    record = 2
                }
                Thread.sleep(2000 + timeBetween - 500)
                timeBetween -= 10
                if (n == oldN){
                    Log.d("Game", "YOU LOSE")
                    launchIntent.putExtra("score", myScore)
                    startActivity(launchIntent)
                    n = -17
                }
            }
            soundPool.release()
        }
    }

    fun correct() {
        val score = findViewById<TextView>(R.id.score)
        n += 1
        myScore = n
        size += 1F
        soundPool.play(correctID, 0.5F, 0.5F, 0, 0, 1F)
        Handler(Looper.getMainLooper()).post(Runnable {
            score.text = n.toString()
            score.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        })
        Log.d("Game", "CORRECT")
        record = -1
        minPitch = 180.0
        maxPitch = -180.0
        minRoll = 180.0
        maxRoll = -180.0
        minAzimuth = 180.0
        maxAzimuth = -180.0
    }

    fun lose() {
        val sPool : SoundPool = SoundPool.Builder().build()
        var sound = sPool.load(baseContext, R.raw.wrong, 1)
        sPool.play(sound, 1F, 1F, 0, 0, 1F)
        n = -17
        record = -1
        Log.d("Game", "LOSE")
    }

    override fun onStart() {
        super.onStart()
        sensorManager.registerListener(this, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorLight, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
        background.release()
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
            Sensor.TYPE_LIGHT -> {
                lightData = event!!.values.clone()
            }
            else -> {
                return
            }
        }
        if(record == 1 || record == 3 || record == 4 || record == 5)
        {
            var rotationMatrix = FloatArray(9)
            var rotOK = SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData)
            var orientationValues = FloatArray(3)

            if(rotOK)
                SensorManager.getOrientation(rotationMatrix, orientationValues)

            var azimuth = orientationValues[0] * 57.3
            var pitch = orientationValues[1] * 57.3
            var roll = orientationValues[2] * 57.3

            azimuthText.text = azimuth.toString()
            pitchText.text = pitch.toString()
            rollText.text = roll.toString()

            minPitch = min(pitch, minPitch)
            maxPitch = max(pitch, maxPitch)
            minRoll = min(roll, minRoll)
            maxRoll = max(roll, maxRoll)
            minAzimuth = min(azimuth, minAzimuth)
            maxAzimuth = max(azimuth, maxAzimuth)
            //Chop
            if(record == 1){
                if(doubleMove == 1 && maxRoll - minRoll >= 90 && maxAzimuth - minAzimuth >= 90){
                    correct()
                    doubleMove = 0
                }
                else if(maxRoll - minRoll >= 90 && maxAzimuth - minAzimuth >= 90){
                    doubleMove = 1
                    maxRoll = roll
                    minRoll = roll
                }
            }
            //Flip
            else if(record == 3){
                if(maxPitch - minPitch >= 60){
                    correct()
                }
            }
            //Twist
            else if(record == 4){
                if (maxRoll - minRoll >= 150){
                    correct()
                }
            }
            //Spin
            else if(record == 5){
                if (maxAzimuth - minAzimuth >= 80) {
                    correct()
                }
            }
        }
        else if(record == 2){
            //Light
            var lx = lightData[0]

            Log.d("GAME", "Light Updated $lx")

            lightText.text = lx.toString()
            if(lx < 1.0 && lx > 0.0){
                correct()
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}