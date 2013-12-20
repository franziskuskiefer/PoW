package de.franziskuskiefer.pow;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import de.franziskuskiefer.android.httplibrary.Callback;
import de.franziskuskiefer.android.httplibrary.HTTPS_POST;

public class MainActivity extends Activity implements OnClickListener, Callback {

	private Soke soke;
	private String sessionID;
	private String authURL;
	private String successURL;
	private String trans;
	private String pwd;
	private String errorURL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.CustomTheme);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.activity_main);
		addListener();
		
		// this.spake = new SPake();
		this.soke = new Soke();
		init();
	}

	private void init() {
		// get custom data from webpage
		Uri data = getIntent().getData();

		if (data != null) {
			Log.d("POW", "Params: " + data.toString());
			String queryString = data.getQuery();
			try {
				String decoded = URLDecoder.decode(queryString, "UTF-8");
				this.trans = decoded;
				handleInitResult(decoded);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.e("POW", e.getLocalizedMessage());
				// can't go anywhere specific as we can not even read the parameters
				finish();
			}
		}
	}

	private void handleInitResult(String s) {
		try {
			JSONObject json = new JSONObject(s);

			this.sessionID = json.getString("sessionID");
			this.authURL = json.getString("authURL");
			this.successURL = json.getString("successURL");
			this.errorURL = json.getString("errorURL");
			
			// TODO: clean up
//			this.TTP = json.getString("TTP");
//			this.TTPSOURCE = json.getString("TTPSOURCE");

		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("POW", e.getLocalizedMessage());
			returnToBrowser(this.errorURL);
		}
	}

	// add listener to login button
	private void addListener() {
		final Button loginButton = (Button) findViewById(R.id.BtnSetupOk);
		loginButton.setOnClickListener(this);

		final Button cancelButton = (Button) findViewById(R.id.BtnSetupCancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void onClick(View v) {
		Log.d("POW", "Login Clicked ... ");

		this.pwd = ((EditText) findViewById(R.id.password)).getText().toString();

		// execute soke init to get first message
		String m = soke.init();

		HashMap<String, String> params = new HashMap<String, String>();
		// FIXME
		params.put("msg", "POWClientExchange");
		params.put("version", "POW_v1_tSOKE_2048_SHA256_certHash");
		params.put("sessionID", sessionID);
		params.put("username", ((EditText) findViewById(R.id.username)).getText().toString());
		params.put("clientMsg", m);

		new HTTPS_POST(this, getApplicationContext(), false, params).execute(authURL);
	}

	@Override
	public void finished(String s) {
		// TODO Auto-generated method stub
		Log.d("POW", "finished: " + s);
	}

	@Override
	public void finished(HashMap<String, String> arg0) {
		// this.spake.next(JsonUtils.addElement(arg0.get("Result"), "sid",
		// "buildthesid..."));
		for (String key : arg0.keySet()) {
			Log.d("POW", key);
		}
		for (String value : arg0.values()) {
			Log.d("POW", value);
		}

		// add sent URI parameters to transcript
		this.trans += "&POWClientExchange=" + arg0.get("Params");

		// get server result
		String jsonString = arg0.get("Result");

		// add result to transcript
		this.trans += "&POWServerExchange=" + Uri.encode(jsonString);

		Log.d("POW", "jsonString: " + jsonString);

		try {
			JSONObject json = new JSONObject(jsonString);
			String auth = soke.next(json.getString("serverPoint"), pwd, json.getString("salt"), trans);

			// go back to browser with final auth token
			if (auth != null)
				returnToBrowser(this.successURL + "?auth1=" + auth + "&sessionID=" + this.sessionID);
			else
				returnToBrowser(this.errorURL);
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("POW", e.getLocalizedMessage());
			returnToBrowser(this.errorURL);
		}

	}
	
	private void returnToBrowser(String url){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
		finish();
	}

	/*
	 * TODO: currently not used
	 */
	public class Branding extends AsyncTask<String, Void, Drawable> {

		@Override
		protected Drawable doInBackground(String... params) {
			Drawable brandingImage = loadImageFromWeb(params[0]);
			return brandingImage;
		}

		@Override
		protected void onPostExecute(Drawable result) {
			// ((ImageView)findViewById(R.id.brandingImage)).setImageDrawable(result);
		}

		private Drawable loadImageFromWeb(String url) {
			try {
				InputStream is = (InputStream) new URL(url).getContent();
				return Drawable.createFromStream(is, ".png");
			} catch (Exception e) {
				return null;
			}
		}

	}
}
