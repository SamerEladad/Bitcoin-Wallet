package transaction;

import convert.FillAndHex;
import convert.LittleEndian;

public class UTXO {
	private String txID;
	private String txID_LE;
	private String scriptPubKey; // ScriptPubKey der Transaktion die ausgegeben werden soll
	private String vout_LE;
	private int vout; // Output-Index, startet bei 0
	private String scriptSize; // 1-Byte, gibt an wie viele Bytes ScriptPubKey groß ist
	private int value; // Verschickte Satoshis in der Tx

	public UTXO(String txID, String scriptPubKey, int vout, int value) {
		this.txID = txID;
		txID_LE = LittleEndian.stringToHexString(txID);
		this.scriptPubKey = scriptPubKey;
		this.vout = vout;
		vout_LE = LittleEndian.intToHexString(vout, 4);
		this.value = value;
		scriptSize = FillAndHex.zero((scriptPubKey.length() / 2), 1);
		// scriptSize = Hex.toHexString(Hex.decode("" + (scriptPubKey.length() / 2)));
	}

	public String getTxID() {
		return txID;
	}

	public String getScriptPubKey(boolean sig) {
		if (sig) {
			return scriptPubKey;
		} else {
			return "";
		}
	}

	public int getVout() {
		return vout;
	}

	public String getScriptSize(boolean sig) {
		if (sig) {
			return scriptSize;
		} else {
			return "00";
		}
	}

	public int getValue() {
		return value;
	}

	public String getTxID_LE() {
		return txID_LE;
	}

	public String getVout_LE() {
		return vout_LE;
	}

	// Methode zum Erstellen eines Inputs für die Transaktion
	public String getInputForTX(boolean sig) {
		String input = "";
		input += getTxID_LE(); // TX-Id der letzten TX im Little Endian Format
		input += getVout_LE(); // An welcher Stelle der letzten Transaktion werden wir begünstigt?
		input += getScriptSize(sig); // "00" bis zum signieren
		input += getScriptPubKey(sig); // "" bis zum signieren
		input += "ffffffff"; // Sequence
		return input;
	}

}
