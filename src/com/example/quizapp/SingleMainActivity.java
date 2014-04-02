package com.example.quizapp;

import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SingleMainActivity extends Activity {

	public static final String PREFS_NAME = "MyPrefsFile";
	private static final String PREF_USERID = "UserId";
	private static final String PREF_GAMEID = "GameId";
	private static final String PREF_QUESTIONNUMB = "QuestionNumb";
	private static final String PREF_MYBET = "MyBet";
	private static final String PREF_BANK = "Bank";
	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://jhl.jobudbud.dk/WebService.asmx";
	private final String SOAP_ACTION = "http://tempuri.org/getRandomTopic";
	private final String METHOD_NAME = "getRandomTopic";
	int gameId;
	int userId;
	int betMoney;
	int currMoney;
	int potSizeFromWeb;
	LinearLayout loadSpinner;
	PropertyInfo gameIdPropBet;
	PropertyInfo gameIdProp;
	PropertyInfo betProp;
	PropertyInfo userIdProp;
	String topicFromWeb;
	String winnerString;
	String winner1;
	String winner2;
	String winner3;
	String winner4;
	TextView tv;
	TextView potSizeTextView;
     
	/**
	 * Når aktiviteten starter, henter den gameId fra preferences, og executer det asynkrone kald.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_main);

        SharedPreferences getId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gameId = getId.getInt(PREF_GAMEID, -1);
        SharedPreferences getUser = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = getUser.getInt(PREF_USERID, -1);
        if(gameId == -1)
        {
        	UnableToGetGameId();
        }
        
        tv = (TextView) findViewById(R.id.tv_result);
        potSizeTextView = (TextView)findViewById(R.id.potSize);
        loadSpinner = (LinearLayout)findViewById(R.id.linelayoutSingle);
        AsyncCallWS task = new AsyncCallWS();

        task.execute();
        
        
    }
    
    /**
     *	Ansynkron klasse der kalder getTopic, og sætter det returnede topic i et textview.
     */
    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i("Do in background", "doInBackground");
            getTopic();
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
        	loadSpinner.setVisibility(View.GONE);
            Log.i("Do in background", "onPostExecute");
            alertDialogBet();
        }
 
        @Override
        protected void onPreExecute() {
        	loadSpinner.setVisibility(View.VISIBLE);
            Log.i("Do in background", "onPreExecute");
            tv.setText("Choosing Topic");
            
        }
 
        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i("Do in background", "onProgressUpdate");
        }
 
    }
    
    /**
     * Henter et random topic fra databasen gennem webservicen, og sætter topicId på game.
     */
    public void getTopic() 
    {
        //Create request
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        
        gameIdProp = new PropertyInfo();
        gameIdProp.type = gameIdProp.INTEGER_CLASS;
		gameIdProp.setName("gameId");
		gameIdProp.setValue(gameId);
		gameIdProp.setType(Integer.class);
		
		request.addProperty(gameIdProp);
        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);
        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
     
        try {
            //Invole web service
            androidHttpTransport.call(SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to variable
            topicFromWeb = response.toString();
     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	public void alertDialogBet() {

		TextView title = new TextView(this);
		title.setText("Place your bet!");
		title.setGravity(Gravity.CENTER);
		
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		final AlertDialog d = new AlertDialog.Builder(this)
				.setCustomTitle(title)
				.setMessage("The topic is " + topicFromWeb + "." + " How much do you wanna bet?")
				.setPositiveButton(android.R.string.ok, null) // Set to null. We
																// override the
																// onclick
				.create();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT);
	    input.setLayoutParams(lp);
	    d.setView(input);
		d.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {

				Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Do something
						
						betMoney = Integer.parseInt(input.getText().toString());
						currMoney = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(PREF_BANK, -1);
						if(betMoney > currMoney)
						{
							input.setText("You only have : " + currMoney);
							
						}
						else
						{
							getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt(PREF_MYBET, betMoney).commit();
							AsyncBet asyncBet = new AsyncBet();
							asyncBet.execute();
							d.dismiss();
						}
						
					}
				});
			}
		});
    	
		d.show();
    }
    
    private class AsyncBet extends AsyncTask<String, Void, Void>
    {
    	@Override
        protected Void doInBackground(String... params) {
            Log.i("Do in background", "doInBackground");
            setBet();
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            Log.i("Do in background", "onPostExecute");
            if(potSizeFromWeb == 2147483647)
            {
            	//TODO Alertbox.
            }
            else
            {	tv.setText(topicFromWeb);
            	loadSpinner.setVisibility(View.GONE);
            	potSizeTextView.setText("Potsize is = " + potSizeFromWeb + ".");
            }
        }
 
        @Override
        protected void onPreExecute() {
        	loadSpinner.setVisibility(View.VISIBLE);
            Log.i("Do in background", "onPreExecute");
            tv.setText("Choosing Topic");
            
        }
 
        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i("Do in background", "onProgressUpdate");
        }
    }
    
    public void setBet()
    {
    	//Create request
        SoapObject request = new SoapObject(NAMESPACE, "betTopicBot");
        
        betProp = new PropertyInfo();
        betProp.type = betProp.INTEGER_CLASS;
        betProp.setName("bet");
        betProp.setValue(betMoney);
        betProp.setType(Integer.class);
        
        request.addProperty(betProp);
        
        gameIdPropBet = new PropertyInfo();
        gameIdPropBet.type = gameIdPropBet.INTEGER_CLASS;
		gameIdPropBet.setName("gameId");
		gameIdPropBet.setValue(gameId);
		gameIdPropBet.setType(Integer.class);
		
		request.addProperty(gameIdPropBet);
		
		userIdProp = new PropertyInfo();
		userIdProp.type = userIdProp.INTEGER_CLASS;
		userIdProp.setName("userId");
		userIdProp.setValue(userId);
		userIdProp.setType(Integer.class);
		
		request.addProperty(userIdProp);
        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);
        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
     
        try {
            //Invole web service
            androidHttpTransport.call("http://tempuri.org/betTopicBot", envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to variable
            potSizeFromWeb = Integer.parseInt(response.toString());
     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void UnableToGetGameId()
    {
    	//TODO alertbox.
    }
    
    /**
     * Starter questmainacitivty og sender topic med.
     * @param view
     */
    public void goToQuestion(View view)
    {
    	
    		Intent intent = new Intent(this, QuestMainActivity.class);
    		intent.putExtra("choosenTopic", topicFromWeb);
    		intent.putExtra("pot", potSizeFromWeb);
    	
    		startActivity(intent);
    		finish();
    }
    
    
}
