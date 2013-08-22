package de.franziskuskiefer.pow;

import java.io.InputStream;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import de.franziskuskiefer.android.httplibrary.Callback;
import de.franziskuskiefer.android.httplibrary.HttpGet;

public class Setup implements Callback {

	Activity activity;
	
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
	}
	
	public void init(){
        // get custom data from webpage
		Uri data = this.activity.getIntent().getData();
        if (data != null) {
        	String scheme = data.getScheme(); // pow
        	String jsonUrl = data.toString().substring(scheme.length()+3);
        	
        	String serverUrl = jsonUrl.substring(0, jsonUrl.lastIndexOf("/"));
        	((TextView)this.activity.findViewById (R.id.hostAddress)).setText (serverUrl);
        	
        	// get json config
        	new HttpGet(this).execute(jsonUrl);
        }        
	}
	
	@Override
	public void finished(String s) {

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
	
}
