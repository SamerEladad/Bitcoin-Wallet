package transaction;

import java.util.LinkedList;

import convert.FillAndHex;

public class RawTX {

	// Liste von UTXOs
	LinkedList<UTXO> utxos;

	// Liste von Outputs
	LinkedList<Output> outputs;
	String output = "";
	String outputs_n;
	final int minerfee = 1000;
	int zahler = 0;

	public RawTX(LinkedList<UTXO> utxos, LinkedList<Output> outputs) {
		this.utxos = utxos;
		this.outputs = outputs;

		// Output-String erstellen
		setOutput();
	}

	public RawTX(LinkedList<UTXO> utxos, String output) {
		this.utxos = utxos;
		this.output = output;
	}

	// Output-String erstellen
	void setOutput() {
		output += FillAndHex.zero(outputs.size(), 1); // Anzahl der Outputs in unserer TX

		// Output-String für jeden Output erstellen
		for (int i = 0; i < outputs.size(); i++) {
			String outputScript = "";
			output += outputs.get(i).getValue_LE(); // Menge der Satoshis die überwiesen werden im Little Endian Format
			outputScript += "76a914"; // OPT-Codes: Duplizieren, Hash160, Push14
			outputScript += outputs.get(i).getPubKeyHash();
			outputScript += "88ac"; // OPT-Codes: equalverify, checksig
			output += Integer.toHexString(outputScript.length() / 2) + outputScript;
		}
		output += "00000000"; // locktime
	}

	void setOutput(String output) {
		this.output = output;
	}

	public String getOutput() {
		return output;
	}

	// Inputs erstellen
	public String getInputs(boolean sig) {
		String inputs = "";
		for (int i = 0; i < utxos.size(); i++) {
			inputs += utxos.get(i).getTxID_LE();
			inputs += utxos.get(i).getVout_LE();
			if (sig) {
				inputs += utxos.get(i).getScriptSize(true);
				inputs += utxos.get(i).getScriptPubKey(true);
				inputs += "ffffffff";
			} else {
				inputs += utxos.get(i).getScriptSize(false);
				inputs += utxos.get(i).getScriptPubKey(false);
				inputs += "ffffffff";
			}
		}
		return inputs;
	}

	public LinkedList<Output> getOutputs() {
		return outputs;
	}

	// RawTX erstellen
	String getRawTX(boolean sig) {
		String rawTX = "";
		rawTX += "02000000"; // V2 Transaktion
		rawTX += FillAndHex.zero(utxos.size(), 1); // Menge der UTXO's die für die TX verwendet werden
		rawTX += getInputs(sig); // Inputs für diese TX sind unsere UTXO's
		rawTX += output;
		return rawTX;
	}

	public static String getRawTXString(String outputs, String inputs, int inputs_n) {
		String rawTX = "";
		rawTX += "02000000"; // V2 Transaktion
		rawTX += FillAndHex.zero(inputs_n, 1);
		rawTX += inputs;
		rawTX += outputs;
		return rawTX;
	}
}
