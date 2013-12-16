package de.franziskuskiefer.pow;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import de.franziskuskiefer.android.httplibrary.Callback;
import de.franziskuskiefer.android.httplibrary.HttpGet;

public class Setup extends AsyncTask<String, Void, Boolean> implements Callback {

	private Activity activity;
	private ProgressDialog dialog;
	
	// params
	private String[] versions;
	private String msg;
	private String sessionID;
	private String authURL;
	private String forgotURL;
	private String sitename;
	private String description;
	private String imgURL;
	private String usernameLabel;
	private String passwordLabel;
	private String numPwd;
	
	public Setup(Activity activity) {
		this.activity = activity;
		dialog = new ProgressDialog(activity);
	}

	@Override
	protected Boolean doInBackground(String... params) {
		init();
		return false;
	}
	
	@Override
	protected void onPreExecute() {
		this.dialog.setMessage("Get Parameters ...");
        this.dialog.show();
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		if (dialog.isShowing()) {
            dialog.dismiss();
        }
	}
	
	private void init(){
        // get custom data from webpage
		Uri data = this.activity.getIntent().getData();
		Soke soke = new Soke();
		
		Log.d("POW", soke.init());
		String salt = "5031D37EECAD88D5";
		String pwd = "password";
		String YString = "048da36f68628a18107650b306f22b41448cb60fe5712dd57a1f64a649852124528a09455de6aad151b4c0a9a8c2e8269c";
		Log.d("POW", soke.next(YString, pwd, salt, ""));
		
        if (data != null) {
        	Log.d("POW", "Params: "+data.toString());
			String queryString = data.getQuery();

			if (queryString != null) {
				final String[] arrParameters = queryString.split("&");
				for (final String tempParameterString : arrParameters) {
					final String[] arrTempParameter = tempParameterString.split("=");
					if (arrTempParameter.length == 2) {
						final String parameterKey = arrTempParameter[0];
						final String parameterValue = arrTempParameter[1];
						// do something with the parameters
						Log.d("POW", parameterKey+": "+parameterValue);
					}
				}
			}
        	
//        	// get json config
//        	new HttpGet(this).execute(jsonUrl);
        }        
	}
	
	private void handleInitResult(String s){
		try {
			JSONObject json = new JSONObject(s);
			
			// protocol versions
			final JSONArray keyArray = json.getJSONArray("versions");
			this.versions = new String[keyArray.length()];
			for(int i = 0; i < keyArray.length(); i++) {
			    this.versions[i] = keyArray.getString(i);
			}
			
			this.msg = json.getString("msg");
			this.sessionID = json.getString("sessionID");
			this.authURL = json.getString("authURL");
			this.forgotURL = json.getString("forgotURL");
			this.sitename = json.getString("sitename");
			this.description = json.getString("description");
			this.imgURL = json.getString("imgURL");
			this.usernameLabel = json.getString("usernameLabel");
			this.passwordLabel = json.getString("passwordLabel");
			this.numPwd = json.getString("numPwd");
		
			// set branding
				// set description
        	((TextView)activity.findViewById (R.id.serverParams)).setText (description);
				// set image
        	new Branding().execute(imgURL);
        	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void finished(String s) {

	}

	@Override
	public void finished(HashMap<String, String> s) {
		handleInitResult(s.get("Result"));
		System.out.println("Fingerprint: "+s.get("Fingerprint"));
		System.out.println("Result: "+s.get("Result"));
		String initVals = JsonUtils.addElement(s.get("Result"), "password", "password");
//		((MainActivity)this.activity).getSpake().init(JsonUtils.addElement(initVals, "certHash", s.get("Fingerprint")));
	}

	
	private class Branding extends AsyncTask<String, Void, Drawable> {

		@Override
		protected Drawable doInBackground(String... params) {
			Drawable brandingImage = loadImageFromWeb(params[0]);
			return brandingImage;
		}
		
		@Override
		protected void onPostExecute(Drawable result) {
			((ImageView)activity.findViewById(R.id.brandingImage)).setImageDrawable(result);
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


	public String[] getVersions() {
		return versions;
	}

	public String getMsg() {
		return msg;
	}

	public String getSessionID() {
		return sessionID;
	}

	public String getAuthURL() {
		return authURL;
	}

	public String getForgotURL() {
		return forgotURL;
	}

	public String getSitename() {
		return sitename;
	}

	public String getDescription() {
		return description;
	}

	public String getUsernameLabel() {
		return usernameLabel;
	}

	public String getPasswordLabel() {
		return passwordLabel;
	}

	public String getNumPwd() {
		return numPwd;
	}

}
