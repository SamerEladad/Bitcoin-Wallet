package phase2.beta5;

import java.awt.Desktop;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.SignatureDecodeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import transaction.API;
import transaction.Output;
import transaction.TX;
import transaction.UTXO;
import wallet.Address;
import wallet.Keys;

//Die PrimaryController-Klasse verwaltet die Interaktionen der Benutzeroberfläche
//und führt Aktionen basierend auf den Benutzereingaben aus.
public class PrimaryController {

	@FXML
	private Text addressText;
	@FXML
	private Text balanceText;
	@FXML
	private Text networkText;
	@FXML
	private Text totReceivedText;
	@FXML
	private Text totSentText;
	@FXML
	private Text uncBalanceText;
	@FXML
	private Text finBalanceText;
	@FXML
	private Text ntxText;
	@FXML
	private Text uncNtxText;
	@FXML
	private Text finNtxText;
	@FXML
	private TextArea scrollLog;
	@FXML
	private Text avaBalanceText;

	// Variablen zur Speicherung von Informationen über das Wallet und das Netzwerk
	private boolean network = false;
	private boolean walletLoaded = false;
	private String address = "";
	private int total_received = 0;
	private int total_sent = 0;
	private int balance = 0;
	private int unconfirmed_balance = 0;
	private int final_balance = 0;
	private int n_tx = 0;
	private int unconfirmed_n_tx = 0;
	private int final_n_tx = 0;
	private int scrollIndex = 0;
	private BigInteger privateKey;
	private int avaBalance = 0;

	// Diese Methode wird aufgerufen, wenn der Benutzer auf "Paper-Wallet erzeugen"
	// klickt
	@FXML
	void handleGenPaperWallet()
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {

		// Erzeugt Schlüsselpaar und generiere die zugehörige Bitcoin-Adresse
		Keys keys = new Keys();
		keys.generateKeyPair();
		String privKey = keys.getPrivateKeyString();
		String pubKey = keys.getPublicKeyString();
		Address address = new Address(pubKey, false);

		// Zeigt den privaten und öffentlichen Schlüssel sowie die Bitcoin-Adresse in
		// einem Textbereich an
		JTextArea textArea = new JTextArea("Privater Schlüssel:\n" + privKey + "\nÖffentlicher Schlüssel:\n" + pubKey
				+ "\nAdresse:\n" + address.getAdress());
		textArea.setColumns(75);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setSize(textArea.getPreferredSize().width, 1);
		textArea.setEditable(false);
		JOptionPane.showMessageDialog(null, textArea, "Paper-Wallet", JOptionPane.PLAIN_MESSAGE);
		extendLog("Paper-Wallet erzeugt.");
	}

	@FXML
	void handleGenWalletFile() throws NoSuchAlgorithmException, NoSuchProviderException,
			InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, BadPaddingException,
			IllegalBlockSizeException, InvalidKeySpecException {

		// Erstellt ein neues Keys-Objekt
		Keys keys = new Keys();

		// Generiert ein Schlüsselpaar
		keys.generateKeyPair();

		// Speichere den privaten Schlüssel in einer Datei
		boolean saved = keys.savePrivateKeyToFile(new BigInteger(keys.getPrivateKeyString(), 16));

		// Log-Meldung, ob die Wallet-Datei erfolgreich erstellt wurde
		if (saved) {
			extendLog("Wallet-Datei erzeugt.");
		} else {
			extendLog("Wallet-Datei erzeugen abgebrochen.");
		}
	}

	@FXML
	void handleLoadWalletFile() throws InvalidKeyException, FileNotFoundException, NoSuchAlgorithmException,
			InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException, IOException, ParseException, NoSuchProviderException {

		// Setzt die Informationen zurück, wenn die Wallet bereits geladen ist
		if (walletLoaded) {
			resetInformation();
		}

		// Erstellt ein neues Keys-Objekt
		Keys keys = new Keys();

		// Liest den privaten Schlüssel aus der Datei
		boolean loaded = keys.readPrivateKeyFromFile();

		// Log-Meldung, ob die Wallet-Datei erfolgreich geladen wurde
		if (loaded) {
			extendLog("Wallet-Datei geladen.");
			privateKey = keys.getPrivateKey();
			changeAddressLabel();
		} else
			extendLog("Wallet-Datei laden abgebrochen");

	}

