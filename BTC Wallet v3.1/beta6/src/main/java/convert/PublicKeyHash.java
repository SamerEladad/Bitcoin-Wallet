package convert;

import org.bitcoinj.core.Base58;
import org.bouncycastle.util.encoders.Hex;

public class PublicKeyHash {

	// Die Methode 'fromAddress' extrahiert den PublicKeyHash aus einer
	// Bitcoin-Adresse
	public static String fromAddress(String address) {

		// Dekodiert die Base58-Adresse und prüfe die Checksumme
		byte[] publicKeyHashWithPrefix = Base58.decodeChecked(address);

		// Initialisiert das Byte-Array für den PublicKeyHash ohne Präfix
		byte[] publicKeyHash = new byte[publicKeyHashWithPrefix.length - 1];

		// Entfernt das Präfix (Version-Byte) aus dem PublicKeyHashWithPrefix
		for (int i = 0; i < publicKeyHash.length; i++) {
			publicKeyHash[i] = publicKeyHashWithPrefix[i + 1];
		}

		// Gibt den PublicKeyHash als hexadezimale Zeichenkette zurück
		return Hex.toHexString(publicKeyHash);
	}
}
