package fr.marioswitch.time

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.games.AuthenticationResult
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayGamesSdk.initialize(this)
        val gamesSignInClient = PlayGames.getGamesSignInClient(this)
        fun connectedMode(){
            status.text = getString(R.string.connected)
            button.text = getString(R.string.leaderboard)
            button.visibility=View.VISIBLE
            button.setOnClickListener{
                PlayGames.getLeaderboardsClient(this).getLeaderboardIntent(getString(R.string.leaderboard_id)).addOnSuccessListener { intent -> startActivityForResult(intent, 9004) }
            }
        }
        fun disconnectedMode(){
            status.text = getString(R.string.disconnected)
            button.visibility=View.INVISIBLE
        }
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask: Task<AuthenticationResult> ->
            if (!isAuthenticatedTask.isSuccessful) {
                disconnectedMode()
                return@addOnCompleteListener
            }
            val authenticationResult =
                isAuthenticatedTask.result
            if (!authenticationResult.isAuthenticated) {
                disconnectedMode()
                return@addOnCompleteListener
            }
            connectedMode()
        }
        setContentView(R.layout.activity_main)
        val save = getSharedPreferences("fr.marioswitch.time",Context.MODE_PRIVATE)
        var totalSeconds = try {
            save.getLong("total", 0)
        }catch(e:ClassCastException){
            save.getInt("total", 0).toLong()
        }

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)){
                    totalSeconds++
                    if(totalSeconds.rem(60).toInt()==0){
                        PlayGames.getLeaderboardsClient(this@MainActivity).submitScore(getString(R.string.leaderboard_id), totalSeconds*1000)
                    }
                    save.edit().putLong("total",totalSeconds).apply()
                    val seconds:Int = totalSeconds.rem(60).toInt()
                    val minutes:Int = (totalSeconds/60).rem(60).toInt()
                    val hours:Int = (totalSeconds/3600).rem(24).toInt()
                    val days:Int = (totalSeconds/86400).toInt()
                    when(totalSeconds){
                        in 0..59 -> time.text = buildString { append(seconds); append(getString(R.string.second)) }
                        in 60..3599 -> time.text = buildString { append(minutes); append(getString(R.string.minute)); append("%02d".format(seconds)); append(getString(R.string.second)) }
                        in 3600..86399 -> time.text = buildString { append(hours); append(getString(R.string.hour)); append("%02d".format(minutes)); append(getString(R.string.minute)); append("%02d".format(seconds)); append(getString(R.string.second)) }
                        else -> time.text = buildString { append(days); append(getString(R.string.day)); append("%02d".format(hours)); append(getString(R.string.hour)); append("%02d".format(minutes)); append(getString(R.string.minute)); append("%02d".format(seconds)); append(getString(R.string.second)) }
                    }
                    if(totalSeconds<60){
                        time_seconds.text = ""
                    }else{
                        time_seconds.text = String.format(getString(R.string.time_seconds),totalSeconds)
                    }
                    val goals = arrayOf(0, 60, 300, 600, 1800, 3600, 7200, 14400, 25200, 43200, 86400, 259200, 604800, 1209600, 2592000)
                    val nextLevels = arrayOf(R.string.nextlevel0,R.string.nextlevel1,R.string.nextlevel2,R.string.nextlevel3,R.string.nextlevel4,R.string.nextlevel5,R.string.nextlevel6,R.string.nextlevel7,R.string.nextlevel8,R.string.nextlevel9,R.string.nextlevel10,R.string.nextlevel11,R.string.nextlevel12,R.string.nextlevel13,R.string.nextlevel14)
                    val achievementsIDs = arrayOf(0,R.string.achievement01_id,R.string.achievement02_id,R.string.achievement03_id,R.string.achievement04_id,R.string.achievement05_id,R.string.achievement06_id,R.string.achievement07_id,R.string.achievement08_id,R.string.achievement09_id,R.string.achievement10_id,R.string.achievement11_id,R.string.achievement12_id,R.string.achievement13_id,R.string.achievement14_id)
                    fun getLevel(seconds:Long): Int {
                        for(i in goals.indices){
                            if(seconds<goals[i]){
                                return i-1
                            }
                        }
                        return goals.size-1
                    }
                    when(getLevel(totalSeconds)){
                        in 0..10 -> level.text = String.format(getString(R.string.level),getLevel(totalSeconds))
                        in 11..14 -> level.text = ""
                    }
                    when(getLevel(totalSeconds)){
                        11 -> bronze_icon.visibility = View.VISIBLE
                        else -> bronze_icon.visibility = View.INVISIBLE
                    }
                    when(getLevel(totalSeconds)){
                        12 -> silver_icon.visibility = View.VISIBLE
                        else -> silver_icon.visibility = View.INVISIBLE
                    }
                    when(getLevel(totalSeconds)){
                        13 -> gold_icon.visibility = View.VISIBLE
                        else -> gold_icon.visibility = View.INVISIBLE
                    }
                    when(getLevel(totalSeconds)){
                        14 -> diamond_icon.visibility = View.VISIBLE
                        else -> diamond_icon.visibility = View.INVISIBLE
                    }
                    for(i in 0..14){
                        if(getLevel(totalSeconds)==i){
                            nextlevel.text = getString(nextLevels[i])
                            break
                        }
                    }
                    when(getLevel(totalSeconds)){
                        in 5..9 -> completion.text = buildString { append(totalSeconds*100/goals[getLevel(totalSeconds)+1]); append(" %") }
                        in 10..13 -> completion.text = buildString { append(totalSeconds*1000/goals[getLevel(totalSeconds)+1]); append(" ‰") }
                        else -> completion.text = ""
                    }
                    when(getLevel(totalSeconds)){
                        in 11..14 -> level_title.text = ""
                    }
                    for(i in 1..14){
                        if(totalSeconds>=goals[i]){
                            PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(achievementsIDs[i]))
                        }
                    }
                }
                mainHandler.postDelayed(this, 1000)
            }
        })
    }
}