	@FXML
	void handleSwitchNetwork() throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
		// int option = JOptionPane.showOptionDialog(null, "Achtung! Wenn Sie auf das
		// Bitcoin-Hauptnetzwerk wechseln, handeln Sie mit echtem Geld. Möchten Sie den
		// Wechsel auf das Hauptnetzwerk bestätigen?" , "Netzwerk wechseln",
		// JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, address);

		// Prüft, ob die Wallet geladen ist
		if (walletLoaded) {

			// Wechselt zwischen Testnetzwerk und Hauptnetzwerk
			if (network) {
				int option = JOptionPane.showOptionDialog(null,
						"Bitte bestätigen Sie den Wechsel auf das Testnetzwerk.", "Netzwerk wechseln",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				if (option == 0) {
					network = false;
					networkText.setText("Testnetzwerk");
					changeAddressLabel();
					extendLog("Auf Testnetzwerk gewechselt.");
				} else
					return;

			} else {
				int option = JOptionPane.showOptionDialog(null,
						"Achtung! Wenn Sie auf das Bitcoin-Hauptnetzwerk wechseln, handeln Sie\nmit echtem Geld. Möchten Sie den Wechsel auf das Hauptnetzwerk bestätigen?",
						"Netzwerk wechseln", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				if (option == 0) {
					network = true;
					networkText.setText("Hauptnetzwerk");
					extendLog("Auf Hauptnetzwerk gewechselt.");
					changeAddressLabel();
				} else
					return;
			}
		} else {

			// Wechselt zwischen Testnetzwerk und Hauptnetzwerk, wenn keine Wallet geladen
			// ist
			if (network) {
				int option = JOptionPane.showOptionDialog(null,
						"Möchten Sie wirklick auf das Bitcoin-Testnetzwerk wechseln?", "Netzwerk wechseln",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				if (option == 0) {
					network = false;
					networkText.setText("Testnetzwerk");
					extendLog("Auf Testnetzwerk gewechselt.");
				} else
					return;
			} else {
				int option = JOptionPane.showOptionDialog(null,
						"Achtung! Wenn Sie auf das Bitcoin-Hauptnetzwerk wechseln, handeln Sie\nmit echtem Geld. Möchten Sie den Wechsel auf das Hauptnetzwerk bestätigen?",
						"Netzwerk wechseln", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				if (option == 0) {
					network = true;
					networkText.setText("Hauptnetzwerk");
					extendLog("Auf Hauptnetzwerk gewechselt.");
				} else
					return;
			}
		}
	}

	@FXML
	void handleFileFromPaper() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException,
			InvalidKeySpecException {

		// Schleife, um den privaten Schlüssel einzulesen
		while (true) {

			// Zeigt ein Eingabefenster zum Einfügen des privaten Schlüssels
			String privateKey = (String) JOptionPane.showInputDialog(null, "Geben Sie ihren privaten Schlüssel ein.",
					"Wallet-Datei aus Paper-Wallet erzeugen", JOptionPane.PLAIN_MESSAGE, null, null, null);
			if (privateKey == null) {

				// Bestätigung, ob der Vorgang abgebrochen werden soll
				int option = JOptionPane.showOptionDialog(null, "Möchten Sie den Vorgang wirklich abbrechen?",
						"Achtung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 1);
				if (option == 0) {
					extendLog("Wallet-Datei aus Paper-Wallet erzeugen abgebrochen.");
					return;
				}
			} else {

				// Speichert den privaten Schlüssel in einer Datei
				Keys.savePrivateKeyToFile(new BigInteger(privateKey, 16));
				extendLog("Wallet-Datei aus Paper-Wallet erzeugt.");
				return;
			}
		}
	}

	@FXML
	void handleShowWalletDetails() {
		if (walletLoaded) {

			// Warnung, den privaten Schlüssel nicht weiterzugeben
			JOptionPane.showMessageDialog(null, "Zeigen Sie den Privaten Schlüssel niemals jemand anderem!", "Achtung",
					JOptionPane.WARNING_MESSAGE);

			// Erzeugt ein ECKey-Objekt aus dem privaten Schlüssel
			ECKey ecKey = ECKey.fromPrivate(privateKey, false);

			// Erstellt eine JTextArea, um die Wallet-Details anzuzeigen
			JTextArea textArea = new JTextArea("Privater Schlüssel:\n" + ecKey.getPrivateKeyAsHex()
					+ "\nÖffentlicher Schlüssel:\n" + ecKey.getPublicKeyAsHex() + "\nAdresse:\n" + this.address);
			textArea.setColumns(75);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setSize(textArea.getPreferredSize().width, 1);
			textArea.setEditable(false);

			// Zeigt die Wallet-Details in einem Dialogfenster an
			JOptionPane.showMessageDialog(null, textArea, "Wallet Details", JOptionPane.PLAIN_MESSAGE);
			extendLog("Wallet Details angezeigt");
		} else {

			// Fehlermeldung, wenn keine Wallet geladen ist
			extendLog("Wallet Details anzeigen fehlgeschlagen.");
			JOptionPane.showMessageDialog(null, "Keine Wallet Geladen.", "Fehler", JOptionPane.ERROR_MESSAGE);
		}

	}

	void resetInformation() throws NoSuchAlgorithmException, NoSuchProviderException, IOException {

		// Setzt die gespeicherten Informationen auf ihre Standardwerte zurück
		privateKey = null;
		changeAddressLabel();
		balance = 0;
		avaBalanceText.setText("0 Satoshi");
		balanceText.setText(balance + " Satoshi");
		unconfirmed_balance = 0;
		uncBalanceText.setText(unconfirmed_balance + " Satoshi");
		final_balance = 0;
		finBalanceText.setText(final_balance + " Satoshi");
		total_received = 0;
		totReceivedText.setText(total_received + " Satoshi");
		total_sent = 0;
		totSentText.setText(total_sent + " Satoshi");
		n_tx = 0;
		ntxText.setText(String.valueOf(n_tx));
		unconfirmed_n_tx = 0;
		uncNtxText.setText(String.valueOf(unconfirmed_n_tx));
		final_n_tx = 0;
		finNtxText.setText(String.valueOf(final_n_tx));

		// Fügt dem Log eine Nachricht hinzu, dass die Wallet-Informationen
		// zurückgesetzt wurden
		extendLog("Wallet-Informationen zurückgesetzt.");
	}

	void getInformation() throws IOException {

		// API-Abfrage, um Informationen zur Adresse abzurufen
		JSONObject informations = API.getAddressInformations(address, network);
		LinkedList<UTXO> utxo = new LinkedList<UTXO>();
		JSONArray utxoJSON = new JSONArray();
		try {

			// Versucht, die UTXO-Daten aus den abgerufenen Informationen zu extrahieren
			utxoJSON = informations.getJSONArray("txrefs");
		} catch (JSONException e) {

		}

		// Durchläuft die UTXO-Daten und fügt sie der LinkedList hinzu
		for (int i = 0; i < utxoJSON.length(); i++) {
			JSONObject unspentOutput = utxoJSON.getJSONObject(i);

			String txid = unspentOutput.getString("tx_hash");
			String scriptPubKey = unspentOutput.getString("script");
			int vout = unspentOutput.getInt("tx_output_n");
			int value = unspentOutput.getInt("value");

			utxo.add(new UTXO(txid, scriptPubKey, vout, value));
		}

		// Aktualisiert die verfügbare Balance
		if (utxo == null) {
			avaBalanceText.setText("0 Satoshi");
			avaBalance = 0;
		} else {
			avaBalance = 0;
			for (int i = 0; i < utxo.size(); i++) {
				avaBalance += utxo.get(i).getValue();
			}
			avaBalanceText.setText(String.valueOf(avaBalance + " Satoshi"));
		}

		// Aktualisiert die restlichen Wallet-Informationen
		balance = informations.getInt("balance");
		balanceText.setText(balance + " Satoshi");
		unconfirmed_balance = informations.getInt("unconfirmed_balance");
		uncBalanceText.setText(unconfirmed_balance + " Satoshi");
		final_balance = informations.getInt("final_balance");
		finBalanceText.setText(final_balance + " Satoshi");
		total_received = informations.getInt("total_received");
		totReceivedText.setText(total_received + " Satoshi");
		total_sent = informations.getInt("total_sent");
		totSentText.setText(total_sent + " Satoshi");
		n_tx = informations.getInt("n_tx");
		ntxText.setText(String.valueOf(n_tx));
		unconfirmed_n_tx = informations.getInt("unconfirmed_n_tx");
		uncNtxText.setText(String.valueOf(unconfirmed_n_tx));
		final_n_tx = informations.getInt("final_n_tx");
		finNtxText.setText(String.valueOf(final_n_tx));

		// Fügt dem Log eine Nachricht hinzu, dass die Wallet-Informationen aktualisiert
		// wurden
		extendLog("Wallet-Informationen aktualisiert.");
	}

	@FXML
	void handleRefreshInformation() throws IOException {

		// Aktualisiert die Wallet-Informationen, wenn eine Wallet geladen ist
		if (walletLoaded) {
			getInformation();
		} else {

			// Zeigt eine Fehlermeldung an, wenn keine Wallet geladen ist
			JOptionPane.showMessageDialog(null, "Keine Wallet geladen.", "Fehler", JOptionPane.ERROR_MESSAGE);

		}
	}

	@FXML
	void handleSendTx() throws NoSuchAlgorithmException, NoSuchProviderException, IOException, SignatureDecodeException,
			InterruptedException {
		if (walletLoaded) {

			// Prüft, ob das verfügbare Guthaben größer als 1000 Satoshi ist
			if (avaBalance > 1000) {
				String debitString = new String();
				boolean b = true;
				int debit = 0;
				while (b) {
					debitString = (String) JOptionPane.showInputDialog(null,
							"Wie viele Bitcoin wollen Sie insgesamt senden? Geben Sie den Betrag in Satoshi ein.",
							"Bitcoin senden", JOptionPane.QUESTION_MESSAGE, null, null, null);
					if (debitString == null) {
						int option = JOptionPane.showOptionDialog(null, "Möchten Sie den Vorgang wirklich abbrechen?",
								"Achtung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 1);
						if (option == 0) {
							extendLog("Transaktion abgebrochen");
							return;
						}
					} else {
						try {
							debit = Integer.parseInt(debitString);
							b = false;
						} catch (Exception e) {
							b = true;
							JOptionPane.showMessageDialog(null,
									"Fehlerhafter Wert eingegeben. Achten Sie darauf nur Zahlen einzugeben.", "Fehler",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}

				if ((debit + 1000) > avaBalance) {
					JOptionPane.showMessageDialog(null,
							"Das verfügbare Guthaben reicht nicht aus um " + debit + " Satoshi zu senden.", "Fehler",
							JOptionPane.ERROR_MESSAGE);
				} else {
					LinkedList<Output> outputs = new LinkedList<Output>();
					int n_out = 0;
					String n_outString = new String();
					while (!b) {
						n_outString = (String) JOptionPane.showInputDialog(null,
								"Geben Sie die Anzahl der Empfänger an", "Empfängeranzahl",
								JOptionPane.QUESTION_MESSAGE, null, null, null);
						if (n_outString == null) {
							int option = JOptionPane.showOptionDialog(null,
									"Möchten Sie den Vorgang wirklich abbrechen?", "Achtung", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE, null, null, 1);
							if (option == 0) {
								extendLog("Transaktion abgebrochen");
								return;
							}
						} else {
							try {
								n_out = Integer.parseInt(n_outString);
								if (n_out > 0)
									b = true;
								else {
									JOptionPane.showMessageDialog(null,
											"Die Anzahl an Empfängern muss mindestens 1 betragen.", "Fehler",
											JOptionPane.ERROR_MESSAGE);
								}
							} catch (Exception e) {
								b = false;
								JOptionPane.showMessageDialog(null,
										"Fehlerhafter Wert eingegeben. Achten Sie darauf nur Zahlen einzugeben.",
										"Fehler", JOptionPane.ERROR_MESSAGE);
							}
						}
					}

					for (int i = 0; i < n_out; i++) {

						String recipientAddress = (String) JOptionPane.showInputDialog(null,
								"Geben Sie die Adresse des " + (i + 1) + ". Empfängers ein.", "Empfängeradresse",
								JOptionPane.QUESTION_MESSAGE, null, null, null);
						if (recipientAddress == null) {
							int option = JOptionPane.showOptionDialog(null,
									"Möchten Sie den Vorgang wirklich abbrechen?", "Achtung", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE, null, null, 1);
							if (option == 0) {
								extendLog("Transaktion abgebrochen");
								return;
							} else
								i--;
						} else {
							if (n_out > 1) {
								boolean b_1 = true;
								while (b_1) {
									String sendAmountString = (String) JOptionPane.showInputDialog(null,
											"Geben Sie die Menge der Satoshi ein, die an die Adresse "
													+ recipientAddress + " geschickt werden soll.",
											"Betrag", JOptionPane.QUESTION_MESSAGE, null, null, null);
									if (sendAmountString == null) {
										int option = JOptionPane.showOptionDialog(null,
												"Möchten Sie den Vorgang wirklich abbrechen?", "Achtung",
												JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 1);
										if (option == 0) {
											extendLog("Transaktion abgebrochen");
											return;
										}
									} else {
										try {
											int sendAmount = Integer.parseInt(sendAmountString);
											try {
												outputs.add(new Output(sendAmount, recipientAddress));
											} catch (Exception e) {
												extendLog("Keine gültige Adresse eingegeben, Transaktion abgebrochen.");
												return;
											}
											b = false;
											b_1 = false;
										} catch (Exception e) {
											b = true;
											b_1 = true;
											JOptionPane.showMessageDialog(null,
													"Fehlerhafter Wert eingegeben. Achten Sie darauf nur Zahlen einzugeben.",
													"Fehler", JOptionPane.ERROR_MESSAGE);
										}
									}
								}

							} else {
								try {
									outputs.add(new Output(debit, recipientAddress));
								} catch (Exception e) {
									extendLog("Keine gültige Adresse eingegeben, Transaktion abgebrochen.");
									return;
								}

								b = false;
							}
						}

					}
					LinkedList<UTXO> temp = API.getUTXOs(address, network);
					if (temp == null) {
						extendLog("Transaktion abgebrochen.");
						JOptionPane.showMessageDialog(null,
								"Keine bestätigten Transaktionen zum Ausgeben vorhanden,\nwarten Sie bis offene Transaktionen bestätigt werden.",
								"Fehler", JOptionPane.ERROR_MESSAGE);
						return;
					}

					int check_utxo_sats = 0;

					for (int i = 0; i < temp.size(); i++) {
						check_utxo_sats += temp.get(i).getValue();
					}

					if (check_utxo_sats >= debit + 1000) {
						int vint = 0;
						int utxo_sats = 0;
						while (utxo_sats < (debit + 1000)) {
							utxo_sats += temp.get(vint).getValue();
							vint++;
						}

						LinkedList<UTXO> utxo = new LinkedList<UTXO>();

						for (int i = 0; i < vint; i++) {
							utxo.add(temp.get(i));
						}

						if ((debit + 1000) < utxo_sats) {
							outputs.add(new Output((utxo_sats - 1000 - debit), address));
						}

						TX tx = new TX(utxo, outputs);

						String[] apiResponse = API.pushTX(tx.sign(privateKey), network);
						if (Integer.parseInt(apiResponse[0]) == 201) {
							JSONObject pushedTX = new JSONObject(apiResponse[1]);
							pushedTX = pushedTX.getJSONObject("tx");
							String txID = pushedTX.getString("hash");
							String trackTxUrl = "";
							if (network) {
								trackTxUrl = "https://live.blockcypher.com/btc/tx/" + txID + "/";
							} else {
								trackTxUrl = "https://live.blockcypher.com/btc-testnet/tx/" + txID + "/";
							}

							String html = "<a href=\"" + trackTxUrl + "\">" + trackTxUrl + "</a>\n";

							html = "<html><body" + html + "</body></html>";
							JEditorPane editor = new JEditorPane("text/html", html);
							editor.addHyperlinkListener(new HyperlinkListener() {
								@Override
								public void hyperlinkUpdate(HyperlinkEvent e) {
									if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
										try {
											Desktop.getDesktop().browse(URI.create(e.getURL().toString()));
										} catch (IOException e1) {
											e1.printStackTrace();
										}
									}
								}
							});
							editor.setEditable(false);

							JOptionPane.showMessageDialog(null, editor,
									"Transaktion erfolgreich! Öffnen Sie den Link um den Status ihrer Transaktion einzusehen.",
									JOptionPane.PLAIN_MESSAGE);
							extendLog("Transaktion über " + debit
									+ " Satoshi wurde ausgeführt. Es wurden 1000 Satoshi als Transaktionsgebühr berechnet.");
							getInformation();

						} else {
							JOptionPane.showMessageDialog(null,
									"Die Transaktion ist fehlgeschlagen. API Response Code:\n" + apiResponse[0],
									"Fehler", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(null,
								"Warten Sie bis offene Transaktionen bestätigt wurden.\nVersuchen Sie es in 5-10 Minuten erneut.",
								"Fehler", JOptionPane.ERROR_MESSAGE);
						extendLog("Transaktion abgebrochen.");
					}
				}
			} else {

				// Zeigt Fehlermeldung an, wenn das Guthaben nicht ausreicht
				JOptionPane.showMessageDialog(null,
						"Das Guthaben ihrer Wallet reicht nicht aus um die Transaktion durchzuführen.", "Fehler",
						JOptionPane.ERROR_MESSAGE);
				extendLog("Transaktion abgebrochen.");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Keine Wallet geladen.", "Fehler", JOptionPane.ERROR_MESSAGE);
		}
	}

	@FXML
	void handleLoadPaperWallet() throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
		if (walletLoaded) {
			resetInformation();
		}
		boolean choosed = false;
		int i = 0;

		// Schleife, um den Benutzer zur Eingabe eines gültigen privaten Schlüssels
		// aufzufordern
		while (!choosed) {
			if (i > 0) {
				JOptionPane.showMessageDialog(null, "Es wurde kein gültiger Privater Schlüssel eingegeben.", "Fehler",
						JOptionPane.ERROR_MESSAGE);
			}
			String privateKey = (String) JOptionPane.showInputDialog(null, "Geben Sie ihren privaten Schlüssel ein",
					"Paper-Wallet laden", JOptionPane.PLAIN_MESSAGE, null, null, null);
			if (privateKey == null) {
				int option = JOptionPane.showOptionDialog(null, "Möchten Sie den Vorgang wirklich abbrechen?",
						"Achtung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 1);
				if (option == 0) {
					extendLog("Paper-Wallet laden abgebrochen.");
					return;
				}
			} else {
				try {
					this.privateKey = new BigInteger(privateKey, 16);
					choosed = true;
				} catch (Exception e) {
					choosed = false;
					i++;
				}
			}
		}
		extendLog("Paper-Wallet geladen.");
		changeAddressLabel();
	}

	@FXML
	void changeAddressLabel() throws NoSuchAlgorithmException, NoSuchProviderException, IOException {

		// Wenn ein privater Schlüssel vorhanden ist, generiere die zugehörige Adresse
		if (privateKey != null) {
			walletLoaded = true;
			Address address = new Address(ECKey.fromPrivate(privateKey, false).getPublicKeyAsHex(), network);
			this.address = address.getAdress();
			addressText.setText(this.address);
			getInformation();
		} else
			addressText.setText("");
	}

	// Methode zum Hinzufügen von Text zum Protokoll
	void extendLog(String s) {
		if (scrollIndex == 0) {
			scrollLog.appendText(s);
			scrollIndex++;
		} else {
			scrollLog.appendText("\n" + s);
		}
	}
}
