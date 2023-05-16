package com.academy.util.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;


public class IncrementVersion {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		String comAboutFilePath=args[0]+"/"+"about.properties";
		String comAboutFilePath1=args[0]+"/"+"about1.properties";
		
		FileInputStream is = new FileInputStream(comAboutFilePath);
		InputStreamReader isr = new InputStreamReader(is);
		try (BufferedReader br = new BufferedReader(isr)) {//OMNI-90680
			FileOutputStream os = new FileOutputStream(comAboutFilePath1);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			try(BufferedWriter out = new BufferedWriter(osw)){//OMNI-90680	
				String line;
				String s=null;
				int version = 0;
				String newVersion = null;
				while((line = br.readLine()) != null) {
					if(line.startsWith("Build")) {				
						s = line.substring(6, line.length());					
						String str = s.substring(s.lastIndexOf("-")+1, s.length());
					
						if(s.lastIndexOf("-") > 0) {
							version = Integer.parseInt(str);
						}
				
						DecimalFormat f = new DecimalFormat("000");
						f.format(version+1);
						if(s.lastIndexOf("-") > 0) {
							newVersion = s.substring(0, s.lastIndexOf("-")+1)+ f.format(version+1);
						}else {
							newVersion = s.concat("-"+f.format(version+1));
						}
					
						out.write("Build="+newVersion);
						out.newLine();					
					}
					else {
						out.write(line);
						out.newLine();
					}			
			}
			out.close();
			}//OMNI-90680
			os.close();
		}//OMNI-90680
		FileInputStream is1 = new FileInputStream(comAboutFilePath1);
		InputStreamReader isr1 = new InputStreamReader(is1);
		try (BufferedReader br1 = new BufferedReader(isr1)) {//OMNI-90680
			FileOutputStream os1 = new FileOutputStream(comAboutFilePath);
			OutputStreamWriter osw1 = new OutputStreamWriter(os1);
			try(BufferedWriter out1 = new BufferedWriter(osw1)){//OMNI-90680
				String line1;
				while((line1 = br1.readLine()) != null) {
					out1.write(line1);
					out1.newLine();
				}
			out1.close();
			}//OMNI-90680
			os1.close();
		}//OMNI-90680
	}
}