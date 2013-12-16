package de.franziskuskiefer.pow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import de.franziskuskiefer.android.httplibrary.Callback;
import de.franziskuskiefer.android.httplibrary.Util;

public class HTTPConnection extends AsyncTask<String, Void, HashMap<String, String>> {

	private static final String GET = "GET";
	private static final String POST = "POST";
	private static final String DELETE = "DELETE";
	private static final int CONNECT_TIMEOUT = 15000;
	private static final int READ_TIMEOUT = 10000;
	private static final String HTTPS = "https";
	private String REQUEST_METHOD;
	private Callback caller;
	private HashMap<String, String> parameters = null;
	private String textParameters = null;

	public HTTPConnection(Callback callback, String method){
		this.caller = callback;
		// TODO: make nicer
		if (method.equalsIgnoreCase(GET))
			REQUEST_METHOD = GET;
		else if (method.equalsIgnoreCase(POST))
			REQUEST_METHOD = POST;
		else if (method.equalsIgnoreCase(DELETE))
			REQUEST_METHOD = DELETE;
		else throw new UnsupportedOperationException("Sorry, I don't know the HTTP request method '"+method+"'.");
	}
	
	public HTTPConnection(Callback callback, String method, String textParams){
		this(callback, method);
		this.textParameters = textParams;
	}
	
	/**
	 * 
	 * @param callback to handle the result
	 * @param method POST or GET
	 * @param params only for POST requests
	 */
	public HTTPConnection(Callback callback, String method, HashMap<String, String> params) {
		this(callback, method);
		this.parameters = params;
		
	}

	@Override
	protected HashMap<String, String> doInBackground(String... urls) {
		Log.d("CONNECTOR_DEBUG", "do in background: "+urls[0]);
		// urls come from the execute() call
		try {
			return getRequest(urls[0]);
		} catch (IOException e) {
			HashMap<String, String> result = new HashMap<String, String>();
			result.put("Exception", "Unable to retrieve web page. URL may be invalid.");
			return result;
		}
	}

	// onPostExecute invoke caller's finished
	@Override
	protected void onPostExecute(HashMap<String, String> result) {
		this.caller.finished(result);
	}

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a HashMap or an empty HashMap
	private HashMap<String, String> getRequest(String myurl) throws IOException {
		Log.d("CONNECTOR_DEBUG", "do request");
		// we only handle HTTPS connections here !!!
		if (myurl.toLowerCase(Locale.UK).startsWith(HTTPS)) {
				Log.d("POWDEMO", "url: "+myurl);
				
				HttpsURLConnection conn = (HttpsURLConnection) new URL(myurl).openConnection();
				conn.setReadTimeout(READ_TIMEOUT);
				conn.setConnectTimeout(CONNECT_TIMEOUT);
				conn.setRequestMethod(REQUEST_METHOD);
				conn.setDoInput(true);
				HostnameVerifier verifier = MyHostnameVerifier.getInstance("", "");
				conn.setHostnameVerifier(verifier);

				if (REQUEST_METHOD.equals(GET))
					return doGet(conn);
				else if (REQUEST_METHOD.equals(POST))
					return doPost(conn);
				else // has to be DELETE as we would have thrown an error in the constructor otherwise
					return doDelete(conn);
		}
		
		return new HashMap<String, String>();
	}

	private HashMap<String, String> doPost(HttpsURLConnection conn) throws IOException {
		Log.d("CONNECTOR_DEBUG", "do post");
		Log.d("CONNECTOR_DEBUG", "#params: "+this.parameters.size());
		HashMap<String, String> result = new HashMap<String, String>();
		
		conn.setDoOutput(true);
		
		/* XXX: have to use text/plain to interact with todo api */
		String encodedParams;
		if (this.textParameters != null){
			conn.setRequestProperty("Content-Type", "text/plain"); 
			conn.setRequestProperty("charset", "utf-8");
			
			encodedParams=this.textParameters;
		} else {
			// add parameter list
			List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			Iterator<Entry<String, String>> it = this.parameters.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> val = it.next();
				postParameters.add(new BasicNameValuePair(val.getKey(), val.getValue()));
			}
			
			encodedParams = Util.stream2string(new UrlEncodedFormEntity(postParameters).getContent());
		}
		Log.d("CONNECTOR_DEBUG", "encodedParams0: "+encodedParams);

		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		Log.d("CONNECTOR_DEBUG", "encodedParams: " + Uri.encode(encodedParams));
		writer.write(encodedParams);
		writer.close();
		os.close();
		
