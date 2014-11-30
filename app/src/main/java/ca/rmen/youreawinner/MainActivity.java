/*
 * Copyright 2013 Carmen Alvarez
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
package ca.rmen.youreawinner;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String PREF_SCORE = "score";
	private static final String PREF_SOUND = "sound";
	private static final String KEY_WINNER_TEXT = "winner_text";
	private TextView mTextViewWinnerText;
	private TextView mTextViewScore;
	private ImageView mButton;
	private String[] mWinnerPhrases;
	private Random mRandom;
	private long mScore;
	private SharedPreferences mSharedPreferences;
	private SoundPool mSoundPool;
	private int mButtonPressSoundPoolId;
	private int mButtonReleaseSoundPoolId;
	private boolean mSoundEnabled;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextViewWinnerText = (TextView) findViewById(R.id.winner_text);
		mTextViewScore = (TextView) findViewById(R.id.score_value);
		mButton = (ImageView) findViewById(R.id.button);
		mButton.setOnTouchListener(mButtonListener);
		mWinnerPhrases = getResources().getStringArray(R.array.winner_phrases);
		mRandom = new Random();
		mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		mButtonPressSoundPoolId = mSoundPool.load(this, R.raw.button_press, 1);
		mButtonReleaseSoundPoolId = mSoundPool.load(this, R.raw.button_release,
				1);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mScore = mSharedPreferences.getLong(PREF_SCORE, 0);
		mSoundEnabled = mSharedPreferences.getBoolean(PREF_SOUND, true);
		mTextViewScore.setText(String.valueOf(mScore));
	}

	private final View.OnTouchListener mButtonListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_UP:
				if (mSoundEnabled)
					mSoundPool
							.play(mButtonReleaseSoundPoolId, 1f, 1f, 1, 0, 1f);
				int winnerPhraseIndex = mRandom.nextInt(mWinnerPhrases.length);
				// The winner text is in italic, and on some devices (tatoo)
				// the last letter is cut off on the upper right. Adding
				// paddingRight in the layout xml does not fix this. Adding
				// a space to the end of the text here does.
				mTextViewWinnerText.setText(" "
						+ mWinnerPhrases[winnerPhraseIndex] + " ");
				int scoreIncrease = mRandom.nextInt(400);
				mScore += 100 + scoreIncrease;
				mTextViewScore.setText(String.valueOf(mScore));
				// Saving the score preference only in onPause or
				// onSaveInstanceState (instead of each time here) doesn't
				// improve audio latency. You can hear audio latency on the down
				// action as well, which doesn't do any writes to the disk.
				Editor editor = mSharedPreferences.edit();
				editor.putLong(PREF_SCORE, mScore);
				editor.commit();
				break;
			case MotionEvent.ACTION_DOWN:
				if (mSoundEnabled)
					mSoundPool.play(mButtonPressSoundPoolId, 1f, 1f, 1, 0, 1f);
				break;
			default:
				// Ignore
				break;
			}
			return false;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem soundMenuItem = menu.findItem(R.id.menu_sound);
		updateSoundMenuItem(soundMenuItem);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_share:
			// Share our winning score with the world
			shareScore();
			return true;
		case R.id.menu_sound:
			// Toggle sound on/off
			mSoundEnabled = !mSoundEnabled;
			mSharedPreferences.edit().putBoolean(PREF_SOUND, mSoundEnabled)
					.commit();
			updateSoundMenuItem(item);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		CharSequence winnerText = savedInstanceState.getString(KEY_WINNER_TEXT);
		mTextViewWinnerText.setText(winnerText);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putCharSequence(KEY_WINNER_TEXT, mTextViewWinnerText.getText());
		super.onSaveInstanceState(outState);
	}

	/**
	 * Bring up an intent chooser so the user can choose an app to use to share
	 * his winning score (with a link to the app on the play store of course).
	 */
	private void shareScore() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		String shareText = getString(R.string.share_text, mScore);
		intent.putExtra(Intent.EXTRA_TEXT, shareText);
		Intent chooser = Intent.createChooser(intent,
				getString(R.string.share_label));
		startActivity(chooser);

	}

	/**
	 * The sound menu/action item should show the current state of the sound:
	 * show the "on" icon and label if sound is enabled. Show the "off" icon and
	 * label if sound is disabled.
	 */
	private void updateSoundMenuItem(MenuItem item) {
		mSoundEnabled = mSharedPreferences.getBoolean(PREF_SOUND, true);
		if (mSoundEnabled) {
			item.setIcon(R.drawable.ic_lock_ringer_on);
			item.setTitle(R.string.sound_on_label);
		} else {
			item.setIcon(R.drawable.ic_lock_ringer_off);
			item.setTitle(R.string.sound_off_label);
		}
	}
}