package convert;

import org.bouncycastle.util.encoders.Hex;

public class LittleEndian {

	// Die Methode 'intToHexString' konvertiert einen Integer-Wert in eine
	// hexadezimale Darstellung
	// und gibt ihn in Little-Endian-Byte-Reihenfolge zurück.
	public static String intToHexString(int val, int bytes) {

		// Konvertiert den Integer-Wert in ein Byte-Array mit führenden Nullen
		byte intBytes[] = Hex.decode(FillAndHex.zero(val, bytes));

		// Initialisiert das Byte-Array für die Little-Endian-Reihenfolge
		byte littleEndian[] = new byte[intBytes.length];

		// Konvertiert das Byte-Array in Little-Endian-Byte-Reihenfolge
		for (int i = 0; i < littleEndian.length; i++) {
			littleEndian[i] = intBytes[littleEndian.length - i - 1];
		}

		// Gibt die hexadezimale Zeichenkette in Little-Endian-Byte-Reihenfolge zurück
		return Hex.toHexString(littleEndian);
	}

	// Die Methode 'stringToHexString' konvertiert eine hexadezimale Zeichenkette in
	// eine andere
	// hexadezimale Zeichenkette, die ihre Bytes in Little-Endian-Byte-Reihenfolge
	// repräsentiert.
	public static String stringToHexString(String s) {

		// Konvertiert die hexadezimale Zeichenkette in ein Byte-Array
		byte[] stringBytes = Hex.decode(s);

		// Initialisiert das Byte-Array für die Little-Endian-Reihenfolge
		byte[] littleEndian = new byte[stringBytes.length];

		// Konvertiert das Byte-Array in Little-Endian-Byte-Reihenfolge
		for (int i = 0; i < littleEndian.length; i++) {
			littleEndian[i] = stringBytes[littleEndian.length - i - 1];
		}

		// Gibt die hexadezimale Zeichenkette in Little-Endian-Byte-Reihenfolge zurück
		return Hex.toHexString(littleEndian);
	}
}
