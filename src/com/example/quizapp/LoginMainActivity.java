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
import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LoginMainActivity extends Activity {
	
	
	public static final String PREFS_NAME = "MyPrefsFile";
	private static final String PREF_USERID = "UserId";
	private static final String PREF_USERNAME = "Username";
	private static final String PREF_BANK = "Bank";
	
	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://jhl.jobudbud.dk/WebService.asmx";
	private final String SOAP_ACTION = "http://tempuri.org/loginCheck";
	private final String METHOD_NAME = "loginCheck";
	LinearLayout loadSpinner;
	PropertyInfo usernameProp;
	PropertyInfo passwordProp;
	Button loginButton;
	EditText user;
	EditText pass;
	String userNameApp;
	String passWordApp;
	String userNameFromDB;
	int bankFromDB;
	ArrayList<String> userFromDB;
	int userId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_main);
		loadSpinner = (LinearLayout) findViewById(R.id.linelayout);

	}
	
	/**
	 * Henter brugernavn og password ud af app'en og sender dem til verificering igennem AsyncCallLogin.
	 * @param v
	 */
	public void onClickLogin(View v)
	{
		loginButton = (Button)findViewById(R.id.loginBtn);
		loginButton.setClickable(false);
		user = (EditText)findViewById(R.id.editText1);
		pass = (EditText)findViewById(R.id.editText2);
		
		userNameApp = user.getText().toString();
		passWordApp = pass.getText().toString();
		
		if(userNameApp.equals("") || passWordApp.equals(""))
		{
			userNameApp = "Fejl";
			passWordApp = "Fejl";
		}
		
		AsyncCallLogin asyncCallLogin = new AsyncCallLogin();
		
		asyncCallLogin.execute();
	}
	/**
	 * Laver en request der indeholder brugernavn og password, og sender det igennem webservicen.
	 */
	public void Login()
	{
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		
		usernameProp = new PropertyInfo();
		usernameProp.type = usernameProp.STRING_CLASS;
		usernameProp.setName("usname");
		usernameProp.setValue(userNameApp);
		usernameProp.setType(String.class);
		
		request.addProperty(usernameProp);
		
		passwordProp = new PropertyInfo();
		passwordProp.type = passwordProp.STRING_CLASS;
		passwordProp.setName("pass");
		passwordProp.setValue(passWordApp);
		passwordProp.setType(String.class);
		
		request.addProperty(passwordProp);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		
		try {
			androidHttpTransport.call(SOAP_ACTION, envelope);

			SoapObject response = (SoapObject) envelope.bodyIn;
			SoapObject property = (SoapObject)response.getProperty(0);
			int count = property.getPropertyCount();
			userFromDB = new ArrayList<String>();
            for (int i = 0; i < count; i++)
            {
            	//SoapObject property = (SoapObject)response.getProperty(i);
                userFromDB.add(property.getProperty(i).toString());
            }

			userId = Integer.parseInt(userFromDB.get(0));
			userNameFromDB = userFromDB.get(1);
			bankFromDB = Integer.parseInt(userFromDB.get(2));
			
		} 
		catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
	}
	
	/**
	 * Laver et nyt intent, som sender dig videre til MainActivity.
	 */
	public void toMain()
	{
		Intent intent = new Intent(this, MainActivity.class);    	
    	startActivity(intent);
	}
	
	/**
	 * Fejlhåndtere login, ved at lave en alertDialog.
	 */
	
	public void failedLogin()
	{
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

		// Set the message to display
		alertbox.setMessage("Wrong username/password. Please try again.");

		// Add a neutral button to the alert box and assign a click listener
		alertbox.setNeutralButton("OK",
				new DialogInterface.OnClickListener() {

					// Click listener on the neutral button of alert box
					public void onClick(DialogInterface arg0, int arg1) {
						// The neutral button was clicked
					}
				});

		// show the alert box
		alertbox.show();
	}
	
	/**
	 *	Den asyncrone klasse der kalder metoden Login, og fejlhåndtere hvis login er forkert, ved at kalde failedLogin().
	 *	Sætter desuden userid ind i preferences.
	 */
	private class AsyncCallLogin extends AsyncTask<String, Void, Void>
	{

		@Override
		protected Void doInBackground(String... params) {
			Login();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(userId == 999)
			{
				failedLogin();
				loginButton.setClickable(true);
			}
			
			else
			{
				getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt(PREF_USERID, userId).commit();
				getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_USERNAME, userNameFromDB).commit();
				getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt(PREF_BANK, bankFromDB).commit();
				loadSpinner.setVisibility(View.GONE);
				
				toMain();
				
			}
		}

		@Override
		protected void onPreExecute() {
			loadSpinner.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login_main, menu);
		return true;
	}

}
