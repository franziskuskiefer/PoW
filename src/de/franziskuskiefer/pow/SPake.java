package de.franziskuskiefer.pow;

import java.math.BigInteger;
import java.util.Random;

import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.json.JSONException;
import org.json.JSONObject;

import de.franziskuskiefer.android.httplibrary.Util;

public class SPake {

	private BigInteger M, N;
	private String pwd;
	
	// temporary values
	private BigInteger ephSecret;
	private BigInteger ephPublic;
	
	// modp/ietf/2048
	private BigInteger G_p = new BigInteger("32317006071311007300338913926423828248817941241140239112842009751400741706634354222619689417363569347117901737909704191754605873209195028853758986185622153212175412514901774520270235796078236248884246189477587641105928646099411723245426622522193230540919037680524235519125679715870117001058055877651038861847280257976054903569732561526167081339361799541336476559160368317896729073178384589680639671900977202194168647225871031411336429319536193471636533209717077448227988588565369208645296636077250268955505928362751121174096972998068410554359584866583291642136218231078990999448652468262416972035911852507045361090559");
	private BigInteger G_q = new BigInteger("16158503035655503650169456963211914124408970620570119556421004875700370853317177111309844708681784673558950868954852095877302936604597514426879493092811076606087706257450887260135117898039118124442123094738793820552964323049705861622713311261096615270459518840262117759562839857935058500529027938825519430923640128988027451784866280763083540669680899770668238279580184158948364536589192294840319835950488601097084323612935515705668214659768096735818266604858538724113994294282684604322648318038625134477752964181375560587048486499034205277179792433291645821068109115539495499724326234131208486017955926253522680545279");
	private BigInteger G_g = new BigInteger("2");
	private int lambda = 2048;
	
	// the resulting key
	private BigInteger key;
	private String certHash;
	private BigInteger pwdNum;
	
	public SPake() {
		this.M = new BigInteger("3d941d6d9cd4c77719840d6b391a63ca6ded5b5cf6aafeefb9ea530f523039e9c372736a79b7eb022e50029f7f2cb4fb16fd1def75657288eca90d2c880f306be76fe0341b3c8961ae6e61aabbb60e416069d97eeada2f1408f2017449dddcd5ac927f164b1a379727941bd7f2170d02ef12ef3ec801fae585ac7b9d4079f50feced64687128208d46e3e10c5d78eb05832f5322c07a3b4e14c6f595206fde99115e8eea19b5fb13dd434332ec3eccb41a4baa54a14183c3416313678697db8507abdcfc6a97c86099fa5172316d784c6997fc2e74e8e59c7c1bc90426164682f5bfbf6373b13ea90d7e13fbffd65e10c4ad96c38ccbf8e8def28d76746729dc", 16);
		this.N = new BigInteger("49bb3b5f7a3d9b500d36366e9935c11b7e159d36696b93d22d8dd742f1386f5d352caeca7e0891acfae0ed5856362e09428bf728f07f4a20092a4473497bebc58f5a1bf2a789f391ab3253cc23ec3f282fdb86a155c82d0f2fd8ba17f6a68d4a8afddaeef8bf155d3bca4b30de36be6a8652ee59f7239dbbc3f12c8c40998f3192271520414096ed726569f7b6b18f2198e87db6bcdf0b8bfd3da77f613a7db1e032b14bed73aa3d4c14cb4cc3f2f7b35feaf82685eb67a73217fec27c62e7e88523ea1c51352d5f42cb63dc1e9d128fe5a95252c01d2eda32b9843779dbaa332f557f0619265dfbaa920f9ce8003062275a367742b776999edc8ab1f766ba6c", 16);
	}
	
	public void init(String initVals){
		try {
			JSONObject json = new JSONObject(initVals);
			
			this.pwd = json.getString("password");
			this.certHash = json.getString("certHash");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public BigInteger next(String m){
System.out.println("min: "+m);
		try {
			
			JSONObject json = new JSONObject(m);
			
			String msg = json.getString("serverPoint");
			String salt = json.getString("salt");
//			String sid = json.getString("sid");
			
			// compute password hash
			this.pwd = hashPwd(this.pwd, salt);
			this.pwdNum = new BigInteger(this.pwd, 16);
			
			this.ephSecret = new BigInteger(lambda , new Random());
			this.ephPublic = createMessage(this.M);

			BigInteger min = new BigInteger(msg, 16);
			this.key = computeKey(this.N, min);
			System.out.println("Key: "+this.key);
//			this.key = hashedKey(min, this.ephPublic);
			
			return this.ephPublic;
		} catch (JSONException e){
			e.printStackTrace();
		}
		
		return null;
	}

	private String hashPwd(String pwd, String salt) {
		PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
		generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(pwd.toCharArray()), salt.getBytes(), 1000);
		KeyParameter key = (KeyParameter)generator.generateDerivedMacParameters(256);

		String hashedPwd = Util.byteArrayToHexString(key.getKey());
		System.out.println("hashedPwd: "+hashedPwd);

		return hashedPwd;
	}

	private BigInteger createMessage(BigInteger mask){
		return this.G_g.modPow(this.ephSecret, this.G_p).multiply(mask.modPow(this.pwdNum, this.G_p)).mod(this.G_p);
	}

	private BigInteger computeKey(BigInteger publicValue, BigInteger min){
		BigInteger NPW = publicValue.modPow(this.pwdNum, this.G_p);
		return min.multiply(NPW.modPow(new BigInteger("-1"), this.G_p)).modPow(this.ephSecret, this.G_p);
	}
	
//	private BigInteger hashedKey(BigInteger min, BigInteger pubVal){
//		MessageDigest md;
//		try {
//			md = MessageDigest.getInstance("SHA-256");
//			md.update(this.sessionID.getBytes());
//			md.update(pubVal.byteValue());
//			md.update(min.byteValue());
//			md.update(this.key.byteValue());
//			md.update(this.pwdNum.byteValue());
//			return (new BigInteger(md.digest())).abs();
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		}
//		
//		return null;
//	}
}