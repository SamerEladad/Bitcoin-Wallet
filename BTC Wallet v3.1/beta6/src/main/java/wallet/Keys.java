package wallet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Keys {
	private KeyPair ecKeyPair;
	private ECPublicKey ecPubKey;
	private ECPrivateKey ecPrivKey;
	private BigInteger privateKey;
	// private ECKey ecKey;

	public Keys() {

	}

	// Schlüsselpaar generieren
	public void generateKeyPair()
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {

		// BouncyCastleProvider asd;

		// BouncyCastleProvider hinzufügen
		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator ecKeyGen = KeyPairGenerator.getInstance("EC", "BC");
		ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
		ecKeyGen.initialize(ecSpec);
		ecKeyPair = ecKeyGen.generateKeyPair();
		ecPubKey = (ECPublicKey) ecKeyPair.getPublic();
		ecPrivKey = (ECPrivateKey) ecKeyPair.getPrivate();
	}

	// PrivateKey als String im Hexadezimalformat zurückgeben
	public String getPrivateKeyString()
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		/*
		 * Der PrivateKey ist noch im ECPrivateKey Objekt gespeichert. Durch die Methode
		 * getS() kann der PrivateKey abgefragt werden, welche als BigInteger
		 * zurückgegeben wird. Durch die Methode toString(16) wird der BigInteger als
		 * String gefüllt mit Hexwerten ausgegeben
		 */

		// Privaten Schlüssel aus dem ECPrivateKey-Objekt abrufen und als
		// Hexadezimalzeichenkette ausgeben
		return adjustTo64(ecPrivKey.getS().toString(16));

	}

	// PrivateKey als BigInteger zurückgeben
	public BigInteger getPrivateKey() {
		return privateKey;
	}

	// PublicKey als String im Hexadezimalformat zurückgeben
	public String getPublicKeyString()
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		/*
		 * Soweit ich weiß, ist der ECPoint nicht anderes als ein Koordinatenpaar,
		 * welches durch den EC Algorithmus aus dem PrivateKey erzeugt wurde. Setzt man
		 * die X- und Y-Koordinaten zusammen, hat man den Public Key. Diesem String muss
		 * jetzt nur noch der Prefix "04" zugefügt werden um einen Bitcoin Public Key zu
		 * erzeugen. Damit haben wir einen 64 bytes großen BTC Public Key mit einem 1
		 * byte großen Prefix.
		 */

		// X- und Y-Koordinaten des öffentlichen Schlüssels abrufen und zusammenfügen
		ECPoint ecPoint = ecPubKey.getW();
		String pointXhex = adjustTo64(ecPoint.getAffineX().toString(16));
		String pointYhex = adjustTo64(ecPoint.getAffineY().toString(16));
		return "04" + pointXhex + pointYhex;
	}

	/*
	 * public ECKey getECKey() { return ecKey; }
	 */

	// Methode, um die Länge des Hexadezimalstrings anzupassen
	protected String adjustTo64(String s)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		/*
		 * Da ich mich für eine übliche Darstellung der Keys mit zweistelligen Hexwerten
		 * entschieden habe, müssen die ECKeys in die passende länge gebracht werden.
		 * Ein Private Key zum Beispiel ist eine 256-bit bzw. 32-byte große Zahl. Eine
		 * zweistellige Hexzahl ist ein Byte groß. Also muss der String 64 Zeichen lang
		 * sein. Wenn der erzeugte Key zu kurz ist, wird er mit maximal zwei Nullen
		 * erweitert, ansonsten verworfen.
		 */
		switch (s.length()) {
		case 62:
			return "00" + s;
		case 63:
			return "0" + s;
		case 64:
			return s;
		default:
			generateKeyPair();
			return "";
		}
	}

	// Generiert SecretKey aus Passwort und Salt
	protected static SecretKey getKeyFromPassword(String password, String salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		// Erzeugt einen SecretKey basierend auf dem eingegebenen Passwort und dem Salt
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
		return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
	}

	// Generiert IvParameterSpec
	protected static IvParameterSpec generateIv() {

		// Erzeugt einen zufälligen Initialisierungsvektor (IV) für die
		// AES-Verschlüsselung
		byte[] ivParam = new byte[16];
		new SecureRandom().nextBytes(ivParam);
		return new IvParameterSpec(ivParam);
	}

	// Verschlüsselt einen String mit Passwort-basierter Verschlüsselung
	protected static String encryptPasswordBased(String plainText, SecretKey key, IvParameterSpec iv)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

		// Verschlüsselt den übergebenen Klartext mit dem SecretKey und dem IV
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		return Base64.encodeBase64String(cipher.doFinal(plainText.getBytes()));
	}

	// Entschlüsselt einen String mit Passwort-basierter Verschlüsselung
	protected String decryptPasswordBased(String cipherText, SecretKey key, IvParameterSpec iv)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

		// Entschlüsselt den übergebenen verschlüsselten Text mit dem SecretKey und dem
		// IV
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		String decryptedPassword = new String(cipher.doFinal(Base64.decodeBase64(cipherText)));
		return decryptedPassword;
	}

	// Liest PrivateKey aus einer Datei
	public boolean readPrivateKeyFromFile() throws FileNotFoundException, IOException, ParseException,
			NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		boolean readed = false;
		while (!readed) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
			fileChooser.setFileFilter(filter);
			int fileChooserResult = fileChooser.showOpenDialog(null);
			String decrypt = new String();

			if (fileChooserResult == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				JSONParser jsonParser = new JSONParser();

				JSONObject pwJson = (JSONObject) jsonParser.parse(new FileReader(selectedFile));
				SecretKey aesKey = null;

				while (decrypt.length() < 6 || decrypt.length() > 10) {
					JPanel panel = new JPanel();
					JLabel label = new JLabel("Passwort:");
					JPasswordField pwField = new JPasswordField(10);
					panel.add(label);
					panel.add(pwField);
					String[] options = new String[] { "OK" };
					int option = JOptionPane.showOptionDialog(null, panel, "Geben Sie ihr Passwort ein",
							JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					try {
						if (option == 0) {
							aesKey = getKeyFromPassword(String.valueOf(pwField.getPassword()), "salt");
							String ivString = (String) pwJson.get("iv");
							String aesKeyString = (String) pwJson.get("pw");
							decrypt = decryptPasswordBased(aesKeyString, aesKey,
									new IvParameterSpec(Base64.decodeBase64(ivString)));
							privateKey = new BigInteger(decrypt, 16);
							// ecKey = ecKey.fromPrivate(new BigInteger(decrypt, 16), false);
							readed = true;
							return true;
						} else {
							int option_2 = JOptionPane.showOptionDialog(null,
									"Wollen Sie den Vorgang wirklich abrechen?", "Achtung", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE, null, null, null);
							if (option_2 == 0) {
								return false;
							}
						}
					} catch (BadPaddingException e) {
						JOptionPane.showMessageDialog(null, "Das eingegebene Passwort ist falsch.", "Fehler",
								JOptionPane.ERROR_MESSAGE);
					}
				}

			} else {
				int option = JOptionPane.showOptionDialog(null, "Wollen Sie den Vorgang wirklich abrechen?", "Achtung",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				if (option == 0) {
					return false;
				}
			}
		}
		return false;
	}

	// Speichert PrivateKey in einer Datei
	public static boolean savePrivateKeyToFile(BigInteger privateKey) throws InvalidKeyException,
			NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException,
			IllegalBlockSizeException, InvalidKeySpecException {

		// Speichert den PrivateKey verschlüsselt in einer Datei, nachdem der Benutzer
		// ein Passwort gewählt hat
		String password = "";
		while (password.length() < 6 || password.length() > 10) {
			JPanel panel = new JPanel();
			JLabel label = new JLabel("mindestens 6, maximal 10 Zeichen");
			JPasswordField pwField = new JPasswordField(10);
			panel.add(label);
			panel.add(pwField);
			String[] options = new String[] { "Ok", "Abbrechen" };
			int option = JOptionPane.showOptionDialog(null, panel, "Wählen Sie das Passwort für Ihre Wallet-Datei",
					JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (option == 0) {
				password = String.valueOf(pwField.getPassword());
				if (password.length() < 6)
					JOptionPane.showMessageDialog(null, "Passwort zu kurz, bitte neues Passwort eingeben", "Fehler",
							JOptionPane.ERROR_MESSAGE);
				else if (password.length() > 10)
					JOptionPane.showMessageDialog(null, "Passwort zu lang, bitte neues Passwort eingeben", "Fehler",
							JOptionPane.ERROR_MESSAGE);
			} else {
				int option_2 = JOptionPane.showOptionDialog(null, "Wollen Sie den Vorgang wirklich abrechen?",
						"Achtung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				if (option_2 == 0) {
					return false;
				}
			}
		}
		IvParameterSpec ivParam = generateIv();
		String encrypt = encryptPasswordBased(privateKey.toString(16), getKeyFromPassword(password, "salt"), ivParam);
		JSONObject pwFile = new JSONObject();

		pwFile.put("pw", encrypt);
		pwFile.put("iv", Base64.encodeBase64String(ivParam.getIV()));
		boolean finish = false;
		while (!finish) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
			fileChooser.setFileFilter(filter);
			int fileChooserResult = fileChooser.showSaveDialog(null);

			if (fileChooserResult == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				if (!selectedFile.getName().endsWith(".json")) {
					selectedFile = new File(selectedFile.getAbsolutePath() + ".json");
				}
				try {

					// Schreibt den verschlüsselten PrivateKey und den IV in die ausgewählte Datei
					FileWriter writer = new FileWriter(selectedFile);
					writer.write(pwFile.toString());
					writer.close();
					return true;
				} catch (IOException e) {
					return false;
				}
			} else {
				int option = JOptionPane.showOptionDialog(null, "Möchten Sie den Vorgang wirklich abbrechen?",
						"Achtung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, 1);
				if (option == 0) {
					finish = true;
				}
			}
		}
		return false;
	}
}
