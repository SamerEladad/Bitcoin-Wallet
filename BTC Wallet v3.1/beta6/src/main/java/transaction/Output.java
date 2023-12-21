package transaction;

import convert.FillAndHex;
import convert.LittleEndian;
import convert.PublicKeyHash;

public class Output {
	private String value_LE;
	private String pubKeyHash;
	private int value;
	private String pubKeyHashLength;

	public Output(int value, String address) {
		this.value = value;

		// Wert in Little Endian Format umwandeln
		value_LE = LittleEndian.intToHexString(value, 8);

		// PublicKeyHash aus Adresse erzeugen
		pubKeyHash = PublicKeyHash.fromAddress(address);

		// Länge des PublicKeyHash festlegen
		pubKeyHashLength = FillAndHex.zero((pubKeyHash.length() / 2), 1);
	}

	public String getValue_LE() {
		return value_LE;
	}

	public String getPubKeyHash() {
		return pubKeyHash;
	}

	public String getPubKeyHashLength() {
		return pubKeyHashLength;
	}

	public int getValue() {
		return value;
	}

}
