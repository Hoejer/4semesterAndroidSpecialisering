package com.example.quizapp;


import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


public class WinnerMainActivity extends Activity {
	
	private final String NAMESPACE = "http://tempuri.org/";
	public static final String PREFS_NAME = "MyPrefsFile";
	private static final String PREF_USERNAME = "Username";
	private static final String PREF_USERID = "UserId";
	private static final String PREF_BANK = "Bank";
	private static final String PREF_MYBET = "MyBet";
	private final String URL = "http://jhl.jobudbud.dk/WebService.asmx";
	ArrayList<String> winnersList;
	LinearLayout loadSpinner;
	TextView userWhoWonTV;
	TextView amountWonTV;
	String myUsername;
	int userId;
	int gameId;
	int potToTransfer;
	int potsize;
	int myPotsize;
	int myBet;
	private PropertyInfo gameIdWhoWon;
	private PropertyInfo userIdProp;
	private PropertyInfo iWonHowMuchProp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_winner_main);
		SharedPreferences getId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		userId= getId.getInt(PREF_USERID, -1);
		myUsername = getId.getString(PREF_USERNAME, "Error");
		myPotsize = getId.getInt(PREF_BANK, -1);
		myBet = getId.getInt(PREF_MYBET, -1);
		gameId = getIntent().getExtras().getInt("gameId");
		potsize = getIntent().getExtras().getInt("pot");
		userWhoWonTV = (TextView)findViewById(R.id.userWhoWon);
		amountWonTV = (TextView)findViewById(R.id.potsizeWinner);
		loadSpinner = (LinearLayout)findViewById(R.id.linelayoutWinner);
		AsyncWhoWon asyncWhoWon = new AsyncWhoWon();
		asyncWhoWon.execute();
		
		
	}

	
	private class AsyncWhoWon extends AsyncTask<String, Void, Void>
    {

		@Override
		protected Void doInBackground(String... params) {
			checkWhoWon();
			return null;
		}

		@Override
		protected void onPreExecute() {
			loadSpinner.setVisibility(View.VISIBLE);
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			loadSpinner.setVisibility(View.GONE);
			setWinners();
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
    	
    }
    
    public void checkWhoWon()
    {
    	//Create request
        SoapObject request = new SoapObject(NAMESPACE, "whoWon");
        
        gameIdWhoWon = new PropertyInfo();
        gameIdWhoWon.type = gameIdWhoWon.INTEGER_CLASS;
        gameIdWhoWon.setName("gameId");
        gameIdWhoWon.setValue(gameId);
        gameIdWhoWon.setType(Integer.class);
        
        request.addProperty(gameIdWhoWon);
        
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);
        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
     
        try {
            //Invole web service
            androidHttpTransport.call("http://tempuri.org/whoWon", envelope);
            //Get the response
            SoapObject response = (SoapObject) envelope.bodyIn;
            
            //Lav array, hvis der er flere vindere.
            SoapObject property = (SoapObject)response.getProperty(0);
            int count = property.getPropertyCount();
            winnersList = new ArrayList<String>(); 
            
            for (int i = 0; i < count; i++)
            {
            	
                winnersList.add(property.getProperty(i).toString());
            }
     
        } catch (Exception e) {
            winnersList.add("A BOT!!");
        }
    }
    
    /**
     * Indsætter vinderne i et textview, og tjekker om der er flere vindere.
     */
    public void setWinners()
    {
    	String winwin = "";
    	didIWin();
		for(int i = 0; i < winnersList.size(); i++)
		{
			if(i == winnersList.size() - 1)
			{
				winwin += winnersList.get(i).toString();
			}
			else
			{
				winwin += winnersList.get(i).toString() + ", ";
			}
		}
		userWhoWonTV.setText(winwin);
		amountWonTV.setText(String.valueOf(potsize));
    	
    }
    
    public void didIWin()
    {
    	for(int i = 0; i < winnersList.size(); i ++)
    	{
    		if(winnersList.get(i).equals(myUsername))
    		{
    			potToTransfer =  (potsize / winnersList.size()) + myPotsize - myBet;
    			
    			AsyncUpdateBank asyncUpdateBank = new AsyncUpdateBank();
    			asyncUpdateBank.execute();
    		}
    		
    	}
    }
    
    public void updateMyBank()
    {
    	SoapObject request = new SoapObject(NAMESPACE, "iWon");
    	
    	userIdProp = new PropertyInfo();
    	userIdProp.type = userIdProp.INTEGER_CLASS;
    	userIdProp.setName("userId");
    	userIdProp.setValue(userId);
    	userIdProp.setType(Integer.class);
    	
    	request.addProperty(userIdProp);
    	
    	iWonHowMuchProp = new PropertyInfo();
    	iWonHowMuchProp.type = iWonHowMuchProp.INTEGER_CLASS;
    	iWonHowMuchProp.setName("howMuch");
    	iWonHowMuchProp.setValue(potToTransfer);
    	iWonHowMuchProp.setType(Integer.class);
    	
    	request.addProperty(iWonHowMuchProp);
    	
    	 SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                 SoapEnvelope.VER11);
         envelope.dotNet = true;
         //Set output SOAP object
         envelope.setOutputSoapObject(request);
         //Create HTTP call object
         HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
      
         try {
             //Invole web service
             androidHttpTransport.call("http://tempuri.org/iWon", envelope);
             //Get the response
             //SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
             
      
         } catch (Exception e) {
             e.printStackTrace();
         }
    	
    }
    
    private class AsyncUpdateBank extends AsyncTask<String, Void, Void>
    {

		@Override
		protected Void doInBackground(String... params) {
			updateMyBank();
			return null;
		}

		@Override
		protected void onPreExecute() {
			loadSpinner.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			loadSpinner.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
    	
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
