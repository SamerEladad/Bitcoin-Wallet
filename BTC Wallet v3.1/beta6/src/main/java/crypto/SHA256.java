package crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Die SHA256-Klasse bietet zwei Methoden, welche jeweils ein Byte Array als Übergabe- 
// wie auch Rückgabeparameter haben. Die erste Methode bietet die Möglichkeit des einfachen#
// Hashens mit SHA256, die zweite hashed doppelt mit SHA256.
public class SHA256 {

	// Die Funktion hash führt die SHA256-Hashfunktion auf dem Eingabe-Byte-Array
	// aus
	// und gibt das gehashte Byte-Array zurück.
	public static byte[] hash(byte[] data) throws NoSuchAlgorithmException {

		// Initialisiert die SHA256-Hashfunktion
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

		// Führt die Hashfunktion auf dem Eingabe-Byte-Array aus
		byte[] hash = sha256.digest(data);

		// Gibt das gehashte Byte-Array zurück
		return hash;
	}

	// Die Funktion hashTwice führt die SHA256-Hashfunktion zweimal auf dem
	// Eingabe-Byte-Array aus
	// und gibt das doppelt gehashte Byte-Array zurück.
	public static byte[] hashTwice(byte[] data) throws NoSuchAlgorithmException {

		// Initialisiert die SHA256-Hashfunktion
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

		// Führt die Hashfunktion zum ersten Mal auf dem Eingabe-Byte-Array aus
		byte[] firstHash = sha256.digest(data);

		// Führt die Hashfunktion zum zweiten Mal auf dem zuvor gehashten Byte-Array aus
		byte[] secondHash = sha256.digest(firstHash);

		// Gibt das doppelt gehashte Byte-Array zurück
		return secondHash;
	}
}
