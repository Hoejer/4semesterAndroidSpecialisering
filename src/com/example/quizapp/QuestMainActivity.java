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
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;


public class QuestMainActivity extends Activity {

	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://jhl.jobudbud.dk/WebService.asmx";
	private final String SOAP_ACTION = "http://tempuri.org/getQuestion";
	private final String METHOD_NAME = "getQuestion";
	private PropertyInfo topicToCallFrom;
	String topicFromIntent;
	TextView questionTv;
	TextView answerTv1;
	TextView answerTv2;
	TextView answerTv3;
	TextView answerTv4;
	String question;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quest_main);
		questionTv = (TextView)findViewById(R.id.textView1);
		topicFromIntent = getIntent().getExtras().getString("choosenTopic");
		
		AsyncCallGetQuestion getQuestion = new AsyncCallGetQuestion();
		getQuestion.execute();
		
		
	}
	
	private class AsyncCallGetQuestion extends AsyncTask<String, Void, Void>
	{

		@Override
		protected void onPreExecute() {
			Log.i("Do in background", "onPreExecute");
			questionTv.setText("Loading Question...");
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.i("Do in background", "onPostExecute");
			// TODO Auto-generated method stub
			questionTv.setText(question);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.i("Do in background", "onProgressUpdate");
			
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			Log.i("Do in background", "doInBackground");
			getTopicFromWeb(topicFromIntent);
			return null;
		}
		
	}
	
	public void getTopicFromWeb(String oldTopic)
	{
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		topicToCallFrom = new PropertyInfo();
		topicToCallFrom.type = topicToCallFrom.STRING_CLASS;
		topicToCallFrom.setName("HullaBullaBulla");
		topicToCallFrom.setValue(oldTopic.toString());
		topicToCallFrom.setType(String.class);
		request.addProperty(topicToCallFrom);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		
		try
		{
			androidHttpTransport.call(SOAP_ACTION, envelope);
			
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			
			question = response.toString();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.quest_main, menu);
		return true;
	}

}
