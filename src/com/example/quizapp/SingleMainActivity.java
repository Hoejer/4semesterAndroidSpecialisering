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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SingleMainActivity extends Activity {

	public static final String PREFS_NAME = "MyPrefsFile";
	private static final String PREF_USERID = "UserId";
	private static final String PREF_GAMEID = "GameId";
	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://jhl.jobudbud.dk/WebService.asmx";
	private final String SOAP_ACTION = "http://tempuri.org/getRandomTopic";
	private final String METHOD_NAME = "getRandomTopic";
	int gameId;
	PropertyInfo gameIdProp;
	String topicFromWeb;
	TextView tv;
     
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_main);

        SharedPreferences getId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gameId = getId.getInt(PREF_GAMEID, -1);
        if(gameId == -1)
        {
        	UnableToGetGameId();
        }
        
        tv = (TextView) findViewById(R.id.tv_result);

        AsyncCallWS task = new AsyncCallWS();

        task.execute();
    }
    
    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i("Do in background", "doInBackground");
            getTopic();
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            Log.i("Do in background", "onPostExecute");
            tv.setText(topicFromWeb);
        }
 
        @Override
        protected void onPreExecute() {
            Log.i("Do in background", "onPreExecute");
            tv.setText("Choosing Topic");
            
        }
 
        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i("Do in background", "onProgressUpdate");
        }
 
    }
    
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
    
    public void UnableToGetGameId()
    {
    	//TODO alertbox.
    }
    public void goToQuestion(View view)
    {
    	Intent intent = new Intent(this, QuestMainActivity.class);
    	intent.putExtra("choosenTopic", topicFromWeb);
    	
    	startActivity(intent);
    }
}
