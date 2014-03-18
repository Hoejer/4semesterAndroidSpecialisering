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

public class MainActivity extends Activity {

	public static final String PREFS_NAME = "MyPrefsFile";
	private static final String PREF_USERID = "UserId";
	private static final String PREF_GAMEID = "GameId";
	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://jhl.jobudbud.dk/WebService.asmx";
	private final String SOAP_ACTION = "http://tempuri.org/startGameBot";
	private final String METHOD_NAME = "startGameBot";
	int userId;
	int gameId;
	PropertyInfo userIdProp;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SharedPreferences getId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		userId = getId.getInt(PREF_USERID, -1);
		if (userId == -1) {
			FailedToRecieveUserId();
		}

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

	public void SinglePlayerOnClick(View view) 
	{
		AsyncStartGame asyncStartGame = new AsyncStartGame();
		asyncStartGame.execute();
	}

	public void SinglePlayerMain() 
	{
		Intent intent = new Intent(this, SingleMainActivity.class);

		startActivity(intent);
	}

	private class AsyncStartGame extends AsyncTask<String, Void, Void> 
	{

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
				getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
						.putInt(PREF_GAMEID, gameId).commit();
				
				SinglePlayerMain();

			}
		}

	}

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
