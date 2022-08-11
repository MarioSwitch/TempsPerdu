package fr.marioswitch.time

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.Lifecycle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val save = getSharedPreferences("fr.marioswitch.time",Context.MODE_PRIVATE)
        var totalSeconds = save.getInt("total", 0)

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)){
                    totalSeconds++
                    save.edit().putInt("total",totalSeconds).apply()
                    val seconds:Int = totalSeconds.rem(60)
                    val minutes:Int = (totalSeconds/60).rem(60)
                    val hours:Int = (totalSeconds/3600).rem(24)
                    val days:Int = totalSeconds/86400
                    when(totalSeconds){
                        in 0..59 -> time.text = buildString { append(seconds); append(getString(R.string.second)) }
                        in 60..3599 -> time.text = buildString { append(minutes); append(getString(R.string.minute)); append(seconds); append(getString(R.string.second)) }
                        in 3600..86399 -> time.text = buildString { append(hours); append(getString(R.string.hour)); append(minutes); append(getString(R.string.minute)); append(seconds); append(getString(R.string.second)) }
                        else -> time.text = buildString { append(days); append(getString(R.string.day)); append(hours); append(getString(R.string.hour)); append(minutes); append(getString(R.string.minute)); append(seconds); append(getString(R.string.second)) }
                    }
                    if(totalSeconds<60){
                        time_seconds.text = ""
                    }else{
                        time_seconds.text = String.format(getString(R.string.time_seconds),totalSeconds)
                    }
                    val nowLevel:Int
                    when(totalSeconds){
                        in 0..59 -> nowLevel = 0
                        in 60..299 -> nowLevel = 1
                        in 300..599 -> nowLevel = 2
                        in 600..1799 -> nowLevel = 3
                        in 1800..3599 -> nowLevel = 4
                        in 3600..7199 -> nowLevel = 5
                        in 7200..14399 -> nowLevel = 6
                        in 14400..25199 -> nowLevel = 7
                        in 25200..43199 -> nowLevel = 8
                        in 43200..86399 -> nowLevel = 9
                        in 86400..259199 -> nowLevel = 10
                        in 259200..604799 -> nowLevel = 11
                        in 604800..1209599 -> nowLevel = 12
                        in 1209600..2591999 -> nowLevel = 13
                        else -> nowLevel = 14
                    }
                    val nextGoal:Int
                    when(nowLevel){
                        0 -> nextGoal = 60
                        1 -> nextGoal = 300
                        2 -> nextGoal = 600
                        3 -> nextGoal = 1800
                        4 -> nextGoal = 3600
                        5 -> nextGoal = 7200
                        6 -> nextGoal = 14400
                        7 -> nextGoal = 25200
                        8 -> nextGoal = 43200
                        9 -> nextGoal = 86400
                        10 -> nextGoal = 259200
                        11 -> nextGoal = 604800
                        12 -> nextGoal = 1209600
                        13 -> nextGoal = 2592000
                        else -> nextGoal = 0
                    }
                    when(nowLevel){
                        in 0..10 -> level.text = String.format(getString(R.string.level),nowLevel)
                        in 11..14 -> level.text = ""
                    }
                    when(nowLevel){
                        11 -> bronze_icon.visibility = View.VISIBLE
                        else -> bronze_icon.visibility = View.INVISIBLE
                    }
                    when(nowLevel){
                        12 -> silver_icon.visibility = View.VISIBLE
                        else -> silver_icon.visibility = View.INVISIBLE
                    }
                    when(nowLevel){
                        13 -> gold_icon.visibility = View.VISIBLE
                        else -> gold_icon.visibility = View.INVISIBLE
                    }
                    when(nowLevel){
                        14 -> diamond_icon.visibility = View.VISIBLE
                        else -> diamond_icon.visibility = View.INVISIBLE
                    }
                    when(nowLevel){
                        0 -> nextlevel.text = getString(R.string.nextlevel0)
                        1 -> nextlevel.text = getString(R.string.nextlevel1)
                        2 -> nextlevel.text = getString(R.string.nextlevel2)
                        3 -> nextlevel.text = getString(R.string.nextlevel3)
                        4 -> nextlevel.text = getString(R.string.nextlevel4)
                        5 -> nextlevel.text = getString(R.string.nextlevel5)
                        6 -> nextlevel.text = getString(R.string.nextlevel6)
                        7 -> nextlevel.text = getString(R.string.nextlevel7)
                        8 -> nextlevel.text = getString(R.string.nextlevel8)
                        9 -> nextlevel.text = getString(R.string.nextlevel9)
                        10 -> nextlevel.text = getString(R.string.nextlevel10)
                        11 -> nextlevel.text = getString(R.string.nextlevel11)
                        12 -> nextlevel.text = getString(R.string.nextlevel12)
                        13 -> nextlevel.text = getString(R.string.nextlevel13)
                        14 -> nextlevel.text = getString(R.string.nextlevel14)
                    }
                    when(nowLevel){
                        in 5..9 -> completion.text = buildString { append(totalSeconds*100/nextGoal); append(" %") }
                        in 10..13 -> completion.text = buildString { append(totalSeconds*1000/nextGoal); append(" ‰") }
                        else -> completion.text = ""
                    }
                    when(nowLevel){
                        in 11..14 -> level_title.text = ""
                    }
                }
                mainHandler.postDelayed(this, 1000)
            }
        })
    }
}