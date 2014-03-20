package com.example.quizapp;

import java.io.Console;

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
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

//TEst test

public class QuestMainActivity extends Activity {
	
	public static final String PREFS_NAME = "MyPrefsFile";
	private static final String PREF_USERID = "UserId";
	private static final String PREF_GAMEID = "GameId";
	private static final String PREF_QUESTIONNUMB = "QuestionNumb";

	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://jhl.jobudbud.dk/WebService.asmx";
	private final String SOAP_ACTION = "http://tempuri.org/getQuestion";
	private final String METHOD_NAME = "getQuestion";
	private PropertyInfo topicToCallFrom;
	private PropertyInfo answerToCallFrom;
	private PropertyInfo idToCallFrom;
	private PropertyInfo questionNumbProp;
	private PropertyInfo userIdProp;
	private PropertyInfo gameIdProp;
	Boolean bla = true;
	String topicFromIntent;
	String userAnswer;
	TextView questionTv;
	TextView answerTv1;
	TextView answerTv2;
	TextView answerTv3;
	TextView answerTv4;
	int questionId;
	int gameId;
	int userId;
	String questionNumb;
	String question;
	String answer1String;
	String answer2String;
	String answer3String;
	String answer4String;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quest_main);
		questionTv = (TextView) findViewById(R.id.textView1);
		answerTv1 = (TextView) findViewById(R.id.answer1);
		answerTv2 = (TextView) findViewById(R.id.answer2);
		answerTv3 = (TextView) findViewById(R.id.answer3);
		answerTv4 = (TextView) findViewById(R.id.answer4);
		topicFromIntent = getIntent().getExtras().getString("choosenTopic");
		
		SharedPreferences getId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		userId = getId.getInt(PREF_USERID, -1);
		gameId = getId.getInt(PREF_GAMEID, -1);
		questionNumb = getId.getString(PREF_QUESTIONNUMB, "NothingFound");
		if(questionNumb.equals("NothingFound"))
		{
			questionNumb = "question1";
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, questionNumb).commit();
		}
		else if (questionNumb.equals("question1"))
		{
			questionNumb = "question2";
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, questionNumb).commit();
		}
		else if (questionNumb.equals("question2"))
		{
			questionNumb = "question3";
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, questionNumb).commit();
		}
		
		else if (questionNumb.equals("question3"))
		{
			questionNumb = "question4";
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, questionNumb).commit();
		}
		
		else
		{
			questionNumb = "question1";
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, questionNumb).commit();
		}
		

		AsyncCallGetQuestion getQuestion = new AsyncCallGetQuestion();
		getQuestion.execute();

	}

	public void onAnswerClick(View v) {
		switch (v.getId()) {
		case R.id.answer1:
			userAnswer = answerTv1.getText().toString();
			setClickableFalse();
			break;
		case R.id.answer2:
			userAnswer = answerTv2.getText().toString();
			setClickableFalse();
			break;
		case R.id.answer3:
			userAnswer = answerTv3.getText().toString();
			setClickableFalse();
			break;
		case R.id.answer4:
			userAnswer = answerTv4.getText().toString();
			setClickableFalse();
			break;
		default:
			throw new RuntimeException("Unknow button ID");
		}
	}

	public void setClickableFalse() {
		answerTv1.setClickable(false);
		answerTv2.setClickable(false);
		answerTv3.setClickable(false);
		answerTv4.setClickable(false);
		AsyncCallCheckAnswer checkAnswer = new AsyncCallCheckAnswer();
		checkAnswer.execute();
	}

	private class AsyncCallGetQuestion extends AsyncTask<String, Void, Void> {

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
			answerTv1.setText(answer1String);
			answerTv2.setText(answer2String);
			answerTv3.setText(answer3String);
			answerTv4.setText(answer4String);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.i("Do in background", "onProgressUpdate");

		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			Log.i("Do in background", "doInBackground");
			getQuestionFromWeb(topicFromIntent);
			return null;
		}

	}

	private class AsyncCallCheckAnswer extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			rightWrongDialog(getCurrentFocus());
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			answerQuestion();
			return null;
		}

	}

	public void getQuestionFromWeb(String oldTopic) {
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		topicToCallFrom = new PropertyInfo();
		topicToCallFrom.type = topicToCallFrom.STRING_CLASS;
		topicToCallFrom.setName("HullaBullaBulla");
		topicToCallFrom.setValue(oldTopic.toString());
		topicToCallFrom.setType(String.class);
		request.addProperty(topicToCallFrom);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

		try {
			androidHttpTransport.call(SOAP_ACTION, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			String s = response.toString();
			String ss[] = s.split("\\* ", 6);
			questionId = Integer.parseInt(ss[0]);
			question = ss[1];
			answer1String = ss[2];
			answer2String = ss[3];
			answer3String = ss[4];
			answer4String = ss[5];

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void answerQuestion() {
		bla = false;
		SoapObject request = new SoapObject(NAMESPACE, "answerQuestion");
		answerToCallFrom = new PropertyInfo();
		answerToCallFrom.type = answerToCallFrom.STRING_CLASS;
		answerToCallFrom.setName("answerFromApp");
		answerToCallFrom.setValue(userAnswer);
		answerToCallFrom.setType(String.class);
		
		request.addProperty(answerToCallFrom);
		
		idToCallFrom = new PropertyInfo();
		idToCallFrom.type = idToCallFrom.INTEGER_CLASS;
		idToCallFrom.setName("idForQuest");
		idToCallFrom.setValue(questionId);
		idToCallFrom.setType(Integer.class);
		
		request.addProperty(idToCallFrom);
		
		questionNumbProp = new PropertyInfo();
		questionNumbProp.type = questionNumbProp.STRING_CLASS;
		questionNumbProp.setName("questionNumb");
		questionNumbProp.setValue(questionNumb);
		questionNumbProp.setType(String.class);
		
		request.addProperty(questionNumbProp);
		
		userIdProp = new PropertyInfo();
		userIdProp.type = userIdProp.INTEGER_CLASS;
		userIdProp.setName("userId");
		userIdProp.setValue(userId);
		userIdProp.setType(Integer.class);
		
		request.addProperty(userIdProp);
		
		gameIdProp = new PropertyInfo();
		gameIdProp.type = gameIdProp.INTEGER_CLASS;
		gameIdProp.setName("gameId");
		gameIdProp.setValue(gameId);
		gameIdProp.setType(Integer.class);
		
		request.addProperty(gameIdProp);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

		try {
			androidHttpTransport.call("http://tempuri.org/answerQuestion",
					envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			bla = Boolean.valueOf(response.toString());

		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void rightWrongDialog(View v) {
		if (bla) {
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

			// Set the message to display
			alertbox.setMessage("YOU'VE ANSWERED CORRECT, AND WAS THE FASTEST!");

			// Add a neutral button to the alert box and assign a click listener
			alertbox.setNeutralButton("Next Question",
					new DialogInterface.OnClickListener() {

						// Click listener on the neutral button of alert box
						public void onClick(DialogInterface arg0, int arg1) {
							finish();
							// The neutral button was clicked
							Toast.makeText(getApplicationContext(),
									"'OK' button clicked", Toast.LENGTH_LONG)
									.show();
						}
					});

			// show the alert box
			alertbox.show();
		}
		else
		{
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

			// Set the message to display
			alertbox.setMessage("WRONG ANSWER OR TOO SLOW, YOU'VE LOST THIS ROUND..");

			// Add a neutral button to the alert box and assign a click listener
			alertbox.setNeutralButton("Next Question",
					new DialogInterface.OnClickListener() {

						// Click listener on the neutral button of alert box
						public void onClick(DialogInterface arg0, int arg1) {
							finish();
							// The neutral button was clicked
							Toast.makeText(getApplicationContext(),
									"'OK' button clicked", Toast.LENGTH_LONG)
									.show();
						}
					});

			// show the alert box
			alertbox.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.quest_main, menu);
		return true;
	}

}
