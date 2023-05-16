package com.academy.util.common;



import com.academy.util.logger.Logger;
import com.yantra.yfs.japi.YFSException;

public class AcademyCustomException extends YFSException {
	private Logger log = null;
	
	public AcademyCustomException(String errorMessage, String errorCode, String errorDescription)
	{
		super(errorMessage,errorCode,errorDescription);
		log.verbose("errorCode:::::::::::::::::::::::::::::::::::::::::::" + errorCode);
		log.verbose("errorDescription:::::::::::::::::::::::::::::::::::::::::::" + errorCode);
	}
	
}