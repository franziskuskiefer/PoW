<!DOCTYPE HTML>
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>

<body>

<div id="POW-fe748ad9-37f9-4b56-80a8-5c73b73e3e88" style="display: none;">
<?php
	
	$sessionID = base64_encode(number_format(hexdec(uniqid(true)), 0, '', ''));
	
	$powjson = json_encode(array(
								 "versions" => array("POW_v1_tSOKE_secp192r1_SHA256_certHash"),
								 "msg" => "POWServerHello",
								 "sessionID" => $sessionID,
								 "authURL" => "https://www.franziskuskiefer.de/pow/login/pow.php",
								 "sitename" => "Test POW site",
								 "description" => "Welcome to the test of the POW secure password authentication system. Please enter your username and password.",
								 "imgURL" => "https://developer.cdn.mozilla.net/media/img/mdn-logo-sm.png",
								 "usernameLabel" => "Login name",
								 "passwordLabel" => "Password",
								 // "forgotLabel" => "Who knows?",
								 "forgotURL" => "http://www.example.net/forgotURL",
								 // "failURL" => "http://www.example.org/failURL",
								 ));
	$MYSQL_HOST = '127.0.0.1';
	$MYSQL_USERNAME = 'pow';
	$MYSQL_PASSWORD = 'password';
	$MYSQL_DATABASE = 'pow';
	
	$link = mysql_connect($MYSQL_HOST, $MYSQL_USERNAME, $MYSQL_PASSWORD);
	mysql_select_db($MYSQL_DATABASE);

	mysql_query("INSERT INTO sessions (sessionID, msgPOWServerHello, successURL) VALUES ("
				. "'" . mysql_real_escape_string($sessionID) . "', "
				. "'" . mysql_real_escape_string($powjson) . "', "
				. "'http://www.example.com'"
				. ")");

	echo $powjson;
	
?>
</div>

The rest of the webpage goes here.

</body>

</html>
