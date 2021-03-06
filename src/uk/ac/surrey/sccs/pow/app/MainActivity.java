package uk.ac.surrey.sccs.pow.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.Security;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import de.franziskuskiefer.android.httplibrary.Callback;
import de.franziskuskiefer.android.httplibrary.async.HTTPS_POST;

public class MainActivity extends Activity implements OnClickListener, OnKeyListener, Callback {
	
	private static final String PERSONALISED = "personlised";
	final String IMAGE_FILE = "image.png";

	static {
		Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}

	private Soke soke;
	private String sessionID;
	private String authURL;
	private String trans;
	private String iniTrans;
	private String pwd;
	private boolean sokeStarted = false;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// apply fix for PRNG (necessary for pre Android 4.3)
		PRNGFixes.apply();
		
		setTheme(R.style.CustomTheme);
		setContentView(R.layout.activity_main);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		addListener();

		String image = isPersonalised();
		Log.d("POW", "checking personalisation ...");
		if (image == null) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
		    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
			startActivityForResult(Intent.createChooser(intent, "Select Background"), 123);
		} else {
			loadImage();
		}
		
		// add key listener to enter key
		((EditText) findViewById(R.id.password)).setOnKeyListener(this);
		
		// this.spake = new SPake();
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
				// XXX: chrome seems to append = at the end, we have to remove that
				if (decoded.charAt(decoded.length()-1) == '=')
					decoded = decoded.substring(0, decoded.length()-1);
				this.iniTrans = decoded;
				handleInitResult(decoded);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.e("POW", e.getLocalizedMessage());

				parameterError();
			}
		}
	}

	private void handleInitResult(String s) {
		try {
			JSONObject json = new JSONObject(s);
			
			Log.d("POW", json.toString());

			this.sessionID = json.getString("sessionID");
			this.authURL = json.getString("authURL");
			Log.d("POW", "auth URL: "+this.authURL);
			
			new Branding(this).execute(json.getString("branding"));
			getActionBar().setTitle(json.getString("url"));
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("POW", e.getLocalizedMessage());

			parameterError();
		}
	}
	
	// add listener to login button
	private void addListener() {
		final Button loginButton = (Button) findViewById(R.id.BtnSetupOk);
		loginButton.setOnClickListener(this);

//		final Button cancelButton = (Button) findViewById(R.id.BtnSetupCancel);
//		cancelButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				finish();
//			}
//		});
	}

	@Override
	public void onClick(View v) {

		startSoke();
	}

	private void startSoke() {
		if (!sokeStarted){
			// avoid starting this more than once
			sokeStarted = true;
			// show progress dialogue
			pd = ProgressDialog.show(this, "Login", "Authenticating ...", false, false);
			
			this.pwd = ((EditText) findViewById(R.id.password)).getText().toString();
			
			// Initialize / reset transcript
			this.trans = this.iniTrans;
			
			// execute tSoke init to get first message
			this.soke = new Soke();
			String m = soke.init();
			HashMap<String, String> params = buildFirstMessage(m);
			Log.d("POW", "params for init message");
			for (Entry<String, String> s : params.entrySet()) {
				Log.d("POW", s.getKey() +" - "+s.getValue());
			}
			
			new HTTPS_POST(this, getApplicationContext(), true, params).execute(authURL);
		}
	}

	private HashMap<String, String> buildFirstMessage(String m) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("msg", "POWClientExchange");
		params.put("version", "POW_v1_tSOKE_2048_SHA256_certHash");
		params.put("sessionID", sessionID);
		params.put("username", ((EditText) findViewById(R.id.username)).getText().toString());
		params.put("clientMsg", m);
		return params;
	}
	
	private HashMap<String, String> buildFinalMessage(String[] a1) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("msg", "POWClientFinished");
		params.put("version", "POW_v1_tSOKE_2048_SHA256_certHash");
		params.put("sessionID", sessionID);
		params.put("auth1", a1[0]);
		params.put("auth1NoPwd", a1[1]);
		return params;
	}

	@Override
	public void finished(String s) {
		// TODO this should never be called!
		Log.d("POW", "finished: " + s);
	}

	@Override
	public void finished(HashMap<String, String> arg0) {
		// this.spake.next(JsonUtils.addElement(arg0.get("Result"), "sid", "buildthesid..."));
		
		// XXX: debugging
		for (String key : arg0.keySet()) {
			Log.d("POW", key);
		}
		for (String value : arg0.values()) {
			Log.d("POW", value);
		}

		if (soke.isFinished()) {
			// this is the server's finished message
			// get server result
			String jsonString = arg0.get("Result");

			Log.d("POW", "jsonString: " + jsonString);
			if (jsonString == null || jsonString.equals("")){
				sokeError();
			}

			JSONObject json;
			try {
				json = new JSONObject(jsonString);
				try {
					
//					String msg = json.getString("msg");
//					if (msg != null && msg.contains("ERROR")){
//						// something went wrong on the server side (maybe he couldn't verify a1)
//						sokeError();
//					} else {
						String a2 = json.getString("auth2");
						if (soke.verifyServer(a2)){
							// verified the server -> tSoke was successful
							// get authentication token and success URL and return to browser
							String a3 = json.getString("auth3");
							String successURL = json.getString("successURL");
							successURL += "?auth3=" + a3 + "&sessionID=" + this.sessionID;
							returnToBrowser(successURL);
						} else {
							// could not verify the server -> get me out of here!
							sokeError();
						}
//					}
				} catch (JSONException e) {
					e.printStackTrace();
					Log.d("POW", e.getLocalizedMessage());
					
					// the server sent an error message?
					String errorMsg = json.getString("msg");
					Log.d("POW", "Server error message: "+errorMsg);
					if (errorMsg.equals("ERROR_INVALID_PASSWORD")){
						usernamePwdError();
					} else {
						sokeError();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Log.d("POW", e.getLocalizedMessage());
				
				sokeError();
			}
		} else {
			// this is the server's masked public DH key
			// we have to finish tSoke and calculate authentication tokens
			String cert = arg0.get("fingerprint");
			Log.d("POW", "cert: "+cert);

			// get server result
			String jsonString = arg0.get("Result");

			Log.d("POW", "jsonString: " + jsonString);
			if (jsonString == null || jsonString.equals("")){
				sokeError();
			} else {
				
				JSONObject json;
				try {
					json = new JSONObject(jsonString);
					try {
						String serverPoint = json.getString("serverPoint");
						// add sent URI parameters to transcript
						this.trans += "&POWClientExchange=" + arg0.get("Params");
						// add result to transcript
						this.trans += "&POWServerExchange=" + Uri.encode(jsonString);
						String[] a1 = soke.next(serverPoint , pwd, json.getString("salt"), trans, cert);
						Log.d("POW", "auth token: "+a1[0]+" - "+a1[1]);
						
						// call auth server with auth token A1
						if (a1 != null || a1.length == 2){
							HashMap<String, String> params = buildFinalMessage(a1);
							new HTTPS_POST(this, getApplicationContext(), true, params).execute(authURL);
							//				returnToBrowser(this.successURL + "?auth1=" + auth + "&sessionID=" + this.sessionID);
						} else {
							// this shouldn't happen!
							sokeError();
						}
					} catch (JSONException e) {
						e.printStackTrace();
						Log.d("POW", e.getLocalizedMessage());
						
						// the server sent an error message?
						String errorMsg = json.getString("msg");
						Log.d("POW", "Server error message: "+errorMsg);
						if (errorMsg.equals("ERROR_USERNAME_NOT_FOUND")){
							usernamePwdError();
						} else {
							sokeError();
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Log.d("POW", e.getLocalizedMessage());
					
					sokeError();
				}
			}

		}

	}
	
	private void returnToBrowser(String url){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
		
		resetProgress();
		
		Log.d("POW", "opening "+url+" in browser.");
		finish();
	}

	/*
	 * add icon to action bar
	 */
	public class Branding extends AsyncTask<String, Void, Drawable> {

		private Activity activity;

		public Branding(Activity a) {
			this.activity = a;
		}
		
		@Override
		protected Drawable doInBackground(String... params) {
			Drawable brandingImage = loadImageFromWeb(params[0]);
			return brandingImage;
		}

		@Override
		protected void onPostExecute(Drawable result) {
			activity.getActionBar().setIcon(result);
			activity.getActionBar().setDisplayShowHomeEnabled(true);
		}

		private Drawable loadImageFromWeb(String url) {
			try {
				Log.d("POW", "img url: "+url);
				InputStream is = (InputStream) new URL(url).getContent();
				return Drawable.createFromStream(is, ".png");
			} catch (Exception e) {
				Log.d("POW", Util.getStackTrace(e));
				return null;
			}
		}

	}
	
	private void resetProgress() {
		if (pd.isShowing()){
			pd.dismiss();
			sokeStarted = false;
		}
	}
	
	private void parameterError() {
		resetProgress();
		
		Alert alert = Alert.newInstance(R.string.parameterError, true);
		alert.show(getFragmentManager(), "dialog");
	}
	
	private void sokeError() {
		resetProgress();
		
		Alert alert = Alert.newInstance(R.string.tsokeError, true);
		alert.show(getFragmentManager(), "dialog");
	}
	
	private void usernamePwdError() {
		resetProgress();
		
		Alert alert = Alert.newInstance(R.string.userError, false);
		alert.show(getFragmentManager(), "dialog");
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 5;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case 123:
				if (resultCode == RESULT_OK) {
					Uri selectedImage = data.getData();
					decodeUri(selectedImage);
					
					// set the app personalised
					setPersonalised(IMAGE_FILE);
				}
		}
	}

	private void saveImage(Bitmap img){
		FileOutputStream outputStream;

		try {
		  outputStream = openFileOutput(IMAGE_FILE, Context.MODE_PRIVATE);
		  ByteArrayOutputStream stream = new ByteArrayOutputStream();
		  img.compress(Bitmap.CompressFormat.PNG, 100, stream);
		  outputStream.write(stream.toByteArray());
		  outputStream.close();
		} catch (Exception e) {
		  e.printStackTrace();
		}
	}
	
	private void loadImage(){
		ParcelFileDescriptor parcelFD = null;
		try {
			File file = new File(getFilesDir(), IMAGE_FILE);
			parcelFD = getContentResolver().openFileDescriptor(Uri.fromFile(file), "r");
			FileDescriptor imageSource = parcelFD.getFileDescriptor();

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			Bitmap bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);

			Log.d("POW", "loading image ...");
			// display image
			((ImageView)findViewById(R.id.personalised)).setImageBitmap(bitmap);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (parcelFD != null)
				try {
					parcelFD.close();
				} catch (IOException e) {
					// ignored
				}
		}

	}
	
	private void decodeUri(Uri uri) {
		ParcelFileDescriptor parcelFD = null;
		try {
			parcelFD = getContentResolver().openFileDescriptor(uri, "r");
			FileDescriptor imageSource = parcelFD.getFileDescriptor();

			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(imageSource, null, o);

			// the new size we want to scale to
			final int REQUIRED_SIZE = 1024;

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE) {
					break;
				}
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			Bitmap bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);

			// display image
			((ImageView)findViewById(R.id.personalised)).setImageBitmap(bitmap);

			// save image
			saveImage(bitmap);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (parcelFD != null)
				try {
					parcelFD.close();
				} catch (IOException e) {
					// ignored
				}
		}
	}

	private String isPersonalised() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getString(PERSONALISED, null);
	}
	
	private void setPersonalised(String s) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString(PERSONALISED, s);
		editor.commit();
	}
	
	private void resetPersonalisation() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.remove(PERSONALISED);
		editor.commit();
	}

	/**
	 * adding & handling the menu
	 */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.reset_app:
	        	resetPersonalisation();
	        	finish();
	            return true;
	        case R.id.about:
	        	showAbout();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void showAbout() {
		AboutDialog about = new AboutDialog(this);
		about.setTitle("About POW");
		about.show();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
           startSoke();
		
        return false;
	}
}
