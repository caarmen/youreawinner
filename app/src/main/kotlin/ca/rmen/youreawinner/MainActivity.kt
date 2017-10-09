/*
 * Copyright 2017 Carmen Alvarez
 *
 * This file is part of You're a Winner!.
 *
 * You're a Winner! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * You're a Winner! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with You're a Winner!.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.rmen.youreawinner

import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import ca.rmen.youreawinner.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding

    private var mWinnerPhrases: Array<out String> = arrayOf()
    private val mRandom: Random = Random()
    private val mSoundPool: SoundPool = createSoundPool()
    private var mButtonPressSoundPoolId: Int = 0
    private var mButtonReleaseSoundPoolId: Int = 0
    private lateinit var mSharedPreferences: SharedPreferences
    private var mScore: Long = 0
    private var mSoundEnabled: Boolean = false

    companion object {
        private const val PREF_SCORE = "score"
        private const val PREF_SOUND = "sound"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mWinnerPhrases = resources.getStringArray(R.array.winner_phrases)
        mButtonPressSoundPoolId = mSoundPool.load(this, R.raw.button_press, 1)
        mButtonReleaseSoundPoolId = mSoundPool.load(this, R.raw.button_release, 1)
        volumeControlStream = AudioManager.STREAM_MUSIC
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        mBinding.button.setOnTouchListener(mButtonListener)
    }

    override fun onResume() {
        super.onResume()
        mScore = mSharedPreferences.getLong(PREF_SCORE, 0)
        mSoundEnabled = mSharedPreferences.getBoolean(PREF_SOUND, true)
        mBinding.scoreValue.text = mScore.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val soundMenuItem = menu.findItem(R.id.menu_sound)
        updateSoundMenuItem(soundMenuItem)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_share -> shareScore()
            R.id.menu_sound -> {
                mSoundEnabled = !mSoundEnabled
                mSharedPreferences.edit().putBoolean(PREF_SOUND, mSoundEnabled).apply()
                updateSoundMenuItem(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val mButtonListener: View.OnTouchListener = View.OnTouchListener { _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_UP -> {
                if (mSoundEnabled) mSoundPool.play(mButtonReleaseSoundPoolId, 1f, 1f, 1, 0, 1f)

                val winnerPhraseIndex = mRandom.nextInt(mWinnerPhrases.size)
                mBinding.winnerText.text = getString(R.string.winner_text, mWinnerPhrases[winnerPhraseIndex])
                val scoreIncrease = mRandom.nextInt(400)
                mScore += 100 + scoreIncrease
                mBinding.scoreValue.text = mScore.toString()
                mSharedPreferences.edit().putLong(PREF_SCORE, mScore).apply()

            }
            MotionEvent.ACTION_DOWN -> {
                if (mSoundEnabled) mSoundPool.play(mButtonPressSoundPoolId, 1f, 1f, 1, 0, 1f)
            }
        }
        false
    }

    /**
     * Bring up an intent chooser so the user can choose an app to use to share
     * his winning score (with a link to the app on the play store of course).
     */
    private fun shareScore() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        val shareText = getString(R.string.share_text, mScore.toString())
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        val chooser = Intent.createChooser(intent, getString(R.string.share_label))
        startActivity(chooser)
    }

    /**
     * The sound menu/action item should show the current state of the sound:
     * show the "on" icon and label if sound is enabled. Show the "off" icon and
     * label if sound is disabled.
     */
    private fun updateSoundMenuItem(item: MenuItem) {
        mSoundEnabled = mSharedPreferences.getBoolean(PREF_SOUND, true)
        if (mSoundEnabled) {
            item.setIcon(R.drawable.ic_lock_ringer_on)
            item.setTitle(R.string.sound_on_label)
        } else {
            item.setIcon(R.drawable.ic_lock_ringer_off)
            item.setTitle(R.string.sound_off_label)
        }
    }

    private fun createSoundPool() : SoundPool {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            return SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        } else {
            return SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build())
                    .build()
        }
    }
}
