package com.example.quizapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;



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
	boolean win;
	LinearLayout loadSpinner;
	Boolean bla = true;
	String topicFromIntent;
	String userAnswer;
	TextView questionNumber;
	TextView questionTv;
	TextView countDownTv;
	Button answerTv1;
	Button answerTv2;
	Button answerTv3;
	Button answerTv4;
	int questionId;
	int gameId;
	int userId;
	int potSizeFromWeb;
	int stoppedCountAt;
	String questionNumb;
	String question;
	String answer1String;
	String answer2String;
	String answer3String;
	String answer4String;
	boolean finished;
	CountDownTimer cdt;

	/**
	 * Assigner en masse textviews til navne, og henter userid, gameid og question numb ud af Preferences.
	 * Herefter kalder vi den asynkrone klasse som skal hente et random spørgsmål ud af databasen gennem webservicen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quest_main);
		questionNumber = (TextView)findViewById(R.id.welcome);
		questionTv = (TextView) findViewById(R.id.textView1);
		answerTv1 = (Button) findViewById(R.id.answer1);
		answerTv2 = (Button) findViewById(R.id.answer2);
		answerTv3 = (Button) findViewById(R.id.answer3);
		answerTv4 = (Button) findViewById(R.id.answer4);
		countDownTv = (TextView)findViewById(R.id.timerCountDown);
		topicFromIntent = getIntent().getExtras().getString("choosenTopic");
		potSizeFromWeb = getIntent().getExtras().getInt("pot");
		loadSpinner = (LinearLayout)findViewById(R.id.linelayoutQuest);
		
		SharedPreferences getId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		userId = getId.getInt(PREF_USERID, -1);
		gameId = getId.getInt(PREF_GAMEID, -1);
		questionNumb = getId.getString(PREF_QUESTIONNUMB, "NothingFound");
		if(questionNumb.equals("NothingFound") || questionNumb.equals("startQuestion"))
		{
			questionNumb = "question1";
			questionNumber.setText("Question 1 of 4");
			finished = false;
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, questionNumb).commit();
			AsyncCallGetQuestion getQuestion = new AsyncCallGetQuestion();
			getQuestion.execute();
		}
		else if (questionNumb.equals("question1"))
		{
			questionNumb = "question2";
			questionNumber.setText("Question 2 of 4");
			finished = false;
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, questionNumb).commit();
			AsyncCallGetQuestion getQuestion = new AsyncCallGetQuestion();
			getQuestion.execute();
		}
		else if (questionNumb.equals("question2"))
		{
			questionNumb = "question3";
			questionNumber.setText("Question 3 of 4");
			finished = false;
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, questionNumb).commit();
			AsyncCallGetQuestion getQuestion = new AsyncCallGetQuestion();
			getQuestion.execute();
		}
		
		else if (questionNumb.equals("question3"))
		{
			questionNumb = "question4";
			questionNumber.setText("Question 4 of 4");
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, questionNumb).commit();
			AsyncCallGetQuestion getQuestion = new AsyncCallGetQuestion();
			getQuestion.execute();
		}
		
		else
		{
			questionNumb = "question1";
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_QUESTIONNUMB, questionNumb).commit();
		}
		

		

	}

	/**
	 * Finder ud af hvilket svar man har trykket på, for så at kalde metoden setClickableFalse().
	 * @param v
	 */
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

	/**
	 * Deaktivere muligheden for at dobbeltklikke eller klikke på andre svar, når man har svaret.
	 */
	
	public void setClickableFalse() {
		cdt.cancel();
		answerTv1.setClickable(false);
		answerTv2.setClickable(false);
		answerTv3.setClickable(false);
		answerTv4.setClickable(false);
		AsyncCallCheckAnswer checkAnswer = new AsyncCallCheckAnswer();
		checkAnswer.execute();
	}

	/**
	 * Den asynkrone klasse der kalder metoden som henter spørgsmålet fra databasen gennem webservicen.
	 * Sætter spørgsmålet og de 4 svarmuligheder.
	 */
	private class AsyncCallGetQuestion extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			loadSpinner.setVisibility(View.VISIBLE);
			Log.i("Do in background", "onPreExecute");
			questionTv.setText("Loading Question...");
		}

		@Override
		protected void onPostExecute(Void result) {
			loadSpinner.setVisibility(View.GONE);
			Log.i("Do in background", "onPostExecute");
			// TODO Auto-generated method stub
			questionTv.setText(question);
			long seed = System.nanoTime();
			ArrayList<String> randomizedAnswers = new ArrayList<String>();
			randomizedAnswers.add(answer1String);
			randomizedAnswers.add(answer2String);
			randomizedAnswers.add(answer3String);
			randomizedAnswers.add(answer4String);
			Collections.shuffle(randomizedAnswers, new Random(seed));
			
			answerTv1.setText(randomizedAnswers.get(0));
			answerTv2.setText(randomizedAnswers.get(1));
			answerTv3.setText(randomizedAnswers.get(2));
			answerTv4.setText(randomizedAnswers.get(3));
			
			cdt = new CountDownTimer(11000, 1000) {

			     public void onTick(long millisUntilFinished) {
			        countDownTv.setText(String.valueOf(millisUntilFinished / 1000));
			        stoppedCountAt = (int) millisUntilFinished / 1000;
			     }

			     public void onFinish() {
			    	 setClickableFalse();
			    	 bla = false;
			    	 countDownTv.setText("Out of time!");
			         rightWrongDialog(getCurrentFocus());
			     } 
			     
			  }.start();

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

	/**
	 * Den asynkrone klasse der kalder rightWrongDialog, for at tjekke om man har svaret rigtigt.
	 */
	
	private class AsyncCallCheckAnswer extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			rightWrongDialog(getCurrentFocus());
		}

		@Override
		protected void onPreExecute() {
			loadSpinner.setVisibility(View.VISIBLE);
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

	/**
	 * Kalder webservicen gennem en request der indeholder det topic som blev sendt med fra SingleMainActivity.
	 * Den modtager en string som den splitter op.
	 * @param oldTopic
	 */
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

	/**
	 * Sætter ens userId i Game question(1,2,3,4) hvis man har svaret rigtigt hurtigst.
	 */
	public void answerQuestion() {
		
		bla = false;
		if(chancesOfWinning())
		{
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

	}

	/**
	 * Dialog der fortæller om man har svaret rigtigt hurtigst, forkert eller for langsomt.
	 * @param v
	 */
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
								nextQuestion();
								finish();
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
								nextQuestion();
								finish();
								
						}
					});

			// show the alert box
			alertbox.show();
		}
	}
	
	private void goToWinner()
	{
		Intent intent = new Intent(this, WinnerMainActivity.class);
		intent.putExtra("gameId", gameId);
		intent.putExtra("pot", potSizeFromWeb);
		startActivity(intent);
		finish();
	}
	
	public void nextQuestion()
	{
		if(questionNumb.equals("question4"))
		{
			goToWinner();
		}
		else
		{
			Intent intent = new Intent(this, QuestMainActivity.class);
			intent.putExtra("choosenTopic", topicFromIntent);
			intent.putExtra("pot", potSizeFromWeb);
	
			startActivity(intent);
			finish();
		}
		
	}
	
	public boolean chancesOfWinning()
	{
		boolean winCheck = true;
		Random rand = new Random();
		int bla = rand.nextInt(100);
		if(stoppedCountAt < 7)
		{
			if(bla < 66)
			{
				winCheck = true;
			}
			else
			{
				winCheck = false;
			}
		}
		return winCheck;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.quest_main, menu);
		return true;
	}

}
