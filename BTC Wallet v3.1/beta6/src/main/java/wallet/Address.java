package wallet;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.bitcoinj.core.Base58;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

public class Address {

	private String btcAdress;

	public Address(String pubKey, boolean mainnet) throws NoSuchAlgorithmException, NoSuchProviderException {
		/*
		 * Beim Konstruktoraufruf wird die Adresse erzeugt, welche durch die getAdress
		 * Funktion abrufbar ist. 1. Für das Anwenden der Hashfunktionen brauchen wir
		 * den Publickey-String in Byte-Form, da die Hash-Funktionen ein ByteArray als
		 * Input erwarten. Der Publickey liegt als unkompressierte 64 Zeichen lange
		 * Version vor und besteht aus zweistelligen Hexwerten. Der Output der
		 * Decodierung wird der hash160 Methode zusammen mit einer boolschen Variable
		 * übergeben (true = Mainnet, false = Testnet), die Rückgabe der Methode wird
		 * als Byte-Array abgespeichert. 2. Nun wird die checksum Methode aufgerufen,
		 * welche die Checksumme der hash160bytes zurückliefert, ebenfalls als Bytes. 3.
		 * Nun werden die einzelnen Teile zusammengefügt. Dafür wird an den Hash160-Wert
		 * die Checksum angehängt. Dieser zusammengesetzte Wert ergibt jetzt schon fast
		 * die Adresse. Der Wert wird jetzt nur noch mit dem Base58-Verfahren codiert,
		 * wodurch die Adresse entsteht. Info: Base58 lässt die folgenden vier Zeichen 0
		 * (Null), O (großes o), I (großes i) und l (kleines L) weg, wodurch sich die
		 * Adresse besser lesen lässt, da man sie leicht verwechseln kann.
		 */

		// Schritt 1: Umwandlung des Publickeys in ein ByteArray
		byte[] hash160bytes;
		if (mainnet)
			hash160bytes = hash160(Hex.decode(pubKey), mainnet);
		else
			hash160bytes = hash160(Hex.decode(pubKey), mainnet);

		// Schritt 2: Berechnung der Checksum
		byte[] chesum = checksum(hash160bytes);

		// Schritt 3: Zusammenfügen von Hash160-Wert und Checksum
		byte[] adressBytes = new byte[hash160bytes.length + chesum.length];
		for (int i = 0; i < hash160bytes.length; i++) {
			adressBytes[i] = hash160bytes[i];
		}
		for (int i = hash160bytes.length; i < adressBytes.length; i++) {
			adressBytes[i] = chesum[i - hash160bytes.length];
		}

		// Schritt 4: Codierung der Adresse mit Base58
		btcAdress = Base58.encode(adressBytes);
	}

	public String getAdress() {
		return btcAdress;
	}

	public byte[] hash160(byte[] input, boolean mainnet) throws NoSuchAlgorithmException, NoSuchProviderException {
		/*
		 * Die Hash160-Funktion besteht aus den Hashfunktionen SHA256 & RipeMD160. Der
		 * PublicKey wird mit diesen beiden Funktionen doppelt gehashed. Zu erst SHA,
		 * dann Ripe. Die Reihenfolge ist wichtig. Danach wird der Network-Prefix
		 * hinzugefügt. "00" fürs Mainnet, "6f" fürs Testnet.
		 */

		// Schritt 1: Anwendung der Hash-Funktionen SHA256 und RipeMD160
		SHA256Digest shaDigest = new SHA256Digest();
		RIPEMD160Digest rmdDigest = new RIPEMD160Digest();
		shaDigest.update(input, 0, input.length);
		byte[] shaHashed = new byte[shaDigest.getDigestSize()];
		shaDigest.doFinal(shaHashed, 0);
		rmdDigest.update(shaHashed, 0, shaHashed.length);
		byte[] rmdHashed = new byte[rmdDigest.getDigestSize()];
		rmdDigest.doFinal(rmdHashed, 0);

		// Schritt 2: Hinzufügen des Network-Prefix
		if (mainnet)
			return Hex.decode("00" + Hex.toHexString(rmdHashed));
		else
			return Hex.decode("6f" + Hex.toHexString(rmdHashed));

	}

	public byte[] checksum(byte[] input) throws NoSuchAlgorithmException {
		/*
		 * Für das Berechnen der Checksum wird der PublicKey, welcher zuvor mit der
		 * Hash160-Funktion gehashed wurde, nun doppelt mit der SHA256-Funktion
		 * gehashed. Die ersten 4 Bytes des so entstandenen Hash-Wertes bilden die
		 * Checksum, welche zurückgegeben wird.
		 */

		// Schritt 1: Anwendung der Hash-Funktion SHA256 zweimal
		SHA256Digest shaDigest = new SHA256Digest();
		shaDigest.update(input, 0, input.length);
		byte[] shaHashed = new byte[shaDigest.getDigestSize()];
		shaDigest.doFinal(shaHashed, 0);
		shaDigest.update(shaHashed, 0, shaHashed.length);
		shaDigest.doFinal(shaHashed, 0);

		// Schritt 2: Extraktion der ersten 4 Bytes für die Checksum
		byte[] checkSum = new byte[4];
		for (int i = 0; i < checkSum.length; i++)
			checkSum[i] = shaHashed[i];
		return checkSum;
	}

}
