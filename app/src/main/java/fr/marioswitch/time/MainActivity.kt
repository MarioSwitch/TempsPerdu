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
import fr.marioswitch.time.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        PlayGamesSdk.initialize(this)
        val gamesSignInClient = PlayGames.getGamesSignInClient(this)
        fun connectedMode(){
            binding.status.text = getString(R.string.connected)
            binding.button.text = getString(R.string.leaderboard)
            binding.button.visibility=View.VISIBLE
            binding.button.setOnClickListener{
                PlayGames.getLeaderboardsClient(this).getLeaderboardIntent(getString(R.string.leaderboard_id)).addOnSuccessListener { intent -> startActivityForResult(intent, 9004) }
            }
        }
        fun disconnectedMode(){
            binding.status.text = getString(R.string.disconnected)
            binding.button.visibility=View.INVISIBLE
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
                    save.edit().putLong("total",totalSeconds).apply()
                    val seconds:Int = totalSeconds.rem(60).toInt()
                    val minutes:Int = (totalSeconds/60).rem(60).toInt()
                    val hours:Int = (totalSeconds/3600).rem(24).toInt()
                    val days:Int = (totalSeconds/86400).toInt()
                    when(totalSeconds){
                        in 0..59 -> binding.time.text = buildString { append(seconds); append(getString(R.string.second)) }
                        in 60..3599 -> binding.time.text = buildString { append(minutes); append(getString(R.string.minute)); append("%02d".format(seconds)); append(getString(R.string.second)) }
                        in 3600..86399 -> binding.time.text = buildString { append(hours); append(getString(R.string.hour)); append("%02d".format(minutes)); append(getString(R.string.minute)); append("%02d".format(seconds)); append(getString(R.string.second)) }
                        else -> binding.time.text = buildString { append(days); append(getString(R.string.day)); append("%02d".format(hours)); append(getString(R.string.hour)); append("%02d".format(minutes)); append(getString(R.string.minute)); append("%02d".format(seconds)); append(getString(R.string.second)) }
                    }
                    val goals = arrayOf(0, 60, 300, 600, 1800, 3600, 7200, 14400, 25200, 43200, 86400, 259200, 604800, 1209600, 2592000)
                    val level_times = arrayOf(R.string.level0,R.string.level1,R.string.level2,R.string.level3,R.string.level4,R.string.level5,R.string.level6,R.string.level7,R.string.level8,R.string.level9,R.string.level10,R.string.level11,R.string.level12,R.string.level13,R.string.level14)
                    val achievementsIDs = arrayOf(0,R.string.achievement01_id,R.string.achievement02_id,R.string.achievement03_id,R.string.achievement04_id,R.string.achievement05_id,R.string.achievement06_id,R.string.achievement07_id,R.string.achievement08_id,R.string.achievement09_id,R.string.achievement10_id,R.string.achievement11_id,R.string.achievement12_id,R.string.achievement13_id,R.string.achievement14_id)
                    fun getLevel(): Int {
                        for(i in goals.indices){
                            if(totalSeconds<goals[i]){
                                return i-1
                            }
                        }
                        return goals.size-1
                    }
                    fun getLevelString(offset:Int): String {
                        return when(val levelWithOffset=getLevel()+offset){
                            in 0..10 -> levelWithOffset.toString()
                            11 -> "🥉"
                            12 -> "🥈"
                            13 -> "🥇"
                            14 -> "💎"
                            else -> ""
                        }
                    }
                    if(getLevel()<14){
                        fun getTimeLeft(): String{
                            val secondsLeft = goals[getLevel()+1] - totalSeconds
                            val secondsToShow = secondsLeft.rem(60)
                            val minutesToShow = (secondsLeft/60).rem(60)
                            val hoursToShow = (secondsLeft/3600)
                            val daysToShow = secondsLeft/86400
                            when(secondsLeft){
                                in 0..59 -> return buildString { append(secondsToShow); append("\"") }
                                in 60..3599 -> return buildString { append(minutesToShow); append("\' "); append("%02d".format(secondsToShow)); append("\"") }
                                in 3600..359999 -> return buildString { append(hoursToShow); append(":"); append("%02d".format(minutesToShow)) }
                                else -> return  buildString { append(daysToShow); append(" "); append(getString(R.string.days)) }
                            }
                        }
                        binding.levelNow.text = getLevelString(0)
                        binding.levelNext.text = getLevelString(1)
                        binding.timeNow.text = getString(level_times[getLevel()])
                        binding.timeNext.text = getString(level_times[getLevel()+1])
                        binding.timeLeft.text = String.format(getString(R.string.timeLeft), getTimeLeft())
                        binding.progressBar.min = goals[getLevel()]
                        binding.progressBar.max = goals[getLevel()+1]
                        binding.progressBar.progress = totalSeconds.toInt()
                        binding.congratulations.visibility = View.INVISIBLE
                    }else{
                        binding.levelNow.visibility = View.INVISIBLE
                        binding.levelNext.visibility = View.INVISIBLE
                        binding.timeNow.visibility = View.INVISIBLE
                        binding.timeNext.visibility = View.INVISIBLE
                        binding.timeLeft.visibility = View.INVISIBLE
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.congratulations.visibility = View.VISIBLE
                    }
                    if(totalSeconds.rem(60).toInt()==0){
                        for(i in 1..14){
                            if(totalSeconds>=goals[i]){
                                PlayGames.getAchievementsClient(this@MainActivity).unlock(getString(achievementsIDs[i]))
                            }
                        }
                        PlayGames.getLeaderboardsClient(this@MainActivity).submitScore(getString(R.string.leaderboard_id), totalSeconds*1000)
                    }
                }
                mainHandler.postDelayed(this, 1000)
            }
        })
    }
}
