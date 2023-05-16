package com.academy.util.common;

import java.util.HashMap;

import org.krysalis.barcode4j.impl.code128.DefaultCode128Encoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyEncoderUtil {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyEncoderUtil.class);

	/*
	 * Method for testing the encoded utility as a service using API Tester if
	 * configured
	 */
	public Document Encode_Code128_NCHAR(YFSEnvironment env, Document inDoc) {
		log.beginTimer(this.getClass() + ".Encode_Code128_NCHAR");
		log.verbose("Entering AcademyEncoderUtil.Encode_Code128_NCHAR :: Input Doc" + XMLUtil.getXMLString(inDoc));
		Element eleInDoc = inDoc.getDocumentElement();
		String v_value = eleInDoc.getAttribute(AcademyConstants.ATTR_DECODED_VALUE);
		String v_returnValue = Encode_Code128_NCHAR(v_value);
		if (!YFCCommon.isVoid(v_returnValue)) {
			eleInDoc.setAttribute(AcademyConstants.ATTR_ENCODED_VALUE, v_returnValue);
		}
		log.verbose("Entering AcademyEncoderUtil.Encode_Code128_NCHAR :: Output Doc" + XMLUtil.getXMLString(inDoc));
		log.endTimer(this.getClass() + ".Encode_Code128_NCHAR");
		return inDoc;
	}

	/*
	 * Method for performing encoding logic on the input value passed and return
	 * encoded value. Developed as part of Hip Printer functionality for BOPIS 2.0 -
	 * OMNI-10542
	 * 
	 * Encode_Code128_NCHAR preferred for scanning Alphanumeric values
	 */

	public static String Encode_Code128_NCHAR(String v_value) {
		int v_charPos = 0;
		int v_minCharPos = 0;
		int v_currentChar = 0;
		int v_checksum = 0;
		int v_isTableB = 0;
		int v_isValid = 0;
		int v_charCount = 0;
		String v_returnValue = "";
		int v_valueLength = 0;
		int v_loop = 0;

		v_loop = 1;
		v_charCount = 1;
		v_isTableB = 1;
		v_isValid = 1;
		v_returnValue = "";
		v_valueLength = LENGTH(v_value);

		if (v_valueLength > 0) { /* then */
			// 01
			{ /* begin */
				while (v_charCount <= v_valueLength) { /* loop */
					{ /* begin */
						v_currentChar = ASCII(SUBSTR(v_value, v_charCount, 1));

						if (NOT(v_currentChar >= 32 && v_currentChar <= 126)) { /* then */
							{ /* begin */
								v_isValid = 0;
								break;
							} /* end */
						} /* end if */

						v_charCount = v_charCount + 1;
					} /* end */
				} /* end loop */

				// 1st While Begin End

				if (v_isValid == 1) { /* then */
					{ /* begin */
						v_charPos = 1; // 0 to 1

						while (v_charPos <= v_valueLength) { /* loop */
							{ /* begin */
								if (v_isTableB == 1) { /* then */
									{ /* begin */
										// isTableB
										v_minCharPos = (v_charPos == 1 || (v_charPos + 3 == v_valueLength)) ? 4 : 6;
										v_minCharPos = Encode_Code128_IsNumber_NCHAR(v_value, v_charPos, v_minCharPos);

										if (v_minCharPos < 0) { /* then */
											{ /* begin */
												if (v_charPos == 1) { /* then */
													{ /* begin */
														v_returnValue = NCHR(205);
													} /* end */
												} else {
													{ /* begin */
														v_returnValue = v_returnValue + NCHR(199);
													} /* end */
												} /* end if */

												v_isTableB = 0;
											} /* end */
										} else {
											{ /* begin */
												if (v_charPos == 1) { /* then */
													{ /* begin */
														v_returnValue = NCHR(204);
													} /* end */
												} /* end if */
											} /* end */
										} /* end if */
									} /* end */
								} /* end if */

								// isTable B
								if (v_isTableB == 0) { /* then */
									{ /* begin */
										// Not isTable B
										v_minCharPos = 2;
										// System.out.println("Values: "+ v_value+" "+ v_charPos+" "+v_minCharPos);
										v_minCharPos = Encode_Code128_IsNumber_NCHAR(v_value, v_charPos, v_minCharPos);
										// System.out.println("Values: "+ v_value+" "+ v_charPos+" "+v_minCharPos);

										if (v_minCharPos < 0) { /* then */
											{ /* begin */
												v_currentChar = CAST(SUBSTR(v_value, v_charPos, 2)/* as number */);
												v_currentChar = (v_currentChar < 95) ? v_currentChar + 32
														: v_currentChar + 100;
												v_returnValue = v_returnValue + NCHR(v_currentChar);
												v_charPos = v_charPos + 2;
											} /* end */
										} else {
											{ /* begin */
												v_returnValue = v_returnValue + NCHR(200);
												v_isTableB = 1;
											} /* end */
										} /* end if */
									} /* end */
								} /* end if */

								// Not isTable B
								if (v_isTableB == 1) { /* then */
									{ /* begin */
										// isTable B
										v_returnValue = v_returnValue + SUBSTR(v_value, v_charPos, 1);
										v_charPos = v_charPos + 1;
									} /* end */
								} /* end if */
							} /* end */
						} /* end loop */

						// isTable B
						// 2nd While Begin End
						// Calculatation of checksum

						v_checksum = 0;

						while (v_loop <= LENGTH(v_returnValue)) { /* loop */
							{ /* begin */
								v_currentChar = ASCII(SUBSTR(v_returnValue, v_loop, 1));
								v_currentChar = (v_currentChar < 127) ? v_currentChar - 32 : v_currentChar - 100;

								if (v_loop == 1) { /* then */
									{ /* begin */
										v_checksum = v_currentChar;
									} /* end */
								} else {
									{ /* begin */
										v_checksum = MOD((v_checksum + ((v_loop - 1) * v_currentChar)), 103);
									} /* end */
								} /* end if */

								v_loop = v_loop + 1;
							} /* end */
						} /* end loop */

						// 3rd While Begin End
						// Calculation of the checksum ASCII code

						v_checksum = (v_checksum < 95) ? v_checksum + 32 : v_checksum + 100;
						// Add the checksum and the STOP
						v_returnValue = v_returnValue + NCHR(v_checksum) + NCHR(206);
					} /* end */
				} /* end if */

				v_returnValue = v_returnValue.replaceAll(" ", NCHR(194));
				return v_returnValue;

			} /* end */
		} /* end if */

		// 01
		return "";
	} /* end */

	/*
	 * encode is similar to Encode_Code128_NCHAR but differ with odd number scanning
	 */
	public static String encode(String v_value) {
		String returnvalue = "";
		DefaultCode128Encoder code128 = new DefaultCode128Encoder();
		int[] encodedData = code128.encode(v_value);
		int checksum = encodedData[0];

		for (int i = 0; i < encodedData.length; i++) {
			checksum += i * encodedData[i];
			returnvalue += (char) codec1[encodedData[i]];
		}

		checksum %= 103;
		returnvalue += (char) codec1[checksum];
		returnvalue += (char) 206;

		returnvalue = returnvalue.replaceAll(" ", NCHR(194));
		return returnvalue;
	}

	public static void compareEncodedValues(String v_value) {
		String oldEncoding = Encode_Code128_NCHAR(v_value);
		String newEncoding = encode(v_value);

		if (oldEncoding.equals(newEncoding)) {
			System.out.println("<br>v_value = " + v_value + " : Same barcode from old and new function");
			System.out.println("<br>Encoding = " + oldEncoding);
			System.out.println("<br>EncodingBarcode = " + getHTMLBarcode(oldEncoding));
			System.out.println("<hr>");
		} else {
			System.out.println("<br>v_value = " + v_value + " : Different barcode from old and new function");
			System.out.println("<br>oldEncoding = " + oldEncoding);
			System.out.println("<br>oldEncodingBarcode = " + getHTMLBarcode(oldEncoding));
			System.out.println("<br>newEncoding = " + newEncoding);
			System.out.println("<br>newEncodingBarcode = " + getHTMLBarcode(newEncoding));
			System.out.println("<hr>");
		}
	}

	/*
	 * public static int getCodeSet(String v_value) {
	 * if(v_value.matches(".*[a-z]+.*")) { return Code128Constants.CODESET_B; } else
	 * if(v_value.matches(".*[A-Z]+.*")) { return Code128Constants.CODESET_A; } else
	 * if(v_value.matches(".*[0-9]+.*")) { if(v_value.length()%2 == 0) { return
	 * Code128Constants.CODESET_C; } else { return Code128Constants.CODESET_B; } }
	 * 
	 * return Code128Constants.CODESET_ALL; }
	 */

	public static String getHTMLBarcode(String encodedValue) {
		setReverseMap();
		boolean black = true;
		String returnValue = "<table border='0' cellpadding='0' cellspacing='0'><tr height='50px'>";
		String bars = "";
		String asciiValues = "";

		for (int i = 0; i < encodedValue.length(); i++) {
			String encodedChar = encodedValue.substring(i, i + 1);
			bars += codec5[reverseMap.get(ASCII(encodedChar))];
			asciiValues += ASCII(encodedChar) + ",";
		}

		for (int i = 0; i < bars.length(); i++) {
			String bar = bars.substring(i, i + 1);
			returnValue += "<td style='background-color:" + (black ? "black" : "white") + ";width:"
					+ Integer.parseInt(bar) * 2 + "px' ></td>";
			black = !black;
		}

		returnValue += "</tr></table><br>";
		returnValue = encodedValue + "<br>" + asciiValues + "<br>" + returnValue;
		return returnValue;
	}

	public static void setReverseMap() {
		if (reverseMap == null) {
			reverseMap = new HashMap<Integer, Integer>();

			for (int i = 0; i < codec1.length; i++) {
				reverseMap.put(codec1[i], i);
			}
		}
	}

	private static int Encode_Code128_IsNumber_NCHAR(String v_inputValue, int v_charPos, int P_minCharPos) {
		int v_minCharPos = 0;
		v_minCharPos = P_minCharPos - 1;

		if (v_charPos + v_minCharPos <= LENGTH(v_inputValue)) {
			{
				while (v_minCharPos >= 0) {
					{
						if ((ASCII(SUBSTR(v_inputValue, v_charPos + v_minCharPos, 1)) < 48)
								|| (ASCII(SUBSTR(v_inputValue, v_charPos + v_minCharPos, 1)) > 57)) {
							{
								break;
							}
						}

						v_minCharPos = v_minCharPos - 1;

					}
				}

				return v_minCharPos;
			}
		}

		return 0;
	}

	private static int ASCII(String v_value) {
		// System.out.println("ASCII = " + v_value + " - " + (int)v_value.charAt(0));
		return (int) v_value.charAt(0);
	}

	private static int CAST(String v_value) {
		// System.out.println("CAST = " + v_value + " - " + Integer.parseInt(v_value));
		return Integer.parseInt(v_value);
	}

	private static String SUBSTR(String v_inputValue, int start, int length) {
		return v_inputValue.substring(start - 1, start - 1 + length);
	}

	private static int LENGTH(String v_inputValue) {
		return v_inputValue.length();
	}

	private static boolean NOT(boolean value) {
		return !value;
	}

	private static String NCHR(int value) {
		// System.out.println("NCHR = " + value + " - " + (char)value+"");
		return (char) value + "";
	}

	private static int MOD(int number1, int number2) {
		return number1 % number2;
	}

	static HashMap<Integer, Integer> reverseMap = null;
	static int[] codec1 = new int[] { 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
			52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
			79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104,
			105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125,
			126, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 0, 0, 206, 194 };
	static int[] codec2 = new int[] { 194, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
			52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
			79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104,
			105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125,
			126, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 0, 0, 206, 194 };
	static int[] codec3 = new int[] { 212, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
			52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
			79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104,
			105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125,
			126, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 0, 0, 211, 212 };
	static int[] codec4 = new int[] { 252, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
			52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
			79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104,
			105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125,
			126, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 0, 0, 251, 252 };
	static int[] codec5 = new int[] { 212222, 222122, 222221, 121223, 121322, 131222, 122213, 122312, 132212, 221213,
			221312, 231212, 112232, 122132, 122231, 113222, 123122, 123221, 223211, 221132, 221231, 213212, 223112,
			312131, 311222, 321122, 321221, 312212, 322112, 322211, 212123, 212321, 232121, 111323, 131123, 131321,
			112313, 132113, 132311, 211313, 231113, 231311, 112133, 112331, 132131, 113123, 113321, 133121, 313121,
			211331, 231131, 213113, 213311, 213131, 311123, 311321, 331121, 312113, 312311, 332111, 314111, 221411,
			431111, 111224, 111422, 121124, 121421, 141122, 141221, 112214, 112412, 122114, 122411, 142112, 142211,
			241211, 221114, 413111, 241112, 134111, 111242, 121142, 121241, 114212, 124112, 124211, 411212, 421112,
			421211, 212141, 214121, 412121, 111143, 111341, 131141, 114113, 114311, 411113, 411311, 113141, 114131,
			311141, 411131, 211412, 211214, 211232, 233111, 211133, 2331112, 212222 };

	/*
	 * Encoding method - opted out because it required replacement of barcode4j-2.0.jar with new jar barcode4j.jar
	 * singleEncoding uses single type of encoding for alphanumeric
	 */
	
	/*public static String singleEncoding(String v_value) {
		String returnvalue = "";

		DefaultCode128Encoder code128 = new DefaultCode128Encoder(getCodeSet(v_value));
		int[] encodedData = code128.encode(v_value);
		int checksum = encodedData[0];

		for (int i = 0; i < encodedData.length; i++) {
			checksum += i * encodedData[i];
			returnvalue += (char) codec1[encodedData[i]];
		}

		checksum %= 103;
		returnvalue += (char) codec1[checksum];
		returnvalue += (char) 206;

		return returnvalue;
	}*/
	
	/*
	 * Encoding method - opted out because it required replacement of
	 * barcode4j-2.0.jar with new jar barcode4j.jar
	 * 
	 */
	
	/*public static String encodingB(String v_value) {
		String returnvalue = "";

		DefaultCode128Encoder code128 = new DefaultCode128Encoder(Code128Constants.CODESET_B);
		int[] encodedData = code128.encode(v_value);
		int checksum = encodedData[0];

		for (int i = 0; i < encodedData.length; i++) {
			checksum += i * encodedData[i];
			returnvalue += (char) codec1[encodedData[i]];
		}

		checksum %= 103;
		returnvalue += (char) codec1[checksum];
		returnvalue += (char) 206;

		return returnvalue;
	}*/
}