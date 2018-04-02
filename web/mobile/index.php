<?php

	include 'config.php';
	include 'pbkdf2.php';
	include 'soke.php';
	
	if (!function_exists('getallheaders')) { 
	    function getallheaders() { 
			$headers = ''; 
			foreach ($_SERVER as $name => $value) { 
				if (substr($name, 0, 5) == 'HTTP_') { 
					$headers[str_replace(' ', '-', ucwords(strtolower(str_replace('_', ' ', substr($name, 5)))))] = $value;
				} 
			} 
			return $headers; 
		}
	} 

	// set GMP
	if (extension_loaded('gmp') && !defined('USE_EXT')) {
		define ('USE_EXT', 'GMP');
	} else if (extension_loaded('bcmath') && !defined('USE_EXT')) {
		define ('USE_EXT', 'BCMATH');
	}

	// open sql connection
	$link = mysql_connect($MYSQL_HOST, $MYSQL_USERNAME, $MYSQL_PASSWORD);
	mysql_select_db($MYSQL_DATABASE);

	// receive client messages
	if (isset($_REQUEST['msg']) && ($_REQUEST['msg'] === "POWClientExchange")) {

		// check that the request isnt missing any substential information
		if (!(isset($_REQUEST['version']) && isset($_REQUEST['sessionID']) && isset($_REQUEST['username']) && isset($_REQUEST['clientMsg']))) {
			$returnvalue = array("msg" => "ERROR", "error" => "Invalid request.");
			$returnvalue = json_encode($returnvalue);
			echo $returnvalue;
			return;
		}
		
		// read session from database
		$row = getSession();
		
		// check if entry exists for requested sessionID
		if (isset($row["msg"]) && $row["msg"] == "ERROR_SESSION_NOT_FOUND") {
			// cancel login if session does not exist
			return array("error" => "I can not act on session ID ".$_REQUEST['sessionID']." I don't know.");
		}

		// get session transcript from db and add incoming message
		$sessionTrans = $row["msgPOWServerHello"];
		$sessionTrans .= "&POWClientExchange=" . rawurlencode(file_get_contents("php://input"));
		
		// look up hash and salt for this account
		$username = $_REQUEST['username'];
		$user = getUser($username);
		
		$h1 = $user['hash'];
		$salt = $user['salt'];
		
		// create tSOKE
		$pake = new soke();
		
		// TIMING
#		$startTime = microtime(true);
		
		$mout = $pake->initPAKE($h1);

		// compute outgoing message (salt, mout)
		$returnvalue = array("msg" => "POWServerExchange",
							 "sessionID" => $_REQUEST['sessionID'],
							 "salt" => $salt,
							 "serverPoint" => $mout["m"],
							 "successURL" => $row['successURL'],
							 );
						
		$returnvalue = json_encode($returnvalue);

		// add outgoing message to session transcript
		$sessionTrans .= "&POWServerExchange=" . rawurlencode($returnvalue);

		// calculate auth tokens and session key (tSOKE KEX)
		$domain = $_SERVER["SERVER_NAME"];
		$auth = calcAuth($sessionTrans, $h1, $mout["secret"], $domain);
		
		// TIMING
#		$stopTime = microtime(true);
#		error_log("time " . 1000 * ($stopTime - $startTime) . "\n");

		storeAuth($auth, $username);
		error_log("stored authentication tokens");
		
		echo $returnvalue;
	} else if (isset($_REQUEST['msg']) && ($_REQUEST['msg'] == "POWClientFinished")) {
	
		error_log("got POWClientFinished ...");
		
		if (!isset($_REQUEST['auth1']) || !isset($_REQUEST['sessionID'])) {
			$returnvalue = array("msg" => "ERROR", "error" => "Invalid request.");
			$returnvalue = json_encode($returnvalue);
			echo $returnvalue;
			return;
		}
		
		$row = getSession($_REQUEST['sessionID']);
		
		if(strcasecmp($_REQUEST['auth1'], $row['a1']) == 0) {
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
	
	// k <- SHA-256(POWServerHello, POWClientExchange, POWServerExchange, passHash, certHash, sharedSecret)
	function calcAuth($sessionTrans, $h1, $secret, $domain){
		$k1  = "POWServerHello=" . $sessionTrans;
		$k1 .= "&passHash=" . $h1;
		$k1 .= "&domain=" . $domain;
		$k1 .= "&sharedSecret=" . $secret;
		$k1 = hash("sha256", $k1);
                
		// A1 <- SHA-256(K, "auth1")
		$a1 = hash("sha256", $k1 . "&auth1");
		// A2 <- SHA-256(K, "auth2")
		$a2 = hash("sha256", $k1 . "&auth2");
		// k <- SHA-256(K, "sessionkey")
		$sk = hash("sha256", $k1 . "&sessionkey");
                
		return array($a1, $a2, $sk);
	}
	
	function calcKey($sessionTrans, $h1, $secret, $domain) {
		$k1  = "POWServerHello=" . $sessionTrans;
		$k1 .= "&passHash=" . $h1;
		$k1 .= "&domain=" . $domain;
		$k1 .= "&sharedSecret=" . $secret;
		return hash("sha256", $k1);
	}

	function setSucc($succ){
		mysql_query("UPDATE sessions SET "
								. "success='" . mysql_real_escape_string($succ) . "'"
								. " WHERE sessionID='" . mysql_real_escape_string($_REQUEST['sessionID']) . "'");
	}

	function storeAuth($auth, $username){ #FIXME: change DB description to use key not a1
		mysql_query("UPDATE sessions SET "
								. "username='" . mysql_real_escape_string($username) . "', "
								. "a1='" . mysql_real_escape_string($auth[0]) . "', "
								. "a2='" . mysql_real_escape_string($auth[1]) . "', "
								. "a3='" . mysql_real_escape_string($auth[2]) . "' "
								. " WHERE sessionID='" . mysql_real_escape_string($_REQUEST['sessionID']) . "'");
	}

	function getSession(){
		$result = mysql_query("SELECT * FROM sessions WHERE sessionID='" . mysql_real_escape_string($_REQUEST['sessionID']) . "'");
		if (mysql_num_rows($result) == 0) {
			$returnvalue = array("msg" => "ERROR_SESSION_NOT_FOUND",
								 "sessionID" => $_REQUEST['sessionID']
								 );
			$returnvalue = json_encode($returnvalue);
			echo $returnvalue;
			exit;
		}
		return mysql_fetch_array($result, MYSQL_BOTH);
	}

	function getUser($username){
		$result = mysql_query("SELECT * FROM users WHERE username='" . mysql_real_escape_string($username) . "'");
		
		// if the user does not exist, create a dummy one and proceed with that -> will fail later
		if (mysql_num_rows($result) == 0) {
			$fp = @fopen('/dev/urandom','rb');
			if ($fp !== FALSE) {
				$hash .= @fread($fp,64);
				$salt .= @fread($fp,64);
				@fclose($fp);
			}
			$returnvalue = array("username" => "INVALID_USER",
								 "hash" => hash("sha256", $hash),
								 "salt" => hash("sha256", $salt)
								 );
			return $returnvalue;
		}
		
		return mysql_fetch_array($result, MYSQL_BOTH);
	}

?>
