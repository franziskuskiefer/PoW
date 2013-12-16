package de.franziskuskiefer.pow;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.math.ec.ECPoint;

import android.util.Log;
import de.franziskuskiefer.android.httplibrary.Util;

public class Soke {
	
	private final static BigInteger Mx = new BigInteger("8da36f68628a18107650b306f22b41448cb60fe5712dd57a", 16);
	private final static BigInteger My = new BigInteger("1f64a649852124528a09455de6aad151b4c0a9a8c2e8269c", 16);
	
	private final static X9ECParameters secp129r1 = NISTNamedCurves.getByName("P-192");
	private final static ECPoint M = secp129r1.getCurve().createPoint(Mx, My, false);
	
	// public key
	private ECPoint X;
	// secret key
	private BigInteger x;
	
	public Soke() {
		// TODO Auto-generated constructor stub
	}
	
	private byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public String next(String YString, String pwd, String salt, String trans) {
		
		// Y
		// XXX: we only accept affine X strings for now
		ECPoint Y = secp129r1.getCurve().decodePoint(hexStringToByteArray(YString));
		
		// hash password
		String h = hashPwd(pwd, salt);
		BigInteger hNum = new BigInteger(h, 16).mod(secp129r1.getN());
		
		// Y <- Y - hM
		Y = Y.add(M.multiply(hNum).negate());
		
		// Z <- xY
		ECPoint key = Y.multiply(this.x);
		
		return hashedKey("certHash", Util.byteArrayToHexString(key.getEncoded()), trans, h);
	}

	public String init(){

		SecureRandom secureRandom = new SecureRandom();
		do {
			x = new BigInteger(secp129r1.getN().bitLength(), secureRandom);
		} while (x.equals(BigInteger.ZERO) || x.compareTo(secp129r1.getN()) >= 0);
		X = secp129r1.getG().multiply(x);
		Log.d("POW", "x: "+x.toString());
		Log.d("POW", "Xx: "+X.getX().toBigInteger().toString(16));
		Log.d("POW", "Xy: "+X.getY().toBigInteger().toString(16));

		return "04"+X.getX().toBigInteger().toString(16)+X.getY().toBigInteger().toString(16);
	}
	
	private String hashPwd(String pwd, String salt) {
		PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
		generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(pwd.toCharArray()), salt.getBytes(), 1000);
		KeyParameter key = (KeyParameter)generator.generateDerivedMacParameters(256);

		String hashedPwd = Util.byteArrayToHexString(key.getKey());
		Log.d("POW", "hashedPwd: "+hashedPwd);

		return hashedPwd;
	}
	
	private String hashedKey(String certHash, String sharedSecret, String trans, String pwdHash){
		MessageDigest md;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("POWServerHello=");
			sb.append(trans);
			sb.append("&passHash=");
			sb.append(pwdHash);
			sb.append("&certHash=");
			sb.append(certHash);
			sb.append("&sharedSecret=");
			sb.append(sharedSecret);
			
			md = MessageDigest.getInstance("SHA-256");
			md.update(sb.toString().getBytes());
			byte[] digest = md.digest();
			
			md.update(Util.byteArrayToHexString(digest).getBytes());
			md.update("&auth1".getBytes());
			digest = md.digest();
			
			return Util.byteArrayToHexString(digest);
		} catch (NoSuchAlgorithmException e) {
			Log.d("POW", e.getLocalizedMessage());
			e.printStackTrace();
		}

		return null;
	}

}
