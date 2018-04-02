<?php

class Soke {

	public function __construct() {
		
	}

	 public function __destruct() {

	}

	public function init($vals){}

	public function initPAKE($h1){
		// ECC parameters
		$gprimeX = gmp_init("8da36f68628a18107650b306f22b41448cb60fe5712dd57a", 16);
		$gprimeY = gmp_init("1f64a649852124528a09455de6aad151b4c0a9a8c2e8269c", 16);
		$g = NISTcurve::generator_192();
		
		// Y <- yG
		$serverECDH = new EcDH($g);
		$serverPub = $serverECDH->getPublicPoint();
		
		#error_log("Client point: " . $_REQUEST['clientMsg']);
		
		// X
		$clientPub = Point::decodePoint(NISTcurve::curve_192(), $_REQUEST['clientMsg']);
		
		// Z <- yX
		$serverECDH->setPublicPoint($clientPub);
		$serverECDH->calculateKey();
		$Z = $serverECDH->getAgreedKey();
		
		// Y^* <- Y + (h mod n) G'
		$gprime = new Point($g->getCurve(), $gprimeX, $gprimeY);
		$hGprime = Point::mul(gmp_init($h1,16),$gprime);
		$ystar = Point::add($serverPub,$hGprime);

		return array("m" => $ystar->getEncodedPoint($g->getOrder()),
				"secret" => $Z->getEncodedPoint($g->getOrder()),
				"k" => true);
	}

}

?>
