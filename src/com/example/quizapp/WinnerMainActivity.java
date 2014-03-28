package com.example.quizapp;


import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;


public class WinnerMainActivity extends Activity {

	ArrayList<Integer> winners = new ArrayList<Integer>();
	TextView userWhoWonTV;
	TextView amountWonTV;
	int potsize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_winner_main);
		winners = getIntent().getExtras().getIntegerArrayList("Winners");
		potsize = getIntent().getExtras().getInt("pot");
		userWhoWonTV = (TextView)findViewById(R.id.userWhoWon);
		amountWonTV = (TextView)findViewById(R.id.potsizeWinner);
		
		String winwin = "";
		for(int i = 0; i < winners.size(); i++)
		{
			if(i == winners.size() - 1)
			{
				winwin += winners.get(i).toString();
			}
			else
			{
				winwin += winners.get(i).toString() + ", ";
			}
		}
		userWhoWonTV.setText(winwin);
		amountWonTV.setText(String.valueOf(potsize));
	}

	public void onBackClick(View v)
	{
		finish();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.winner_main, menu);
		return true;
	}

	
}
