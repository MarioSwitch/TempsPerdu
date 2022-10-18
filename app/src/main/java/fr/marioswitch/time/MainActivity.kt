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
        fun PlayGamesButton(){
            status.text = getString(R.string.disconnected)
            button.visibility=View.VISIBLE
            button.setOnClickListener{
                status.text = getString(R.string.connecting)
                if(gamesSignInClient.signIn().isSuccessful) {
                    connectedMode()
                }
                else{
                    PlayGamesButton()
                }
            }
        }
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask: Task<AuthenticationResult> ->
            if (!isAuthenticatedTask.isSuccessful) {
                PlayGamesButton()
                return@addOnCompleteListener
            }
            val authenticationResult =
                isAuthenticatedTask.result
            if (!authenticationResult.isAuthenticated) {
                PlayGamesButton()
                return@addOnCompleteListener
            }
            connectedMode()
        }
        setContentView(R.layout.activity_main)
        val save = getSharedPreferences("fr.marioswitch.time",Context.MODE_PRIVATE)
        var totalSeconds = save.getInt("total", 0)

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)){
                    totalSeconds++
                    PlayGames.getLeaderboardsClient(this@MainActivity).submitScore(getString(R.string.leaderboard_id), (totalSeconds.toLong())*1000)
                    save.edit().putInt("total",totalSeconds).apply()
                    val seconds:Int = totalSeconds.rem(60)
                    val minutes:Int = (totalSeconds/60).rem(60)
                    val hours:Int = (totalSeconds/3600).rem(24)
                    val days:Int = totalSeconds/86400
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
                    fun getLevel(seconds:Int): Int {
                        for(i in 0..goals.size){
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
                    when(getLevel(totalSeconds)){
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
                    when(getLevel(totalSeconds)){
                        in 5..9 -> completion.text = buildString { append(totalSeconds*100/goals[getLevel(totalSeconds)+1]); append(" %") }
                        in 10..13 -> completion.text = buildString { append(totalSeconds*1000/goals[getLevel(totalSeconds)+1]); append(" ‰") }
                        else -> completion.text = ""
                    }
                    when(getLevel(totalSeconds)){
                        in 11..14 -> level_title.text = ""
                    }
                    if(totalSeconds>=goals[1]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement01_id))
                    if(totalSeconds>=goals[2]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement02_id))
                    if(totalSeconds>=goals[3]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement03_id))
                    if(totalSeconds>=goals[4]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement04_id))
                    if(totalSeconds>=goals[5]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement05_id))
                    if(totalSeconds>=goals[6]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement06_id))
                    if(totalSeconds>=goals[7]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement07_id))
                    if(totalSeconds>=goals[8]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement08_id))
                    if(totalSeconds>=goals[9]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement09_id))
                    if(totalSeconds>=goals[10]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement10_id))
                    if(totalSeconds>=goals[11]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement11_id))
                    if(totalSeconds>=goals[12]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement12_id))
                    if(totalSeconds>=goals[13]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement13_id))
                    if(totalSeconds>=goals[14]) PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(R.string.achievement14_id))
                }
                mainHandler.postDelayed(this, 1000)
            }
        })
    }
}

