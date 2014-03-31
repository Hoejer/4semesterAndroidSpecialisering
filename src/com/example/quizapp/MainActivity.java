package com.example.quizapp;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String PREFS_NAME = "MyPrefsFile";
	private static final String PREF_USERID = "UserId";
	private static final String PREF_GAMEID = "GameId";
	private static final String PREF_QUESTIONNUMB = "QuestionNumb";
	private static final String PREF_USERNAME = "Username";
	private static final String PREF_BANK = "Bank";
	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://jhl.jobudbud.dk/WebService.asmx";
	private final String SOAP_ACTION = "http://tempuri.org/startGameBot";
	private final String METHOD_NAME = "startGameBot";
	LinearLayout loadSpinner;
	TextView usernameTV;
	TextView moneyTV;
	String username;
	int money;
	int userId;
	int gameId;
	PropertyInfo userIdProp;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		usernameTV = (TextView)findViewById(R.id.usernameProfile);
		moneyTV = (TextView)findViewById(R.id.money);
		SharedPreferences getId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		username = getId.getString(PREF_USERNAME, "Error!");
		usernameTV.setText(username);
		money = getId.getInt(PREF_BANK, -1);
		moneyTV.setText(String.valueOf(money));
		userId = getId.getInt(PREF_USERID, -1);
		if (userId == -1) {
			FailedToRecieveUserId();
		}
		
		loadSpinner = (LinearLayout)findViewById(R.id.linelayoutMain);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void FailedToRecieveUserId() 
	{
		// TODO Alertbox.
	}

	public void FailedToReciveGameId() 
	{
		// TODO Alertbox.
	}

	/**
	 * Starter det asynkrone kald til webservicen, i forhold til at få kaldt 
	 * metoden til at indsætte et Game i databasen.
	 * @param view
	 */
	public void SinglePlayerOnClick(View view) 
	{
		AsyncStartGame asyncStartGame = new AsyncStartGame();
		asyncStartGame.execute();
	}
	
	/**
	 * Starter et nyt intent, SingleMainActivity.
	 */
	public void SinglePlayerMain() 
	{
		Intent intent = new Intent(this, SingleMainActivity.class);

		startActivity(intent);
	}

	/**
	 * Den asynkrone klasse der kalder startGameBot(). Samt fejlhåndtere forkert gameId og sætter gameId til preferences.
	 */
	private class AsyncStartGame extends AsyncTask<String, Void, Void> 
	{

		@Override
		protected void onPreExecute() {
			loadSpinner.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) 
		{
			startGameBot();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) 
		{
			if (gameId == -1) {
				FailedToReciveGameId();
			}

			else {
				loadSpinner.setVisibility(View.GONE);
				getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
						.putInt(PREF_GAMEID, gameId).commit();
				getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, "startQuestion").commit();
				
				SinglePlayerMain();

			}
		}

	}

	/**
	 * Laver en request til webservicen som indeholder userId, og indsat et Game i databasen, som returnere et gameId.
	 */
	public void startGameBot() 
	{
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		userIdProp = new PropertyInfo();
		userIdProp.type = userIdProp.INTEGER_CLASS;
		userIdProp.setName("userId");
		userIdProp.setValue(userId);
		userIdProp.setType(Integer.class);

		request.addProperty(userIdProp);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

		try 
		{
			androidHttpTransport.call(SOAP_ACTION, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			gameId = Integer.parseInt(response.toString());

		} 
		catch (Exception e) 
		{
			// TODO: handle exception
		}
	}

}
