package de.franziskuskiefer.pow;

import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import de.franziskuskiefer.android.httplibrary.Callback;

public class MainActivity extends Activity implements OnClickListener, Callback {

//    private Setup setup;
	private SPake spake;
	private Soke soke;
	private String sessionID;
	private String authURL;
	private String description = "Enter your password"; // TODO: get this from the server
	private String imgURL = "http://www.dogwallpapers.net/wallpapers/newfoundland-puppy-wallpaper.jpg"; // TODO: get this from the server
	private String successURL;
	private String TTP;
	private String TTPSOURCE;
	private String errorURL;
	private String trans;
	private String pwd;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListener();
        
//        this.spake = new SPake();
        this.soke = new Soke();
        init();
        
//        this.setup = new Setup(this);
//        this.setup.execute();
    }
	
	private void init(){
        // get custom data from webpage
		Uri data = getIntent().getData();
//		Soke soke = new Soke();
//		
//		Log.d("POW", soke.init());
//		String salt = "5031D37EECAD88D5";
//		String pwd = "password";
//		String YString = "048da36f68628a18107650b306f22b41448cb60fe5712dd57a1f64a649852124528a09455de6aad151b4c0a9a8c2e8269c";
//		Log.d("POW", soke.next(YString, pwd, salt));
		
        if (data != null) {
        	Log.d("POW", "Params: "+data.toString());
			String queryString = data.getQuery();
			URLDecoder decoder = new URLDecoder();
			String decoded = decoder.decode(queryString);
			this.trans = decoded;
			handleInitResult(decoded);
//			handleInitResult(queryString);

//			if (queryString != null) {
//				final String[] arrParameters = queryString.split("&");
//				for (final String tempParameterString : arrParameters) {
//					final String[] arrTempParameter = tempParameterString.split("=");
//					if (arrTempParameter.length == 2) {
//						final String parameterKey = arrTempParameter[0];
//						final String parameterValue = arrTempParameter[1];
//						// do something with the parameters
//						Log.d("POW", parameterKey+": "+parameterValue);
//					}
//				}
//			}
        	
//        	// get json config
//        	new HttpGet(this).execute(jsonUrl);
        }        
	}
	
	private void handleInitResult(String s){
		try {
			JSONObject json = new JSONObject(s);
			
			this.sessionID = json.getString("sessionID");
			this.authURL = json.getString("authURL");
			this.successURL = json.getString("successURL");
			this.TTP = json.getString("TTP");
			this.TTPSOURCE = json.getString("TTPSOURCE");
			this.errorURL = json.getString("errorURL");
		
			// set branding
				// set description
//        	((TextView)findViewById (R.id.serverParams)).setText (description);
				// set image
//        	new Branding().execute(imgURL);
        	
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("POW", e.getLocalizedMessage());
		}
	}
    
    // add listener to login button
    private void addListener(){
    	final Button button = (Button) findViewById(R.id.loginButton);
        button.setOnClickListener(this);
    }


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return false; //not shown
    }

	@Override
	public void onClick(View v) {
		Log.d("POW", "Login Clicked ... ");
		
		this.pwd = ((EditText)findViewById (R.id.password)).getText().toString();
		
		// execute soke init to get first message
		String m = soke.init();
		
		HashMap<String, String> params = new HashMap<String, String>();
		// FIXME
		params.put("msg", "POWClientExchange");
		params.put("version", "POW_v1_tSOKE_2048_SHA256_certHash");
		params.put("sessionID", sessionID);
		params.put("username", ((EditText)findViewById (R.id.username)).getText().toString());
		params.put("clientMsg", m);
		
		new HTTPConnection(this, "post", params).execute(authURL);
	}

	@Override
	public void finished(String s) {
		// TODO Auto-generated method stub
		Log.d("POW", "finished: "+s);
	}

	@Override
	public void finished(HashMap<String, String> arg0) {
//		this.spake.next(JsonUtils.addElement(arg0.get("Result"), "sid", "buildthesid..."));
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
		
		Log.d("POW", "jsonString: "+jsonString);
		
		try {
			JSONObject json = new JSONObject(jsonString);
			String auth = soke.next(json.getString("serverPoint"), pwd, json.getString("salt"), trans);
			
			// go back to browser with final auth token
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.successURL+"?auth1="+auth+"&sessionID="+this.sessionID));
			startActivity(browserIntent);
//			finish();
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("POW", e.getLocalizedMessage());
		}
		
	}

	public class Branding extends AsyncTask<String, Void, Drawable> {

		@Override
		protected Drawable doInBackground(String... params) {
			Drawable brandingImage = loadImageFromWeb(params[0]);
			return brandingImage;
		}
		
		@Override
		protected void onPostExecute(Drawable result) {
			((ImageView)findViewById(R.id.brandingImage)).setImageDrawable(result);
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
