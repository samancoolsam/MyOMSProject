package com.academy.ecommerce.sterling.util;

import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.log.YFCLogCategory;


public class AcademyHmacGenerationForPayeezy {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyHmacGenerationForPayeezy.class);

	public static  Map<String, String> getSecurityKeys(Map<String, String> returnMap, String payLoad) throws Exception {
		log.verbose("Begin AcademyHmacGenerationForPayeezy.getSecurityKeys() ");
		
		try {
			log.verbose("nonce used ::::::::" + returnMap.get(AcademyConstants.WEBSERVICE_NONCE));
			log.verbose("timestamp used ::::::::" + Long.toString(System.currentTimeMillis()));
			returnMap.put(AcademyConstants.WEBSERVICE_TIMESTAMP, Long.toString(System.currentTimeMillis()));
			returnMap.put(AcademyConstants.WEBSERVICE_PAYLOAD, payLoad);

			log.verbose("before getmac call ");
			returnMap.put(AcademyConstants.WEBSERVICE_AUTHORIZATION, 
					getMacValue(returnMap));
			log.verbose("after getmac call ");

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		log.verbose("End AcademyHmacGenerationForPayeezy.getSecurityKeys() ");
		return returnMap;
	}


	public static String getMacValue(Map<String, String> data) throws Exception {

		Mac mac = Mac.getInstance(data.get(AcademyConstants.WEBSERVICE_HMAC));
		String apiSecret = data.get(AcademyConstants.WEBSERVICE_API_SECRET);

		SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(), data.get(AcademyConstants.WEBSERVICE_HMAC));
		mac.init(secret_key);
		StringBuilder buff = new StringBuilder();
		buff.append(data.get(AcademyConstants.WEBSERVICE_APIKEY))
			.append(data.get(AcademyConstants.WEBSERVICE_NONCE)).append(data.get(AcademyConstants.WEBSERVICE_TIMESTAMP));
		
		if (data.get(AcademyConstants.WEBSERVICE_TOKEN) != null) {
			buff.append(data.get(AcademyConstants.WEBSERVICE_TOKEN));
		}
		if (data.get(AcademyConstants.WEBSERVICE_PAYLOAD) != null) {
			log.verbose("appending payload:::::" + data.get(AcademyConstants.WEBSERVICE_PAYLOAD));
			buff.append(data.get(AcademyConstants.WEBSERVICE_PAYLOAD));
		}

		log.verbose("Buffer Output::"+buff);
		byte[] macHash = mac.doFinal(buff.toString().getBytes(AcademyConstants.STR_UTF8));       

		//We will use DatatypeConveter as Base64 (commons.codec jar) is being reffrenced in many other 
		//jarfiles and which causes No Such method error due to referencing old jar file
		String authorizeStringTest = DatatypeConverter.printBase64Binary(toHex(macHash));
		log.verbose("HMAC Output(DataTyPeConverter):::" + authorizeStringTest);

		return authorizeStringTest;
	}

	public static byte[] toHex(byte[] arr) {
		String hex = DatatypeConverter.printHexBinary(arr);
		log.verbose("value printed by DatatypeConverter.printHexBinary"+ hex);
		//We will use DatatypeConveter as Hex (commons.codec jar) is being reffrenced in many other
		//jarfiles and which causes No Such method error due to referencing old jar file
		return hex.toLowerCase().getBytes();
	}


}
