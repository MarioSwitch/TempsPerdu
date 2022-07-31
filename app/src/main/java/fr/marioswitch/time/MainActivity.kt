package fr.marioswitch.time

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Récupération de la sauvegarde
        var save = getSharedPreferences("fr.marioswitch.time",Context.MODE_PRIVATE)
        var totalSeconds = save.getInt("total", 0)


        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                totalSeconds++
                save.edit().putInt("total",totalSeconds)
                var seconds:Int = totalSeconds.rem(60)
                var minutes:Int = (totalSeconds/60).rem(60)
                var hours:Int = (totalSeconds/3600).rem(24)
                var days:Int = totalSeconds/86400
                when(totalSeconds){
                    in 0..59 -> time.text = buildString { append(seconds); append("s") };
                    in 60..3599 -> time.text = buildString { append(minutes); append("m "); append(seconds); append("s") }
                    in 3600..86399 -> time.text = buildString { append(hours); append("h "); append(minutes); append("m "); append(seconds); append("s") }
                    else -> time.text = buildString { append(days); append("j "); append(hours); append("h "); append(minutes); append("m "); append(seconds); append("s") }
                }
                if(totalSeconds<60){
                    time_seconds.text = ""
                }else{
                    time_seconds.text = buildString { append("soit "); append(totalSeconds); append(" secondes") }
                }
                var nowLevel:Int
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
                var nextGoal:Int
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
                    0 -> level.text = "0/10"
                    1 -> level.text = "1/10"
                    2 -> level.text = "2/10"
                    3 -> level.text = "3/10"
                    4 -> level.text = "4/10"
                    5 -> level.text = "5/10"
                    6 -> level.text = "6/10"
                    7 -> level.text = "7/10"
                    8 -> level.text = "8/10"
                    9 -> level.text = "9/10"
                    10 -> level.text = "10/10"
                    in 11..14 -> level.text = ""
                }
                when(nowLevel){
                    0 -> nextlevel.text = "1 minute"
                    1 -> nextlevel.text = "5 minutes"
                    2 -> nextlevel.text = "10 minutes"
                    3 -> nextlevel.text = "30 minutes"
                    4 -> nextlevel.text = "1 heure"
                    5 -> nextlevel.text = "2 heures"
                    6 -> nextlevel.text = "4 heures"
                    7 -> nextlevel.text = "7 heures"
                    8 -> nextlevel.text = "12 heures"
                    9 -> nextlevel.text = "1 jour"
                    10 -> nextlevel.text = "3 jours"
                    11 -> nextlevel.text = "7 jours"
                    12 -> nextlevel.text = "14 jours"
                    13 -> nextlevel.text = "30 jours"
                    14 -> nextlevel.text = "Aucun"
                }
                when(nowLevel){
                    in 5..9 -> completion.text = buildString { append(totalSeconds*100/nextGoal); append(" %") }
                    in 10..13 -> completion.text = buildString { append(totalSeconds*1000/nextGoal); append(" ‰") }
                    else -> completion.text = ""
                }
                when(nowLevel){
                    in 11..14 -> level_title.text = ""
                }
                mainHandler.postDelayed(this, 1000)
            }
        })
    }
}