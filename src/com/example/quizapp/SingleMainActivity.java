package com.example.quizapp;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SingleMainActivity extends Activity {

	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://jhl.jobudbud.dk/WebService.asmx";
	private final String SOAP_ACTION = "http://tempuri.org/getRandomTopic";
	private final String METHOD_NAME = "getRandomTopic";
	String topicFromWeb;
	TextView tv;
     
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_main);
        //Fahrenheit Text control
        tv = (TextView) findViewById(R.id.tv_result);
        //Create instance for AsyncCallWS
        AsyncCallWS task = new AsyncCallWS();
        //Call execute 
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
            //Assign it to fahren static variable
            topicFromWeb = response.toString();
     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void goToQuestion(View view)
    {
    	Intent intent = new Intent(this, QuestMainActivity.class);
    	intent.putExtra("choosenTopic", topicFromWeb);
    	
    	startActivity(intent);
    }
}
