package de.franziskuskiefer.pow;

import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.conn.ssl.StrictHostnameVerifier;

import android.util.Log;
import android.widget.Toast;

public class MyHostnameVerifier implements HostnameVerifier {

	private StrictHostnameVerifier verifier = new StrictHostnameVerifier();
	private static MyHostnameVerifier instance = new MyHostnameVerifier();
	
	private static String user;
	private static String pwd;
	
	private byte[] sslID = null;
	
	public static HostnameVerifier getInstance(String username, String password) {
		user = username;
		pwd = password;
		return instance;
	}
	
	private MyHostnameVerifier() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean verify(String arg0, SSLSession arg1) {
		Log.d("POWDEMO", "SSL Session ID: "+Arrays.toString(arg1.getId()));
		
		boolean correctHostname = true;
		if (sslID == null){
			Log.d("POWDEMO", "First Run ! -> Set sslID");
			sslID = arg1.getId(); // TODO: perform PAKE
		} else if (!Arrays.equals(sslID, arg1.getId())) {
			// TODO: perform new PAKE
			if (false) // if that throws an error -> MiTM attack
				correctHostname = false;
		}
		// FIXME: can't do this now as the cert IS wrong (need a new server)
//		correctHostname &= verifier.verify(arg0, arg1);

		Log.d("POWDEMO", "Hostname verification: "+verifier.verify(arg0, arg1));
		Log.d("POWDEMO", "SSL Old Session ID: "+Arrays.toString(this.sslID));
		
		return correctHostname;
	}

}
