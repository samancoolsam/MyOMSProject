/*
*
* This software is the confidential and proprietary information of
* Yantra Corp. ("Confidential Information"). You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with Yantra.
*/
package com.academy.util.logger;

import org.apache.log4j.Level;

import com.academy.util.common.ResourceUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.log.YFCLogLevel;
import com.yantra.yfc.log.YFCLogManager;

/**
*	Logger helper.
*/
public class Logger {
   YFCLogCategory logger;
   
   /**
    *	Construct a default Logger
    */
   public Logger() {
       this.logger = YFCLogCategory.instance("com.academy");
   }
   
   /**
    *	Construct a Logger with specified name
    *	@param name Logger name
    */
   public Logger(String name) {
       this.logger = YFCLogCategory.instance(name);
   }
   
   /**
    *	Log message
    *	@param level the level as defined in LogUtil class
    *	@param msg any Object as message.
    */
   public void log( Level level, Object msg ) {
       logger.log( level, msg );
   }
   
    
   public void error(String code) {
       if(isErrorEnabled())
           logger.error(ResourceUtil.resolveMsgCode(code));
   }
   
   public void error(String code, Throwable t) {
       if(isErrorEnabled())
           logger.error(ResourceUtil.resolveMsgCode(code), t);
   }
   
   public void error(String code, Object[] args) {
       if(isErrorEnabled())
           logger.error(ResourceUtil.resolveMsgCode(code, args));
   }
   
   public void error(String code, Object[] args, Throwable t) {
       if(isErrorEnabled())
           logger.error(ResourceUtil.resolveMsgCode(code, args), t);
   }
   
   public void info(String code) {
       if(isInfoEnabled())
           logger.info(ResourceUtil.resolveMsgCode(code));
   }
   
   public void info(String code, Object[] args) {
       if(isInfoEnabled())
           logger.info(ResourceUtil.resolveMsgCode(code, args));
   }
   
   /**
    *	Log DEBUG message
    */
   public void debug( Object msg ) {
       logger.debug(msg );
   }
   
   /**
    *	Log VERBOSE message
    */
   public void verbose(String msg ) {
       logger.verbose(msg );
   }
   
   public boolean isVerboseEnabled() {
       return YFCLogManager.isLevelEnabled(YFCLogLevel.VERBOSE.toInt());
   }
   
   public boolean isDebugEnabled() {
       return YFCLogManager.isLevelEnabled(YFCLogLevel.DEBUG.toInt());
   }
   
   public boolean isInfoEnabled() {
       return YFCLogManager.isLevelEnabled(YFCLogLevel.INFO.toInt());
   }
   
   public boolean isErrorEnabled() {
       return YFCLogManager.isLevelEnabled(YFCLogLevel.ERROR.toInt());
   }
   
   /**
    *	Get default package level Logger.
    *	@return the default Logger
    */
   public static Logger getLogger() {
       return new Logger();
   }
   
   /**
    *	Get Logger by name.
    *	@return the Logger with the name.
    */
   public static Logger getLogger(String name) {
       return new Logger(name);
   }
   
}
