package test;

import java.io.IOException;

import org.json.JSONObject;

import transaction.API;

public class Test {

	public static void main(String[] args) throws IOException {

		// Adresseinformationen abrufen
		JSONObject informations = API.getAddressInformations("n19y5mjsPAqjxXyFq8zHKZ7rXsgSaPM8RL", false);

		// Adresseinformationen ausgeben
		System.out.println(informations.toString());

	}

}