		/* response */
		int response = conn.getResponseCode();
		Log.d("CONNECTOR_DEBUG", "The response is: " + response);
		
		Map<String, List<String>> headerFields = conn.getHeaderFields();
		JSONObject headers = new JSONObject();
		for (String key : headerFields.keySet()) {
			try {
				if (key != null)
					headers.put(key, listToJsonArray(headerFields.get(key)));
			} catch (JSONException e) {
				e.printStackTrace();
				Log.e("POWDEMO", "JSONEception ("+Thread.currentThread().getStackTrace()[2].getLineNumber()+") - "+e.getLocalizedMessage());
			}
		}
		result.put("headers", headers.toString());
		
		InputStream is = conn.getInputStream();

		// Convert the InputStream into a string
		String website = Util.stream2string(is);
		result.put("Result", website);
		result.put("Params", Uri.encode(encodedParams));

		return result;
	}
	
	// XXX: we don't support a body here yet ...
	// using URL to send data
	private HashMap<String, String> doDelete(HttpsURLConnection conn) throws IOException {
		InputStream is = null;
		try {
			HashMap<String, String> result = new HashMap<String, String>();
			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();
			Map<String, List<String>> headerFields = conn.getHeaderFields();
			is = conn.getInputStream();

			// Convert the InputStream into a string
			String website = Util.stream2string(is);
//			Log.d("POWDEMO", "json result: "+website);
			result.put("Result", website);
			JSONObject headers = new JSONObject();
			for (String key : headerFields.keySet()) {
				try {
					if (key != null)
						headers.put(key, listToJsonArray(headerFields.get(key)));
				} catch (JSONException e) {
					e.printStackTrace();
					Log.e("POWDEMO", "JSONEception ("+Thread.currentThread().getStackTrace()[2].getLineNumber()+") - "+e.getLocalizedMessage());
				}
			}
			result.put("headers", headers.toString());
			
			Certificate[] serverCertificates = conn.getServerCertificates();
			String fingerprint = Util.getSHA1Fingerprint(serverCertificates[0]);
			result.put("fingerprint", fingerprint);
			
			return result;
		} finally {
			if (is != null) {
				is.close();
			} 
		}
	}

	private HashMap<String, String> doGet(HttpsURLConnection conn) throws IOException {
		InputStream is = null;
		try {
			HashMap<String, String> result = new HashMap<String, String>();
			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();
			Map<String, List<String>> headerFields = conn.getHeaderFields();
			is = conn.getInputStream();

			// Convert the InputStream into a string
			String website = Util.stream2string(is);
//			Log.d("POWDEMO", "json result: "+website);
			result.put("body", website);
			JSONObject headers = new JSONObject();
			for (String key : headerFields.keySet()) {
				try {
					if (key != null)
						headers.put(key, listToJsonArray(headerFields.get(key)));
				} catch (JSONException e) {
					e.printStackTrace();
					Log.e("POWDEMO", "JSONEception ("+Thread.currentThread().getStackTrace()[2].getLineNumber()+") - "+e.getLocalizedMessage());
				}
			}
			result.put("headers", headers.toString());
			
			Certificate[] serverCertificates = conn.getServerCertificates();
			String fingerprint = Util.getSHA1Fingerprint(serverCertificates[0]);
			result.put("fingerprint", fingerprint);
			
			return result;
		} finally {
			if (is != null) {
				is.close();
			} 
		}
	}

	private String listToJsonArray(List<String> in){
			JSONArray list = new JSONArray();
			for (String string : in) {
				list.put(string);
			}
			return list.toString();
	}
	
	private JSONObject listToJsonObject(String name, List<String> in){
		try {
			JSONObject o = new JSONObject();
			JSONArray list = new JSONArray();
			for (String string : in) {
				list.put(string);
			}
			o.put(name, list);
			return o;
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("POWDEMO", "JSONEception ("+Thread.currentThread().getStackTrace()[2].getLineNumber()+") - "+e.getLocalizedMessage());
		}
		
		return null;
	}

}
