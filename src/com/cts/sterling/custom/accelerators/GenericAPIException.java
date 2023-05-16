package com.cts.sterling.custom.accelerators.exception;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericAPIException.
 */
public class GenericAPIException extends Exception {
	
	
	/** The class name. */
	private String className 	= "";
	
	/** The method name. */
	private String methodName 	= "";
	
	/** The error. */
	private String error 		= "";
	
	/** The error msg. */
	private String errorMsg 	= "";
	
	/**
	 * Instantiates a new generic api exception.
	 */
	public GenericAPIException() {
		super();
	}
	
	/**
	 * Instantiates a new generic api exception.
	 *
	 * @param className the class name
	 * @param methodName the method name
	 * @param error the error
	 */
	public GenericAPIException(String className,String methodName, String error)  {
		
		this.className = className ;
		this.methodName = methodName ;
		this.error = error;		
	
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 */
	public String toString(){
	
		this.errorMsg = 
			"------------------------------------------------------\n" +
			" Error in --> " +
			this.className +
			" --> " +
			this.methodName +
			" --> " +
			this.error +
			"------------------------------------------------------\n" ;
		
		
		return this.errorMsg;
			
	}
}
