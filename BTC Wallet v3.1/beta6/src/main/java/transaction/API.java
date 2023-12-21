package transaction;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class API {
	final static String testnetURL = "https://api.blockcypher.com/v1/btc/test3/addrs/";
	final static String apiFilter = "?unspentOnly=true&includeScript=true";
	final static String mainnetURL = "https://api.blockcypher.com/v1/btc/main/addrs/";
	final static String apiToken = "4eb0935d540040fbac8f916747c4762d";
	final static String pushTestTxURL = "https://api.blockcypher.com/v1/btc/test3/txs/push?token=" + apiToken;
	final static String pushMainTxURL = "https://api.blockcypher.com/v1/btc/main/txs/push?token=" + apiToken;

	public static LinkedList<UTXO> getUTXOs(String address, boolean network) throws IOException {
		LinkedList<UTXO> utxos = new LinkedList<UTXO>();
		String utxosFromAdress = "";

		// URL auf Basis des Netzwerks festlegen
		if (network)
			utxosFromAdress = mainnetURL + address + apiFilter;
		else
			utxosFromAdress = testnetURL + address + apiFilter;

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			// HTTP GET-Anfrage erstellen
			HttpGet request = new HttpGet(utxosFromAdress);

			try (CloseableHttpResponse response = httpClient.execute(request)) {

				// Antwort abrufen und Ergebnis verarbeiten
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				JSONArray unspentOutputs = new JSONArray();
				try {
					unspentOutputs = new JSONObject(result).getJSONArray("txrefs");
				} catch (JSONException e) {
					return null;
				}

				// Unverbrauchte Ausgänge verarbeiten
				for (int i = 0; i < unspentOutputs.length(); i++) {
					JSONObject unspentOutput = unspentOutputs.getJSONObject(i);

					String txid = unspentOutput.getString("tx_hash");
					String scriptPubKey = unspentOutput.getString("script");
					int vout = unspentOutput.getInt("tx_output_n");
					int value = unspentOutput.getInt("value");

					utxos.add(new UTXO(txid, scriptPubKey, vout, value));
				}
			}
		}
		return utxos;
	}

	public static JSONObject getAddressInformations(String address, boolean network) throws IOException {
		String informationsFromAddress = "";

		// URL auf Basis des Netzwerks festlegen
		if (network)
			informationsFromAddress = mainnetURL + address + apiFilter;
		else
			informationsFromAddress = testnetURL + address + apiFilter;

		JSONObject informations = null;
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			// HTTP GET-Anfrage erstellen
			HttpGet request = new HttpGet(informationsFromAddress);

			try (CloseableHttpResponse response = httpClient.execute(request)) {

				// Antwort abrufen und Ergebnis verarbeiten
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				informations = new JSONObject(result);

			}
		}
		return informations;
	}

	public static String[] pushTX(String txScript, boolean network) throws IOException {

		JSONObject txJson = new JSONObject();

		// Transaktionsskript zur JSON hinzufügen
		txJson.put("tx", txScript);

		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = null;

		// HTTP POST-Anfrage auf Basis des Netzwerks erstellen
		if (network) {
			httpPost = new HttpPost(pushMainTxURL);
		} else {
			httpPost = new HttpPost(pushTestTxURL);
		}

		StringEntity entity = new StringEntity(txJson.toString());
		httpPost.setEntity(entity);

		// Anfrage ausführen und Antwort abrufen
		HttpResponse response = httpClient.execute(httpPost);

		int statusCode = response.getStatusLine().getStatusCode();
		HttpEntity responseEntity = response.getEntity();
		String responseString = EntityUtils.toString(responseEntity);
		// System.out.println(responseString);

		// Antwort zurückgeben
		return new String[] { String.valueOf(statusCode), responseString };

	}
}
