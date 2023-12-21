package convert;

import org.bouncycastle.util.encoders.Hex;

public class FillAndHex {
	// Die Methode 'zero' konvertiert einen Integer-Wert in eine hexadezimale
	// Darstellung
	// und füllt ihn mit führenden Nullen auf, um die gewünschte Länge in Bytes zu
	// erreichen.
	public static String zero(int value, int bytes) {

		// Konvertiert den Integer-Wert in eine hexadezimale Zeichenkette
		String intValue = Integer.toHexString(value);

		// Berechnet die gewünschte Länge der hexadezimalen Zeichenkette
		int length = bytes * 2;

		// Initialisiert eine temporäre Zeichenkette, die zum Auffüllen mit Nullen
		// verwendet wird
		String temp = "";

		// Fügt führende Nullen hinzu, um die gewünschte Länge zu erreichen
		for (int i = 0; i < (length - intValue.length()); i++) {
			temp += "0";
		}

		// Fügt die führenden Nullen zur ursprünglichen hexadezimalen Zeichenkette hinzu
		intValue = temp + intValue;

		// Konvertiert die hexadezimale Zeichenkette in ein Byte-Array
		byte intBytes[] = Hex.decode(intValue);

		// Gibt die hexadezimale Zeichenkette mit führenden Nullen zurück
		return Hex.toHexString(intBytes);
	}
}
