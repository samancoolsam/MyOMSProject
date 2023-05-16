 package com.academy.ecommerce.sterling.bopis.print.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.BarcodeUtil;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import com.yantra.yfc.util.YFCCommon;

public class AcademyPrintUtils {
	
	private static String MODULE_WIDTH = "0.28mm";
	private static String MODULE_HEIGHT = "8mm";
	
	/**
	 * @param documentNumber - id for which barcode is generated
	 * @param bcType - barcode type
	 * @param imageType - image format (png, jpeg)
	 * @return String - image HTML tag with raw data uri for barcode image 
	 * @throws Exception
	 */
	
	public static String genBarCodetag(String documentNumber, String bcType, String imageType
			) throws Exception {

		try {
			
			String imgDataString=genBarcodeImageString(documentNumber, bcType, imageType);
			
			if (imgDataString != null) {

			return "data:image/" + imageType + ";base64,"
					+ imgDataString;
			}
			else 
				return "";

		} catch (Exception e) {
			throw e;
		}
	}
	

	
	/**
	 * @param documentNumber - id for which barcode is generated
	 * @param bcType - barcode type
	 * @param imageType - image format (png, jpeg)
	 * @return String - image encoded as base64 string
	 * @throws Exception
	 */
	public static String genBarcodeImageString(String documentNumber, String bcType, String imageType
			) throws Exception {

		try {
			
			DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
			
			if (YFCCommon.isStringVoid(bcType)) {
				imageType = "code128";
			}
			
			InputStream in = new ByteArrayInputStream(getConfiguration(bcType).getBytes());
			
			Configuration cfg = builder.build(in);
			
			in.close();
			
			BarcodeGenerator gen = BarcodeUtil.getInstance()
					.createBarcodeGenerator(cfg);
			
			BitmapCanvasProvider canvas = new BitmapCanvasProvider(300,
					BufferedImage.TYPE_BYTE_BINARY, false, 0);
			
			gen.generateBarcode(canvas, documentNumber);
			
			BufferedImage image = canvas.getBufferedImage();
			
			canvas.finish();
			
			if (YFCCommon.isStringVoid(imageType)) {
				imageType = "png";
			}
			

			return  encodeToString(image, imageType);
					

		} catch (IOException e) {
			throw e;
		}
	}
	
	
	/**
	 * Method to create configuration xml string for barcode
	 * @param bcType Barcode type code128, code39, ean13 etc 
	 * @return String - Configuration xml string
	 */
	private static String getConfiguration(String bcType) {
		
		String configuration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><barcode><"
				+ bcType
				+ ">"
				+ "<height>"
				+ MODULE_HEIGHT
				+ "</height>"
				+ "<module-width>"
				+ MODULE_WIDTH
				+ "</module-width>"
				+ "</" + bcType + ">" + "</barcode>";
		
		return configuration;
	}

	/**
	 * Method to create Image Base64 String from image file
	 * @param BufferedImage image
	 *        String type(png,jpg)
	 * @return String - Image base64 encrypted
	 */
	
	public static String encodeToString(BufferedImage image, String type)
			throws Exception {
		String imageString = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, type, bos);
			imageString = DatatypeConverter
					.printBase64Binary(bos.toByteArray());
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		return imageString;
	}


}