<?php
	
	$MYSQL_HOST = '127.0.0.1';
	$MYSQL_USERNAME = 'pow';
	$MYSQL_PASSWORD = 'fhWbWHb=L`.rU_0';
	$MYSQL_DATABASE = 'pow';
	
	include 'pbkdf2.php';
	
	function getCertInfo() {
#		$cert = $_SERVER['SSL_SERVER_CERT'];
		$cert = '-----BEGIN CERTIFICATE-----
MIIHXzCCBkegAwIBAgIHBQgVDmwX5DANBgkqhkiG9w0BAQsFADCBjDELMAkGA1UE
BhMCSUwxFjAUBgNVBAoTDVN0YXJ0Q29tIEx0ZC4xKzApBgNVBAsTIlNlY3VyZSBE
aWdpdGFsIENlcnRpZmljYXRlIFNpZ25pbmcxODA2BgNVBAMTL1N0YXJ0Q29tIENs
YXNzIDEgUHJpbWFyeSBJbnRlcm1lZGlhdGUgU2VydmVyIENBMB4XDTE1MDExMTE0
MDcyM1oXDTE2MDExMzA4MTQyOVowXjELMAkGA1UEBhMCREUxIDAeBgNVBAMTF3d3
dy5mcmFuemlza3Vza2llZmVyLmRlMS0wKwYJKoZIhvcNAQkBFh5wb3N0bWFzdGVy
QGZyYW56aXNrdXNraWVmZXIuZGUwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIK
AoICAQDLOt6P/TW+TI9Kjd59R+RHwf/6ljHEUbtwYLnug0FnOlocYpRssnu73+6o
ZyVAwnQXpZH692en4yD0JiNt8EjX4LhWs5V9SGKB4X4QBRLd3LYzQFl4kzulYv0c
QH+1L+cGydVkg2HIcT8+Ed9bXUQ3pdXfaKPPSCMz6M+rY565sWLhqS0Zod3Cpnk0
LVMhfMIzAUi0jf1LFVUGuSckAAx7VilVXiPkixMxtcAfSkEfX2+Iehz1Q8EeBXJu
akdmcOw2yRM3uGLZ5aDOXrznoWQ4miYf7sYCRoWGSjHoAijSTkb3ft7G6HIGRe1g
7WmmfEDLz3PjG6TNvkR5HmmlL5LeVVPkS5EzR2iTc8RCBJIeG7suw4EYaiAZ2jAv
JWSf2lCoqK+KXjLRQyAp/dfj+E5gsY02k9eTWs7nx7O6VsAA8yFDz27k5xBqkZbl
5KjjFn/6s3/Df7vanIdEGtJ6mXJfzB1URCbgAhtyKTmJWjYbg+F4PUuD+OaXo8dr
TgdSiUcArKDPDZPEV1Otm/n4Jf0KqxP2YAySjOXNQ/i/g4Rpb5OkMtu40DJLnKvX
pmxOH87xJG7e40+22cwxwM9Xb0/rNRDjTnwesB3RBrimdw6j+/n1RA0ne54YXrxl
lemscLmSU341bNoyBYwyWtYLb2Q+LnYd1tVnp28AJBStTTfpcwIDAQABo4IC8TCC
Au0wCQYDVR0TBAIwADALBgNVHQ8EBAMCA6gwEwYDVR0lBAwwCgYIKwYBBQUHAwEw
HQYDVR0OBBYEFAD4T38/lVskLzRAz27GwAZmUTDyMB8GA1UdIwQYMBaAFOtCNNCY
sKuf9BtrCPfMZC7vDixFMDcGA1UdEQQwMC6CF3d3dy5mcmFuemlza3Vza2llZmVy
LmRlghNmcmFuemlza3Vza2llZmVyLmRlMIIBVgYDVR0gBIIBTTCCAUkwCAYGZ4EM
AQIBMIIBOwYLKwYBBAGBtTcBAgMwggEqMC4GCCsGAQUFBwIBFiJodHRwOi8vd3d3
LnN0YXJ0c3NsLmNvbS9wb2xpY3kucGRmMIH3BggrBgEFBQcCAjCB6jAnFiBTdGFy
dENvbSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTADAgEBGoG+VGhpcyBjZXJ0aWZp
Y2F0ZSB3YXMgaXNzdWVkIGFjY29yZGluZyB0byB0aGUgQ2xhc3MgMSBWYWxpZGF0
aW9uIHJlcXVpcmVtZW50cyBvZiB0aGUgU3RhcnRDb20gQ0EgcG9saWN5LCByZWxp
YW5jZSBvbmx5IGZvciB0aGUgaW50ZW5kZWQgcHVycG9zZSBpbiBjb21wbGlhbmNl
IG9mIHRoZSByZWx5aW5nIHBhcnR5IG9ibGlnYXRpb25zLjA1BgNVHR8ELjAsMCqg
KKAmhiRodHRwOi8vY3JsLnN0YXJ0c3NsLmNvbS9jcnQxLWNybC5jcmwwgY4GCCsG
AQUFBwEBBIGBMH8wOQYIKwYBBQUHMAGGLWh0dHA6Ly9vY3NwLnN0YXJ0c3NsLmNv
bS9zdWIvY2xhc3MxL3NlcnZlci9jYTBCBggrBgEFBQcwAoY2aHR0cDovL2FpYS5z
dGFydHNzbC5jb20vY2VydHMvc3ViLmNsYXNzMS5zZXJ2ZXIuY2EuY3J0MCMGA1Ud
EgQcMBqGGGh0dHA6Ly93d3cuc3RhcnRzc2wuY29tLzANBgkqhkiG9w0BAQsFAAOC
AQEAh9fF8B6GG9GPmVLOxyFuEIqqtl3QU6gHAhkCVaycOPkZRdcGs1iJnzkV+7rX
yk5sWkZb/u1Ujce4k87d28IVIk6PnFNEEuDQvVux40h7hRbJfpSXxihFfJW9/k+R
aNlSqihgPHZXuBTqDsRCNu8PPFbbYyMJkSmBureE8vd9qNo6hvNqt5hf/9Tot9c6
obEtrP3JIh8AIelG6lKhzropZa6Zu2j9t0bTn2SxLVH8zy6TA7dQ40TkZ9hBBmux
bwAM/U0L7X9pvZjw3xdtByz0tuXDgZCqlvRrXKf+ZnW0gk0xL9Rmx6tZtF3QohX4
pABhLTCIkwdjLp4A+Zdg+YEGGQ==
-----END CERTIFICATE-----';
		openssl_x509_export($cert, $output, true);
		$output = preg_replace('/\-+BEGIN CERTIFICATE\-+/','',$output);
		$output = preg_replace('/\-+END CERTIFICATE\-+/','',$output);
		$output = trim($output);
		$output = str_replace( array("\n\r","\n","\r"), '', $output);
		$bin = base64_decode($output);
		$hash = sha1($bin);
		$hash = strtolower($hash);
		return $hash;
	}
	
	// initialize ECC library
	function __autoload($f) {
		// load the interfaces first otherwise contract errors occur
		$interfaceFile = "ecc_library/classes/interface/" . $f . "Interface.php";
		if (file_exists($interfaceFile)) {
			require_once $interfaceFile;
		}
		// load class files after interfaces
		$classFile = "ecc_library/classes/" . $f . ".php";
		if (file_exists($classFile)) {
			require_once $classFile;
		}
		// if utilities are needed load them last
		$utilFile = "ecc_library/classes/util/" . $f . ".php";
		if (file_exists($utilFile)) {
			require_once $utilFile;
		}
	}
	
	// set GMP
	if (extension_loaded('gmp') && !defined('USE_EXT')) {
		define ('USE_EXT', 'GMP');
	} else if (extension_loaded('bcmath') && !defined('USE_EXT')) {
		define ('USE_EXT', 'BCMATH');
	}
	
	$link = mysql_connect($MYSQL_HOST, $MYSQL_USERNAME, $MYSQL_PASSWORD);
	mysql_select_db($MYSQL_DATABASE);
	
	// receive the first client message
	if (isset($_REQUEST['msg']) && ($_REQUEST['msg'] === "POWClientExchange")) {
	
		error_log("got POWClientExchange ...");
	
		if (!(isset($_REQUEST['version']) && isset($_REQUEST['sessionID']) && isset($_REQUEST['username']) && isset($_REQUEST['clientPoint']))) {
			$returnvalue = array("msg" => "ERROR",
								 "error" => "Invalid request."
								 );
			$returnvalue = json_encode($returnvalue);
			echo $returnvalue;
			return;
		}
		
		if (!($_REQUEST['version'] === "POW_v1_tSOKE_secp192r1_SHA256_certHash")) {
			$returnvalue = array("msg" => "ERROR_UNSUPPORTED_VERSION",
								 "sessionID" => $_REQUEST['sessionID']
								 );
			$returnvalue = json_encode($returnvalue);
			echo $returnvalue;
			exit;
		}
		
		// look up hash and salt for this account
		$username = $_REQUEST['username'];
		$result = mysql_query("SELECT * FROM users WHERE username='" . mysql_real_escape_string($username) . "'");
		if (mysql_num_rows($result) == 0) {
			$returnvalue = array("msg" => "ERROR_USERNAME_NOT_FOUND",
								 "sessionID" => $_REQUEST['sessionID']
								 );
			$returnvalue = json_encode($returnvalue);
			echo $returnvalue;
			exit;
		}
		$row = mysql_fetch_array($result, MYSQL_BOTH);
		$h1 = $row['hash'];
		$salt = $row['salt'];
		
#		error_log("session: ".$_REQUEST['sessionID']);
		
		$result = mysql_query("SELECT * FROM sessions WHERE sessionID='" . mysql_real_escape_string($_REQUEST['sessionID']) . "'");
		if (mysql_num_rows($result) == 0) {
			$returnvalue = array("msg" => "ERROR_SESSION_NOT_FOUND",
								 "sessionID" => $_REQUEST['sessionID']
								 );
			$returnvalue = json_encode($returnvalue);
#			error_log(print_r($returnvalue, true));
			echo $returnvalue;
			exit;
		}
		$row = mysql_fetch_array($result, MYSQL_BOTH);

		// TIMING
#		$startTime = microtime(true);

		// ECC parameters
		$n = 6277101735386680763835789423176059013767194773182842284081;
		$gprimeX = gmp_init("8da36f68628a18107650b306f22b41448cb60fe5712dd57a", 16);
		$gprimeY = gmp_init("1f64a649852124528a09455de6aad151b4c0a9a8c2e8269c", 16);
		$g = NISTcurve::generator_192();
		
		// Y <- yG
		$serverECDH = new EcDH($g);
		$serverPub = $serverECDH->getPublicPoint();
		
		// X
		$clientPub = Point::decodePoint(NISTcurve::curve_192(), $_REQUEST['clientPoint']);
		
		// Z <- yX
		$serverECDH->setPublicPoint($clientPub);
		$serverECDH->calculateKey();
		$Z = $serverECDH->getAgreedKey();
		
		// Y^* <- Y + (h mod n) G'
		$gprime = new Point($g->getCurve(), $gprimeX, $gprimeY);
		$hGprime = Point::mul(gmp_init($h1,16),$gprime);
		$ystar = Point::add($serverPub,$hGprime);
		
		// compute outgoing message (salt, Y^*)
		$returnvalue = array("msg" => "POWServerExchange",
							 "sessionID" => $_REQUEST['sessionID'],
							 "salt" => $salt,
							 "serverPoint" => $ystar->getEncodedPoint($g->getOrder()),
							 );
						
		$returnvalue = json_encode($returnvalue);
		
		// k <- SHA-256(POWServerHello, POWClientExchange, POWServerExchange, passHash, certHash, sharedSecret)
		$k1  = "POWServerHello=" . rawurlencode($row['msgPOWServerHello']);
		$k1 .= "&POWClientExchange=" . rawurlencode(file_get_contents("php://input"));
		$k1 .= "&POWServerExchange=" . rawurlencode($returnvalue);
		$k1 .= "&passHash=" . $h1;
		$k1 .= "&certHash=" . getCertInfo();
		$k1 .= "&sharedSecret=" . $Z->getEncodedPoint($g->getOrder());
		
		$k1 = hash("sha256", $k1);
		
		// A1 <- SHA-256(K, "auth1")
		$a1 = hash("sha256", $k1 . "&auth1");
		
		// A2 <- SHA-256(K, "auth2")
		$a2 = hash("sha256", $k1 . "&auth2");
		
		// TIMING
#		$stopTime = microtime(true);
#		error_log("time " . 1000 * ($stopTime - $startTime) . "\n");
		
		// store session variables
		mysql_query("UPDATE sessions SET "
					. "username='" . mysql_real_escape_string($username) . "', "
					. "a1='" . mysql_real_escape_string($a1) . "', "
					. "a2='" . mysql_real_escape_string($a2) . "'"
					. " WHERE sessionID='" . mysql_real_escape_string($_REQUEST['sessionID']) . "'");
		
		echo $returnvalue;
		
		// receive the second client message
	} else if (isset($_REQUEST['msg']) && ($_REQUEST['msg'] == "POWClientFinished")) {
	
#		error_log("got POWClientFinished ...");
		
		if (!isset($_REQUEST['auth1']) || !isset($_REQUEST['sessionID'])) {
			$returnvalue = array("msg" => "ERROR", "error" => "Invalid request.");
			$returnvalue = json_encode($returnvalue);
			echo $returnvalue;
			return;
		}
		
		$result = mysql_query("SELECT * FROM sessions WHERE sessionID='" . mysql_real_escape_string($_REQUEST['sessionID']) . "'");
		if (mysql_num_rows($result) == 0) {
			$returnvalue = array("msg" => "ERROR_SESSION_NOT_FOUND",
								 "sessionID" => $_REQUEST['sessionID']
								 );
			$returnvalue = json_encode($returnvalue);
			echo $returnvalue;
			exit;
		}
		$row = mysql_fetch_array($result, MYSQL_BOTH);
		
		$a1 = base64_decode($_REQUEST['auth1']);
		if(strcasecmp($a1, $row['a1']) == 0) {
			setSucc("1");
			$returnvalue = array("msg" => "POWServerFinished",
								 "sessionID" => $_REQUEST['sessionID'],
								 "auth2" => $row['a2'],
								 "successURL" => $row['successURL'],
								 );
			$returnvalue = json_encode($returnvalue);
			echo $returnvalue;
		} else {
			setSucc("0");
			$returnvalue = array("msg" => "ERROR_INVALID_PASSWORD",
								 "sessionID" => $_REQUEST['sessionID']
								 );
			$returnvalue = json_encode($returnvalue);
			echo $returnvalue;
		}
	}
	
	function setSucc($succ){
		mysql_query("UPDATE sessions SET "
								. "success='" . mysql_real_escape_string($succ) . "'"
								. " WHERE sessionID='" . mysql_real_escape_string($_REQUEST['sessionID']) . "'");
	}
	
?>
