package com.academy.util.common;

/*
 * This software is the confidential and proprietary information of
 * Yantra Corp. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Yantra.
 */
import java.util.StringTokenizer;

/**
 * String utility class.
 */
public final class StringUtil {
    /**
     *	Pad string with space to specified length. If the string length >= the length parameter, no change is made.
     *	@param strPad the string to pad.
     *	@param length the full string length after padding.
     */
    public static String padSpaces( String strPad, int length ) {
        int origLength = strPad.length();
        if (origLength != length) {
            strPad = strPad.trim();
            origLength = strPad.length();
            if ( origLength > length) {
                return strPad;
            } else {
                int spaceLength = length - origLength - 1;
                String strSpace = " ";
                for(int i=0; i< spaceLength; i ++) {
                    strSpace = strSpace + " ";
                }
                return strPad + strSpace;
            }
        }
        return strPad;
    }
    
    /**
     *	Replace (globally) occurance of substring.
     *	@param strInput the String to go through.
     *	@param delim the substring to be replaced.
     *	@param strReplace replacement.
     *	@return replaced String
     */
    public static String escapeChar(String strInput, String delim, String strReplace) {
        StringTokenizer st = new StringTokenizer(strInput,delim, true);
        String strReturn = strInput;
        String strTemp;
        
        if(st.countTokens()>0) {
            strReturn="";
            while (st.hasMoreTokens()) {
                strTemp = st.nextToken();
                if (strTemp.equals(delim)) {
                    strReturn = strReturn + strReplace;
                } else {
                    strReturn = strReturn + strTemp;
                }
            }
        }
        return strReturn;
    }
    
    /**
     *	Return the subclass without specified suffix
     */
    public static String stripSuffix( String value, String suffix ) {
        if( value != null && !value.equals("") ) {
            int suffixStartingIndex = value.lastIndexOf( suffix );
            if( suffixStartingIndex != -1 )
                return value.substring( 0, suffixStartingIndex );
        }
        return value;
    }

    /**
     *	Convert null String to empty String
     */
    public static String nonNull( String value ) {
        if( value == null )
            value = "";
        return value;
    }
    
    /**
     *	Append '/' or '\' to the end of input, based on the input already has '/' or '\'. If the input does not
     *	have '/' or '\', the System property "file.separator"
     */
    public static String formatDirectoryName( String dirName ) {
        if( dirName.charAt(dirName.length()-1) == '\\' || dirName.charAt(dirName.length()-1) == '/' )
            return dirName;
        else {
            if( dirName.indexOf("/") != -1 )
                return dirName+"/";
            
            if( dirName.indexOf("\\") != -1 )
                return dirName+"\\";
        }
        return dirName + System.getProperty("file.separator");
    }
    
    /**
     *	Prepad string with '0'. If the length of the String is >= the input size, no change is made.
     *	@param value the string to pad
     *	@param size the full String length after the pad.
     */
    public static String prepadStringWithZeros( String value, int size ) {
        if( value != null ) {
            int length = value.length();
            if( length > size )
                return value;
            else {
                StringBuffer buffer = new StringBuffer();
                for( int count=0; count<size-length; count++ )
                    buffer.append("0");
                buffer.append( value );
                return buffer.toString();
            }
        }
        return value;
    }
    
    /**
     *	Check if a String is null or can be trimmed to empty.
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.trim().length() == 0);
    }
    
    /**
     *	Convert an empty String (null or "") to a space String.
     */
    public static String padNullEmpty( String val ) {
        if( val == null || val.equals("") ) return " ";
        return val;
    }
    
}
