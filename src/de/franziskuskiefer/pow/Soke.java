package de.franziskuskiefer.pow;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.spongycastle.asn1.nist.NISTNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.math.ec.ECPoint;

import android.util.Log;

public class Soke {
	
	private final static BigInteger Mx = new BigInteger("8da36f68628a18107650b306f22b41448cb60fe5712dd57a", 16);
	private final static BigInteger My = new BigInteger("1f64a649852124528a09455de6aad151b4c0a9a8c2e8269c", 16);
	
	private final static X9ECParameters secp129r1 = NISTNamedCurves.getByName("P-192");
	private final static ECPoint M = secp129r1.getCurve().createPoint(Mx, My, false);
	
	// public key
	private ECPoint X;
	// secret key
	private BigInteger x;
	private String a2 = null;
	
	public Soke() {
		// TODO Auto-generated constructor stub
	}
	
	public String[] next(String YString, String pwd, String salt, String trans, String cert) {
		
		// Y
		// XXX: we only accept affine X strings for now
		ECPoint Y = secp129r1.getCurve().decodePoint(Util.hexStringToByteArray(YString));
		
		// hash password
		String h = hashPwd(pwd, salt);
		BigInteger hNum = new BigInteger(h, 16).mod(secp129r1.getN());
		
		// Y <- Y - hM
		Y = Y.add(M.multiply(hNum).negate());
		
		// Z <- xY
		ECPoint key = Y.multiply(this.x);
		
		return genAuthTokens(cert, Util.byteArrayToHexString(key.getEncoded()), trans, h);
	}

	public String init(){

		SecureRandom secureRandom = new SecureRandom();
		do {
			x = new BigInteger(secp129r1.getN().bitLength(), secureRandom);
		} while (x.equals(BigInteger.ZERO) || x.compareTo(secp129r1.getN()) >= 0);
		this.X = secp129r1.getG().multiply(x);

		return pointToString(this.X);
	}

	private String pointToString(ECPoint X) {
		return "04"+X.getX().toBigInteger().toString(16)+X.getY().toBigInteger().toString(16);
	}
	
	public boolean verifyServer(String serverA2){
		return serverA2 != null && this.a2 != null && serverA2.equals(this.a2);
	}
	
	private String hashPwd(String pwd, String salt) {
		PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
		generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(pwd.toCharArray()), salt.getBytes(), 1000);
		KeyParameter key = (KeyParameter)generator.generateDerivedMacParameters(256);

		String hashedPwd = Util.byteArrayToHexString(key.getKey());
		Log.d("POW", "hashedPwd: "+hashedPwd);

		return hashedPwd;
	}
	
	private String[] genAuthTokens(String certHash, String sharedSecret, String trans, String pwdHash){
		MessageDigest md;
		try {
			byte[] digest = genKey(certHash, sharedSecret, trans, pwdHash);
			
			md = MessageDigest.getInstance("SHA-256");
			
			md.update(Util.byteArrayToHexString(digest).getBytes());
			md.update("&auth1".getBytes());
			String a1 = Util.byteArrayToHexString(md.digest());
			
			// compute second auth token and store it to for later use
			md.update(Util.byteArrayToHexString(digest).getBytes());
			md.update("&auth2".getBytes());
			this.a2 = Util.byteArrayToHexString(md.digest());
			
			// we compute an additional auth token to check if password or certificate was wrong
//			digest = genKey(certHash, "sharedSecret", trans, "pwdHash");
			digest = genKey("cert", sharedSecret, trans, pwdHash);
			md.update(Util.byteArrayToHexString(digest).getBytes());
			md.update("&auth1".getBytes());
			String a1NoPwd = Util.byteArrayToHexString(md.digest());
			
			return new String[]{a1, a1NoPwd};
		} catch (NoSuchAlgorithmException e) {
			Log.d("POW", e.getLocalizedMessage());
			e.printStackTrace();
		}

		return null;
	}

	private byte[] genKey(String certHash, String sharedSecret, String trans, String pwdHash) throws NoSuchAlgorithmException {
		StringBuilder sb = new StringBuilder();
		sb.append("POWServerHello=");
		sb.append(trans);
		sb.append("&passHash=");
		sb.append(pwdHash);
		sb.append("&certHash=");
		sb.append(certHash);
		sb.append("&sharedSecret=");
		sb.append(sharedSecret);
		
		Log.d("POW", "to hash: "+sb.toString());
		
		MessageDigest mdk = MessageDigest.getInstance("SHA-256");
		mdk.update(sb.toString().getBytes());
		return mdk.digest();
	}
	
	public boolean isFinished(){
		return this.a2 != null ? true : false;
	}

}
