package uk.ac.surrey.sccs.pow.app;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Util {

	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

	public static String byteArrayToHexString(byte[] in){
		String result = "";
		for (byte b : in) {
			result += String.format("%02x", b);
		}
		return result;
	}
	
	public static String getStackTrace(Throwable throwable) {
		final StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
	
}
