package edu.temple.chopitgame

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<ImageView>(R.id.button);
        val layout = findViewById<View>(R.id.layout)
        val butWidth = button.layoutParams.width
        val butHeight = button.layoutParams.height

        button.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        button.layoutParams.height = butHeight - 30
                        button.layoutParams.width = butWidth - 30
                        button.requestLayout()
                        button.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        Log.d("Test", "DOWN")
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        button.layoutParams.height = butHeight
                        button.layoutParams.width = butWidth
                        button.requestLayout()
                        button.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        Log.d("Test", "UP")
                    }
                }
                return false
            }
        })
    }
}