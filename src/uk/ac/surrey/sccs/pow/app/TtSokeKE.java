package uk.ac.surrey.sccs.pow.app;

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

public class TtSokeKE {
	
	private final static BigInteger Mx = new BigInteger("8da36f68628a18107650b306f22b41448cb60fe5712dd57a", 16);
	private final static BigInteger My = new BigInteger("1f64a649852124528a09455de6aad151b4c0a9a8c2e8269c", 16);
	
	private final static X9ECParameters secp129r1 = NISTNamedCurves.getByName("P-192");
	private final static ECPoint M = secp129r1.getCurve().createPoint(Mx, My, false);
	
	// public key
	private ECPoint X;
	// secret key
	private BigInteger x;
	private String a2 = null;
	
	public TtSokeKE() {
		// TODO Auto-generated constructor stub
	}
	
	public String next(String YString, String pwd, String salt, String trans, String domain) {
		
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
		
		try {
			byte[] digest = genKey(domain, Util.byteArrayToHexString(key.getEncoded()), trans, h);
			String k = Util.byteArrayToHexString(digest);
			return k;
		} catch (NoSuchAlgorithmException e) {
			Log.d("POW", e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return null;
	}

	public String init(){

		// XXX: PRNG fix is applied
		SecureRandom secureRandom = new SecureRandom();
		String s = "";
		do {
			do {
				x = new BigInteger(secp129r1.getN().bitLength(), secureRandom);
			} while (x.equals(BigInteger.ZERO) || x.compareTo(secp129r1.getN()) >= 0);
			this.X = secp129r1.getG().multiply(x);

			s = pointToString(this.X);
		} while (s.length() != 98);
		
		return s;
	}

	private String pointToString(ECPoint X) {
		return "04"+X.getX().toBigInteger().toString(16)+X.getY().toBigInteger().toString(16);
	}
	
	private String hashPwd(String pwd, String salt) {
		PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
		generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(pwd.toCharArray()), salt.getBytes(), 1000);
		KeyParameter key = (KeyParameter)generator.generateDerivedMacParameters(256);

		String hashedPwd = Util.byteArrayToHexString(key.getKey());
		if (Util.DEV)
			Log.d("POW", "hashedPwd: "+hashedPwd);

		return hashedPwd;
	}
	
	private byte[] genKey(String domain, String sharedSecret, String trans, String pwdHash) throws NoSuchAlgorithmException {
		StringBuilder sb = new StringBuilder();
		sb.append("POWServerHello=");
		sb.append(trans);
		sb.append("&passHash=");
		sb.append(pwdHash);
		sb.append("&domain=");
		sb.append(domain);
		sb.append("&sharedSecret=");
		sb.append(sharedSecret);
		
		if (Util.DEV)
			Log.d("POW", "to hash: "+sb.toString());
		
		MessageDigest mdk = MessageDigest.getInstance("SHA-256");
		mdk.update(sb.toString().getBytes());
		return mdk.digest();
	}
	
	public boolean isFinished(){
		return this.a2 != null ? true : false;
	}

}
