package transaction;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ECKey.ECDSASignature;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.SignatureDecodeException;
import org.bouncycastle.util.encoders.Hex;

import crypto.SHA256;

public class TX {
	RawTX rawTX;
	String rawTxString;

	// Liste von UTXOs
	LinkedList<UTXO> utxos;

	// Liste von Outputs
	LinkedList<Output> outputs;

	public TX(LinkedList<UTXO> utxos, LinkedList<Output> outputs) {
		this.utxos = utxos;
		this.outputs = outputs;

		rawTX = new RawTX(utxos, outputs);
	}

	// Transaktion signieren
	public String sign(BigInteger privateKey) throws NoSuchAlgorithmException, SignatureDecodeException {
		LinkedList<UTXO> signed_inputs = new LinkedList<UTXO>();

		// Jeden Input signieren
		for (int i = 0; i < utxos.size(); i++) {
			String rawTxToSign = "";
			String outputs = rawTX.getOutput();
			String inputs = "";
			int input_n = utxos.size();
			for (int j = 0; j < utxos.size(); j++) {
				inputs += utxos.get(j).getTxID_LE();
				inputs += utxos.get(j).getVout_LE();
				inputs += utxos.get(j).getScriptSize(i == j);
				inputs += utxos.get(j).getScriptPubKey(i == j);
				inputs += "ffffffff";
			}
			rawTxToSign += RawTX.getRawTXString(outputs, inputs, input_n) + "01000000"; // SigHashAll
			byte[] messageHashedTwice = SHA256.hashTwice(Hex.decode(rawTxToSign));
			ECKey key = ECKey.fromPrivate(privateKey, false);
			ECDSASignature sig = key.sign(Sha256Hash.wrap(messageHashedTwice));
			sig.toCanonicalised();
			byte[] derSigned = sig.encodeToDER();
			String pubKeyHex = key.getPublicKeyAsHex();
			byte[] derSigned01 = ByteBuffer.allocate(derSigned.length + 1).put(derSigned).put(Hex.decode("01")).array();
			int sigLength = derSigned01.length;
			int pubKeyLength = (pubKeyHex.length() / 2);
			String scriptSig = Integer.toHexString(sigLength) + Hex.toHexString(derSigned01)
					+ Integer.toHexString(pubKeyLength) + pubKeyHex;
			UTXO temp = new UTXO(utxos.get(i).getTxID(), scriptSig, utxos.get(i).getVout(), utxos.get(i).getValue());
			signed_inputs.add(temp);
		}
		RawTX signed = new RawTX(signed_inputs, rawTX.getOutput());

		return signed.getRawTX(true);
	}

}
