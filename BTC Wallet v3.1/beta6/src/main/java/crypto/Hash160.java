package crypto;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

/*
 * Die Hash160-Funktion besteht aus den Hashfunktionen SHA256 & RipeMD160. Der
 * PublicKey wird mit diesen beiden Funktionen doppelt gehashed. Zu erst SHA,
 * dann Ripe. Die Reihenfolge ist wichtig. Danach wird der Network-Prefix
 * hinzugefügt. 
 * 
 * hash160String gibt die durch die Hashfunktion erzeugten Bytes im Hexformat als
 * String aus.
 * hash160Bytes gibt das Ergebnis der Hashfunktion als Array aus.
 */

//Die Hash160-Klasse implementiert die Hash160-Funktion, die aus den Hashfunktionen
//SHA256 und RIPEMD160 besteht. Der PublicKey wird zuerst mit SHA256 gehasht und
//anschließend mit RIPEMD160. (Die Reihenfolge ist wichtig).
public class Hash160 {

	// hash160String gibt die durch die Hashfunktion erzeugten Bytes im Hexformat
	// als
	// String aus.
	public static String hash160String(byte[] input) {

		// Initialisiert die SHA256- und RIPEMD160-Digests
		SHA256Digest shaDigest = new SHA256Digest();
		RIPEMD160Digest rmdDigest = new RIPEMD160Digest();

		// Führt die SHA256-Hashfunktion auf dem Eingabe-Byte-Array aus
		shaDigest.update(input, 0, input.length);
		byte[] shaHashed = new byte[shaDigest.getDigestSize()];
		shaDigest.doFinal(shaHashed, 0);

		// Führt die RIPEMD160-Hashfunktion auf dem SHA256-gehashten Byte-Array aus
		rmdDigest.update(shaHashed, 0, shaHashed.length);
		byte[] rmdHashed = new byte[rmdDigest.getDigestSize()];
		rmdDigest.doFinal(rmdHashed, 0);

		// Gibt das Ergebnis der Hashfunktionen als hexadezimalen String zurück
		return Hex.encodeHexString(rmdHashed);
	}

	// hash160Bytes gibt das Ergebnis der Hashfunktion als Byte-Array aus.
	public static byte[] hash160Bytes(byte[] input) {

		// Die Implementierung ist ähnlich wie in hash160String.
		// Der einzige Unterschied besteht darin, dass hier ein Byte-Array
		// anstelle eines hexadezimalen Strings zurückgegeben wird.
		SHA256Digest shaDigest = new SHA256Digest();
		RIPEMD160Digest rmdDigest = new RIPEMD160Digest();
		shaDigest.update(input, 0, input.length);
		byte[] shaHashed = new byte[shaDigest.getDigestSize()];
		shaDigest.doFinal(shaHashed, 0);
		rmdDigest.update(shaHashed, 0, shaHashed.length);
		byte[] rmdHashed = new byte[rmdDigest.getDigestSize()];
		rmdDigest.doFinal(rmdHashed, 0);
		return rmdHashed;
	}
}
