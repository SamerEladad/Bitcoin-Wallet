package phase2.beta5;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

//Die App-Klasse erweitert die JavaFX Application-Klasse und ist für das Laden der Benutzeroberfläche
//und das Anzeigen des Hauptfensters der Anwendung verantwortlich.
public class App extends Application {

	private static Scene scene;
	private static PrimaryController pc;

	// Die start-Methode wird aufgerufen, wenn die JavaFX-Anwendung gestartet wird.
	@Override
	public void start(Stage stage) throws IOException {

		// Laedt das Hauptfenster (BtcAppV2) und setze die Größe des Fensters
		scene = new Scene(loadFXML("/BtcAppV2"), 518, 360);

		// Setzt die Szene für die Bühne (Fenster) und setze den Titel des Fensters
		stage.setScene(scene);
		stage.setTitle("BTC Wallet");

		// Fügt ein Bitcoin-Icon zum Fenster hinzu
		stage.getIcons().add(new Image("/Bitcoin.png"));
		stage.show();
	}

	// Die Funktion setRoot ändert das Wurzelelement der Szene, indem es eine neue
	// FXML-Datei lädt
	static void setRoot(String fxml) throws IOException {
		scene.setRoot(loadFXML(fxml));
	}

	// Die Funktion loadFXML lädt die angegebene FXML-Datei und gibt das
	// Wurzelelement der Szene zurück
	private static Parent loadFXML(String fxml) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
		pc = fxmlLoader.getController();
		return fxmlLoader.load();
	}

	// Die main-Methode startet die JavaFX-Anwendung, indem sie die launch-Methode
	// aufruft
	public static void main(String[] args) {
		launch();
	}

}