package ca.rmen.youreawinner;

import java.util.Random;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String PREF_SCORE = "score";
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		mScore = mSharedPreferences.getLong(PREF_SCORE, 0);
		mTextViewScore.setText(String.valueOf(mScore));
	}

	private final View.OnTouchListener mButtonListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_UP:
				mSoundPool.play(mButtonReleaseSoundPoolId, 1f, 1f, 1, 0, 1f);
				int winnerPhraseIndex = mRandom.nextInt(mWinnerPhrases.length);
				// The winner text is in italic, and on some devices (tatoo)
				// the last letter is cut off on the upper right. Adding
				// paddingRight in the layout xml does not fix this. Adding
				// a space to the end of the text here does.
				mTextViewWinnerText.setText(" "
						+ mWinnerPhrases[winnerPhraseIndex] + " ");
				int scoreIncrease = mRandom.nextInt(400);
				mScore += 100 + scoreIncrease;
				Editor editor = mSharedPreferences.edit();
				editor.putLong(PREF_SCORE, mScore);
				editor.commit();
				mTextViewScore.setText(String.valueOf(mScore));
				break;
			case MotionEvent.ACTION_DOWN:
				mSoundPool.play(mButtonPressSoundPoolId, 1f, 1f, 1, 0, 1f);
				break;
			}
			return false;
		}
	};

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
}
