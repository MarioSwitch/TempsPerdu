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
                mainHandler.postDelayed(this, 1000)
             }
         })
    }
}