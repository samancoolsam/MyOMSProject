package com.academy.ecommerce.yantriks.inventory;

import java.io.BufferedReader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ibm.icu.util.Calendar;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;

public class AcademyJWTTokenGenerator {

	public static YFCLogCategory log = YFCLogCategory.instance(AcademyJWTTokenGenerator.class);

	/**
	 * This service is to get OAuth Token
	 * 
	 * @param env
	 * @param inDoc
	 * @return String
	 * @throws ParserConfigurationException
	 * @throws Exception
	 */
	public static String getAuthToken(String strSystem) throws ParserConfigurationException, Exception {
		String methodName = "AcademyJWTTokenGenerator.getAuthToken()";
		log.beginTimer(methodName);
		RSAPrivateKey key;
		Algorithm algorithm;
		String strToken;
		String strIssuer;
		String strPrivateKeyIdEncrypted;
		String strAudience;
		// In minutes - OAuth Token's actual validity
		// String strTokenActualValidity;
		String strExpiresInMins;
		int iExpiresInMins;
		Date dtAuthExpire;

		log.info("Fetching Properties----");
		key = (RSAPrivateKey) getPrivateKey((String) YFSSystem.getProperty("private_key"));
		strIssuer = YFSSystem.getProperty("issuer");
		strPrivateKeyIdEncrypted = YFSSystem.getProperty("privateKeyId");
		strAudience = YFSSystem.getProperty("audience");
		// strTokenActualValidity = YFSSystem.getProperty("timeToLive");
		strExpiresInMins = YFSSystem.getProperty("timeToLive");
		log.info("Got Properties----");

		// long now = System.currentTimeMillis();
		algorithm = Algorithm.RSA256(null, key);
		strToken = JWT.create().withKeyId(strPrivateKeyIdEncrypted).withIssuer(strIssuer).withSubject(strIssuer)
				.withAudience(strAudience).withIssuedAt(asDate(LocalDateTime.now()))
				.withExpiresAt(asDate(LocalDateTime.now().plusMinutes(Long.parseLong(strExpiresInMins) + 1)))
				.sign(algorithm);

		log.info("Generated Token is " + strToken);

		log.info("OAuth " + strSystem + " - Token Generated : " + strToken);

		iExpiresInMins = parseToInt(strExpiresInMins);

		dtAuthExpire = addToDate(null, Calendar.MINUTE, iExpiresInMins);

		log.info("OAuth - Got new " + strSystem + " Token, set Auth Expiry at:" + dtAuthExpire);

		log.endTimer(methodName);
		return strToken;
	}

	/**
	 * This method is to parse string value to Int
	 * 
	 * @param strValue
	 * @return int
	 */
	private static int parseToInt(String strValue) {
		int iValue = 0;
		try {
			iValue = Integer.parseInt(strValue);
		} catch (Exception exc) {

		}
		return iValue;
	}

	/**
	 * Adds specified interval to input date. Valid values for Interval are
	 * Calendar.YEAR, Calendar.MONTH, Calendar.DATE etc. See Calendar API for more
	 * information
	 * 
	 * @param inputDate Input Date
	 * @param interval  Interval
	 * @param amount    Amount to add(use negative numbers to subtract
	 * @return Date after addition
	 * @throws IllegalArgumentException for Invalid input
	 * @throws Exception                for all others
	 */
	public static Date addToDate(Date inputDate, int interval, int amount) {

		// Validate Input date
		if (inputDate == null) {
			// throw new IllegalArgumentException("Input date cannot be" + " null in
			// DateUtils.addToDate method");
			inputDate = new Date();
		}

		// Get instance of calendar
		Calendar calendar = Calendar.getInstance();

		// Set input date to calendar
		calendar.setTime(inputDate);

		// Add amount to interval
		calendar.add(interval, amount);

		// Return result date;
		return calendar.getTime();
	}

	private static Date asDate(LocalDateTime dateTime) {
		return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	private static PrivateKey getPrivateKey(String privateKey) throws Exception {
		// Read in the key into a String
		StringBuilder pkcs8Lines = new StringBuilder();
		BufferedReader rdr = new BufferedReader(new StringReader(privateKey));
		String line;
		while ((line = rdr.readLine()) != null) {
			pkcs8Lines.append(line);
		}

		// Remove the "BEGIN" and "END" lines, as well as any whitespace
		String pkcs8Pem = pkcs8Lines.toString();
		pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "");
		pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "");
		pkcs8Pem = pkcs8Pem.replaceAll("\\\\n", "");

		// Base64 decode the result
		byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(pkcs8Pem);

		// extract the private key
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(keySpec);

	}
	
	
}
