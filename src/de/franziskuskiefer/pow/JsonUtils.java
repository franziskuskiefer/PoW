package de.franziskuskiefer.pow;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

	public static String addElement(String json, String key, String value){
		try {
			JSONObject o = new JSONObject(json);
			o.put(key, value);
			return o.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
}
