package edu.temple.chopitgame

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<ImageView>(R.id.button)
        val layout = findViewById<View>(R.id.layout)
        val butWidth = button.layoutParams.width
        val butHeight = button.layoutParams.height
        val title = findViewById<TextView>(R.id.titleText)
        var taps = 0

        val launchIntent = Intent(this, GameActivity::class.java)
        val sharedPref = this.getSharedPreferences("HIGHSCORE", MODE_PRIVATE) ?: return

        button.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        button.layoutParams.height = butHeight - 30
                        button.layoutParams.width = butWidth - 30
                        button.requestLayout()
                        button.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        button.layoutParams.height = butHeight
                        button.layoutParams.width = butWidth
                        button.requestLayout()
                        button.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        // start your next activity
                        startActivity(launchIntent)
                    }
                }
                return false
            }
        })

        var myToast: Toast? = null;

        title.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if(taps != 4) {
                            myToast?.cancel()
                            taps += 1
                            myToast = Toast.makeText(
                                getApplicationContext(), //Context
                                "Tap ${5 - taps} more times to reset high score", // Message to display
                                Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                            )
                            myToast?.show()//Finally Show the toast
                        }
                        else{
                            myToast?.cancel()
                            taps = 0
                            myToast = Toast.makeText(
                                getApplicationContext(), //Context
                                "High Score Was Reset", // Message to display
                                Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                            )
                            myToast?.show()//Finally Show the toast
                            with(sharedPref.edit()) {
                                putInt(getString(edu.temple.chopitgame.R.string.saved_high_score_key), 0)
                                apply()
                            }
                        }
                        return true
                    }

                }
                return false
            }
        })
    }
}