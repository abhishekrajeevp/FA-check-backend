package com.bp.middleware.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class GetPublicIpAndLocation {

	public String publicIpAddress() throws Exception {

		String publicIPAddress = "Not found";

		String ipServiceUrl = "https://api.ipify.org?format=json";

		HttpURLConnection ipConnection = (HttpURLConnection) new URL(ipServiceUrl).openConnection();
		ipConnection.setRequestMethod("GET");

		int ipResponseCode = ipConnection.getResponseCode();

		if (ipResponseCode == 200) {

			BufferedReader ipIn = new BufferedReader(new InputStreamReader(ipConnection.getInputStream()));
			StringBuilder ipResponse = new StringBuilder();
			String ipInputLine;

			while ((ipInputLine = ipIn.readLine()) != null) {
				ipResponse.append(ipInputLine);
			}
			ipIn.close();

			JSONObject ipJsonResponse = new JSONObject(ipResponse.toString());
			publicIPAddress = ipJsonResponse.getString("ip");
			System.out.println("Public IP Address: " + publicIPAddress);

		}

		return publicIPAddress;
	}

	public JSONObject getLocationUsingPublicIp(String publicIp) throws Exception {

		JSONObject jsonResponse = new JSONObject();

		InetAddress inetAddress = InetAddress.getLocalHost();
		String ipAddress = inetAddress.getHostAddress();
		System.out.println("Local IP Address: " + ipAddress);

		String url = "http://ip-api.com/json/" + publicIp;

		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();

		if (responseCode == 200) {

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			jsonResponse = new JSONObject(response.toString());

			System.err.println("------------->" + jsonResponse);

			System.out.println("IP Address: " + jsonResponse.getString("query"));
			System.out.println("Country: " + jsonResponse.getString("country"));
			System.out.println("Country Code: " + jsonResponse.getString("countryCode"));
			System.out.println("Region: " + jsonResponse.getString("regionName"));
			System.out.println("City: " + jsonResponse.getString("city"));
			System.out.println("ZIP: " + jsonResponse.getString("zip"));
			System.out.println("Latitude: " + jsonResponse.getDouble("lat"));
			System.out.println("Longitude: " + jsonResponse.getDouble("lon"));
			System.out.println("Timezone: " + jsonResponse.getString("timezone"));
			System.out.println("ISP: " + jsonResponse.getString("isp"));
			System.out.println("Organization: " + jsonResponse.getString("org"));
			System.out.println("AS: " + jsonResponse.getString("as"));

		}

		return jsonResponse;
	}

}
