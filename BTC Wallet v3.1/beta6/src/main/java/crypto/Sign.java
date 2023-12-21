package crypto;

import java.security.NoSuchAlgorithmException;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ECKey.ECDSASignature;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.SignatureDecodeException;
import org.bouncycastle.util.encoders.Hex;

//Die Sign-Klasse bietet eine Methode zum Signieren einer Nachricht
//mit einem gegebenen privaten Schlüssel und gibt die Signatur im DER-Format zurück.
public class Sign {
	public static byte[] thisMessageEncodeToDER(String message, String privateKey)
			throws NoSuchAlgorithmException, SignatureDecodeException {

		// Übergebener String wird doppelt mit SHA256 gehashed und als byte[]
		// gespeichert
		byte[] messageHashedTwice = SHA256.hashTwice(Hex.decode(message));

		// ECKey Objekt wird durch Private Key erzeugt, welcher in
		// Hexadezimaldarstellung vorliegt.
		ECKey key = ECKey.fromPrivate(Hex.decode(privateKey));

		// Erstellt ein ECDSASignature Objekt, dass die Transaktionsdaten, welche mit
		// dem Private
		// Key signiert worden sind, abspeichert.
		ECDSASignature sig = key.sign(Sha256Hash.wrap(messageHashedTwice));

		// Sorgt durch Anwenden der Funktion r, -s (mod n) dafür, dass der S-Wert der
		// Signatur
		// immer kleiner als N ist, um eine der Bitcoin Script Sprache entsprechende,
		// korrekte
		// Signatur zu erhalten
		sig.toCanonicalised();

		// Codiert Signatur im DER-Format und speichert Signatur als Byte[], welches
		// zurückgegeben wird.
		byte signedBytes[] = sig.encodeToDER();

		// Gibt die DER-kodierte Signatur als Byte-Array zurück
		return signedBytes;
	}
}
