<?php
	/**
	 * Implementation of the PBKDF2 key derivation function as described in
	 * RFC 2898.
	 *
	 * @param string $PRF Hash algorithm.
	 * @param string $P Password.
	 * @param string $S Salt.
	 * @param int $c Iteration count.
	 * @param mixed $dkLen Derived key length (in octets). If $dkLen is FALSE
	 *				then length will be set to $PRF output length (in
	 *				octets).
	 * @param bool $raw_output When set to TRUE, outputs raw binary data. FALSE
	 *						   outputs lowercase hexits.
	 * @return mixed Derived key or FALSE if $dkLen > (2^32 - 1) * hLen (hLen
	 *				 denotes the length in octets of $PRF output).
	 */
	function pbkdf2($PRF, $P, $S, $c, $dkLen = false, $raw_output = false) {
		//default $hLen is $PRF output length
		$hLen = strlen(hash($PRF, '', true));
		if ($dkLen === false) $dkLen = $hLen;
		
		if ($dkLen <= (pow(2, 32) - 1) * $hLen) {
			$DK = '';
			
			//create key
			for ($block = 1; $block <= $dkLen; $block++) {
				//initial hash for this block
				$ib = $h = hash_hmac($PRF, $S.pack('N', $block), $P, true);
				
				//perform block iterations
				for ($i = 1; $i < $c; $i++) {
					$ib ^= ($h = hash_hmac($PRF, $h, $P, true));
				}
				
				//append iterated block
				$DK .= $ib;
			}
			
			$DK = substr($DK, 0, $dkLen);
			if (!$raw_output) $DK = bin2hex($DK);
			
			return $DK;
			
			//derived key too long
		} else {
			return false;
		}
	}
?>
