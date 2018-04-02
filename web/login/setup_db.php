<?php
	
	$MYSQL_HOST = '127.0.0.1';
	$MYSQL_USERNAME = 'root';
	$MYSQL_PASSWORD = '';
	$MYSQL_DATABASE = 'pow';
	
	include 'pbkdf2.php';
	
	$link = mysql_connect($MYSQL_HOST, $MYSQL_USERNAME, $MYSQL_PASSWORD);
	mysql_select_db($MYSQL_DATABASE);
	
	mysql_query("DROP TABLE IF EXISTS users");
	mysql_query("CREATE TABLE users (username TEXT, hash TEXT, salt TEXT)");
	
	function addUser($username, $password, $salt) {
		$hash = pbkdf2("sha256", $password, $salt, 1000, $dkLen = false, $raw_output = false);
		mysql_query("INSERT INTO users (username, hash, salt) VALUES ("
					. "'" . mysql_real_escape_string($username) . "', "
					. "'" . mysql_real_escape_string($hash) . "', "
					. "'" . mysql_real_escape_string($salt) . "'"
					. ")");
	}
	
	addUser("alice", "password", "5031D37EECAD88D5");
	addUser("bob", "12345678", "e734ed5308e33bb9");
	addUser("charlie", "catlover", "2a4c8f590ff5c382");
	
	mysql_query("DROP TABLE IF EXISTS sessions");
	mysql_query("CREATE TABLE sessions (sessionID TEXT, msgPOWServerHello TEXT, username TEXT, a1 TEXT, a2 TEXT, successURL TEXT, timestamp TIMESTAMP)");
	
?>
