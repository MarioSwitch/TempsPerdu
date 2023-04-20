package fr.marioswitch.time

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.games.AuthenticationResult
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.tasks.Task
import fr.marioswitch.time.databinding.ActivityMainBinding
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        //Starting code
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        PlayGamesSdk.initialize(this)
        val gamesSignInClient = PlayGames.getGamesSignInClient(this)
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask: Task<AuthenticationResult> ->
            if (!isAuthenticatedTask.isSuccessful) {
                Toast.makeText(this@MainActivity, getString(R.string.googleplay_failure), Toast.LENGTH_LONG).show()
                return@addOnCompleteListener
            }
            val authenticationResult = isAuthenticatedTask.result
            if (!authenticationResult.isAuthenticated) {
                Toast.makeText(this@MainActivity, getString(R.string.googleplay_failure),Toast.LENGTH_LONG).show()
                return@addOnCompleteListener
            }
            Toast.makeText(this@MainActivity, getString(R.string.googleplay_success),Toast.LENGTH_LONG).show()
        }
        val save = getSharedPreferences("fr.marioswitch.time",Context.MODE_PRIVATE)

        //Functions
        fun getLevel(value:Long, tab:Array<Int>): Int {
            for(i in tab.indices){
                if(value<tab[i]){
                    return i-1
                }
            }
            return tab.size-1
        }
        fun getLevelString(value:Long, tab:Array<Int>, offset:Int): String {
            return when(val levelWithOffset=getLevel(value,tab)+offset){
                in 0..10 -> levelWithOffset.toString()
                11 -> "🥉"
                12 -> "🥈"
                13 -> "🥇"
                14 -> "💎"
                else -> ""
            }
        }
        fun applyThousandSeparator(value:Long):String{
            val formatter = DecimalFormat("#,##0")
            formatter.decimalFormatSymbols = formatter.decimalFormatSymbols.apply { groupingSeparator = this@MainActivity.getString(R.string.thousand_separator).toCharArray()[0] }
            return formatter.format(value)
        }

        //Clicks code
        var totalClicks = save.getLong("clicks",0)
        val clicksGoals = arrayOf(0,10,100,200,500,1000,5000,20000,100000,300000,1000000,2000000,3000000,5000000,10000000)
        val clicksLevels = arrayOf(R.string.clicks_level_0,R.string.clicks_level_1,R.string.clicks_level_2,R.string.clicks_level_3,R.string.clicks_level_4,R.string.clicks_level_5,R.string.clicks_level_6,R.string.clicks_level_7,R.string.clicks_level_8,R.string.clicks_level_9,R.string.clicks_level_10,R.string.clicks_level_11,R.string.clicks_level_12,R.string.clicks_level_13,R.string.clicks_level_14)
        val clicksAchievements = arrayOf(0,R.string.clicks_achievement01_id,R.string.clicks_achievement02_id,R.string.clicks_achievement03_id,R.string.clicks_achievement04_id,R.string.clicks_achievement05_id,R.string.clicks_achievement06_id,R.string.clicks_achievement07_id,R.string.clicks_achievement08_id,R.string.clicks_achievement09_id,R.string.clicks_achievement10_id,R.string.clicks_achievement11_id,R.string.clicks_achievement12_id,R.string.clicks_achievement13_id,R.string.clicks_achievement14_id)
        fun clicksGetLeft(): String{
            return when(val clicksLeft = clicksGoals[getLevel(totalClicks,clicksGoals)+1] - totalClicks){
                in 0..999 -> buildString { append(clicksLeft) }
                in 1000..9999 -> buildString { append("%.1f".format(clicksLeft.toDouble()/1000)); append("k") }
                in 10000..999999 -> buildString { append("%.0f".format(clicksLeft.toDouble()/1000)); append("k") }
                else -> buildString { append("%.1f".format(clicksLeft.toDouble()/1000000)); append("M") }
            }
        }
        fun updateClicks(){
            binding.clicks.text = String.format(this@MainActivity.getString(R.string.clicks), applyThousandSeparator(totalClicks))
            if(getLevel(totalClicks,clicksGoals)<14){
                binding.clicksLevelNow.text = getLevelString(totalClicks,clicksGoals,0)
                binding.clicksLevelNext.text = getLevelString(totalClicks,clicksGoals,1)
                binding.clicksCountNow.text = this@MainActivity.getString(clicksLevels[getLevel(totalClicks,clicksGoals)])
                binding.clicksCountNext.text = this@MainActivity.getString(clicksLevels[getLevel(totalClicks,clicksGoals)+1])
                binding.clicksCountLeft.text = String.format(this@MainActivity.getString(R.string.clicksLeft), clicksGetLeft())
                binding.clicksProgressBar.min = clicksGoals[getLevel(totalClicks,clicksGoals)]
                binding.clicksProgressBar.max = clicksGoals[getLevel(totalClicks,clicksGoals)+1]
                binding.clicksProgressBar.progress = totalClicks.toInt()
                binding.clicksCongratulations.visibility = View.INVISIBLE
            }else{
                binding.clicksLevelNow.visibility = View.INVISIBLE
                binding.clicksLevelNext.visibility = View.INVISIBLE
                binding.clicksCountNow.visibility = View.INVISIBLE
                binding.clicksCountNext.visibility = View.INVISIBLE
                binding.clicksCountLeft.visibility = View.INVISIBLE
                binding.clicksProgressBar.visibility = View.INVISIBLE
                binding.clicksCongratulations.visibility = View.VISIBLE
            }
        }
        binding.clicksHitbox.setOnClickListener {
            totalClicks++
            save.edit().putLong("clicks",totalClicks).apply()
            updateClicks()
        }
        updateClicks()

        //Time code
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
                        in 0..59 -> binding.time.text = buildString { append(seconds); append(this@MainActivity.getString(R.string.second)) }
                        in 60..3599 -> binding.time.text = buildString { append(minutes); append(this@MainActivity.getString(R.string.minute)); append("%02d".format(seconds)); append(this@MainActivity.getString(R.string.second)) }
                        in 3600..86399 -> binding.time.text = buildString { append(hours); append(this@MainActivity.getString(R.string.hour)); append("%02d".format(minutes)); append(this@MainActivity.getString(R.string.minute)); append("%02d".format(seconds)); append(this@MainActivity.getString(R.string.second)) }
                        else -> binding.time.text = buildString { append(days); append(this@MainActivity.getString(R.string.day)); append("%02d".format(hours)); append(this@MainActivity.getString(R.string.hour)); append("%02d".format(minutes)); append(this@MainActivity.getString(R.string.minute)); append("%02d".format(seconds)); append(this@MainActivity.getString(R.string.second)) }
                    }
                    val timeGoals = arrayOf(0, 60, 300, 600, 1800, 3600, 7200, 14400, 25200, 43200, 86400, 259200, 604800, 1209600, 2592000)
                    val timeLevels = arrayOf(R.string.time_level_0,R.string.time_level_1,R.string.time_level_2,R.string.time_level_3,R.string.time_level_4,R.string.time_level_5,R.string.time_level_6,R.string.time_level_7,R.string.time_level_8,R.string.time_level_9,R.string.time_level_10,R.string.time_level_11,R.string.time_level_12,R.string.time_level_13,R.string.time_level_14)
                    val timeAchievements = arrayOf(0,R.string.time_achievement01_id,R.string.time_achievement02_id,R.string.time_achievement03_id,R.string.time_achievement04_id,R.string.time_achievement05_id,R.string.time_achievement06_id,R.string.time_achievement07_id,R.string.time_achievement08_id,R.string.time_achievement09_id,R.string.time_achievement10_id,R.string.time_achievement11_id,R.string.time_achievement12_id,R.string.time_achievement13_id,R.string.time_achievement14_id)
                    fun timeGetLeft(): String{
                        val secondsLeft = timeGoals[getLevel(totalSeconds,timeGoals)+1] - totalSeconds
                        val secondsToShow = secondsLeft.rem(60)
                        val minutesToShow = (secondsLeft/60).rem(60)
                        val hoursToShow = (secondsLeft/3600)
                        val daysToShow = secondsLeft/86400
                        return when(secondsLeft){
                            in 0..59 -> buildString { append(secondsToShow); append("\"") }
                            in 60..3599 -> buildString { append(minutesToShow); append("\' "); append("%02d".format(secondsToShow)); append("\"") }
                            in 3600..359999 -> buildString { append(hoursToShow); append(":"); append("%02d".format(minutesToShow)) }
                            else -> buildString { append(daysToShow); append(" "); append(
                                this@MainActivity.getString(R.string.days)
                            ) }
                        }
                    }
                    if(getLevel(totalSeconds,timeGoals)<14){
                        binding.timeLevelNow.text = getLevelString(totalSeconds,timeGoals,0)
                        binding.timeLevelNext.text = getLevelString(totalSeconds,timeGoals,1)
                        binding.timeCountNow.text = this@MainActivity.getString(timeLevels[getLevel(totalSeconds,timeGoals)])
                        binding.timeCountNext.text = this@MainActivity.getString(timeLevels[getLevel(totalSeconds,timeGoals)+1])
                        binding.timeCountLeft.text = String.format(this@MainActivity.getString(R.string.timeLeft), timeGetLeft())
                        binding.timeProgressBar.min = timeGoals[getLevel(totalSeconds,timeGoals)]
                        binding.timeProgressBar.max = timeGoals[getLevel(totalSeconds,timeGoals)+1]
                        binding.timeProgressBar.progress = totalSeconds.toInt()
                        binding.timeCongratulations.visibility = View.INVISIBLE
                    }else{
                        binding.timeLevelNow.visibility = View.INVISIBLE
                        binding.timeLevelNext.visibility = View.INVISIBLE
                        binding.timeCountNow.visibility = View.INVISIBLE
                        binding.timeCountNext.visibility = View.INVISIBLE
                        binding.timeCountLeft.visibility = View.INVISIBLE
                        binding.timeProgressBar.visibility = View.INVISIBLE
                        binding.timeCongratulations.visibility = View.VISIBLE
                    }

                    //Updates Play Games data every minute
                    if(totalSeconds.rem(60).toInt()==0){
                        for(i in 1..14){
                            if(totalSeconds>=timeGoals[i]){
                                PlayGames.getAchievementsClient(this@MainActivity).unlock(this@MainActivity.getString(timeAchievements[i]))
                            }
                            if(totalClicks>=clicksGoals[i]){
                                PlayGames.getAchievementsClient(this@MainActivity).unlock(this@MainActivity.getString(clicksAchievements[i]))
                            }
                        }
                        PlayGames.getLeaderboardsClient(this@MainActivity).submitScore(this@MainActivity.getString(R.string.time_leaderboard_id), totalSeconds*1000)
                        PlayGames.getLeaderboardsClient(this@MainActivity).submitScore(this@MainActivity.getString(R.string.clicks_leaderboard_id), totalClicks)
                        if(totalSeconds>=timeGoals[timeGoals.size-1] && totalClicks>=clicksGoals[clicksGoals.size-1]){
                            PlayGames.getAchievementsClient(this@MainActivity).unlock(this@MainActivity.getString(R.string.all_achievement_id))
                        }
                    }
                }
                mainHandler.postDelayed(this, 1000)
            }
        })
    }
}