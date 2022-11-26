package edu.temple.chopitgame

import android.annotation.SuppressLint
import android.content.Intent
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlin.concurrent.thread

class WinActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win)

        val tryAgain = findViewById<ImageView>(R.id.tryAgain)
        val layout = findViewById<View>(R.id.layout)
        val butWidth = tryAgain.layoutParams.width
        val butHeight = tryAgain.layoutParams.height
        val score = findViewById<TextView>(R.id.scoreText)
        val highscore = findViewById<TextView>(R.id.highscoreText)
        val mainMenu = findViewById<Button>(R.id.mainMenu)
        val hs_img = findViewById<ImageView>(R.id.highScore)

        hs_img.visibility = View.INVISIBLE

        val myScore = intent.getIntExtra("score", -1)

        var sPool: SoundPool = SoundPool.Builder().build()
        var sound = sPool.load(baseContext, R.raw.win, 1)

        thread(start = true) {
            Thread.sleep(100)
            sPool.play(sound, 0.5F, 0.5F, 0, 0, 1F)
        }

        score.text = "Score: $myScore"
        val sharedPref = this.getSharedPreferences("HIGHSCORE", MODE_PRIVATE) ?: return
        val hs = sharedPref.getInt(getString(R.string.saved_high_score_key), 0)
        if(myScore > hs) {
            with(sharedPref.edit()) {
                putInt(getString(R.string.saved_high_score_key), myScore)
                apply()
            }
            hs_img.visibility = View.VISIBLE
        }
        highscore.text = "High Score: " + sharedPref.getInt(getString(R.string.saved_high_score_key), 0).toString()

        val again = Intent(this, GameActivity::class.java)
        val mm = Intent(this, MainActivity::class.java)

        mainMenu.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                startActivity(mm)
            }
        })

        tryAgain.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        tryAgain.layoutParams.height = butHeight - 30
                        tryAgain.layoutParams.width = butWidth - 30
                        tryAgain.requestLayout()
                        tryAgain.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        tryAgain.layoutParams.height = butHeight
                        tryAgain.layoutParams.width = butWidth
                        tryAgain.requestLayout()
                        tryAgain.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        startActivity(again)
                    }
                }
                return false
            }
        })
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val launchIntent = Intent(this, MainActivity::class.java)
        startActivity(launchIntent)
    }
}