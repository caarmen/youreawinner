package ca.rmen.youreawinner;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String PREFERENCES_NAME = "ca.rmen.youreawinner";
	private static final String PREF_SCORE = "score";
	private static final String KEY_WINNER_TEXT = "winner_text";
	private TextView mTextViewWinnerText;
	private TextView mTextViewScore;
	private String[] mWinnerPhrases;
	private Random mRandom;
	private int mScore;
	private SharedPreferences mSharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextViewWinnerText = (TextView) findViewById(R.id.winner_text);
		mTextViewScore = (TextView) findViewById(R.id.score_value);
		mWinnerPhrases = getResources().getStringArray(R.array.winner_phrases);
		mRandom = new Random();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSharedPreferences = getSharedPreferences(PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		mScore = mSharedPreferences.getInt(PREF_SCORE, 0);
		mTextViewScore.setText(String.valueOf(mScore));
	}

	public void onButtonClicked(View v) {
		int winnerPhraseIndex = mRandom.nextInt(mWinnerPhrases.length);
		mTextViewWinnerText.setText(mWinnerPhrases[winnerPhraseIndex]);
		int scoreIncrease = mRandom.nextInt(400);
		mScore += 100 + scoreIncrease;
		Editor editor = mSharedPreferences.edit();
		editor.putInt(PREF_SCORE, mScore);
		editor.commit();
		mTextViewScore.setText(String.valueOf(mScore));
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

}
