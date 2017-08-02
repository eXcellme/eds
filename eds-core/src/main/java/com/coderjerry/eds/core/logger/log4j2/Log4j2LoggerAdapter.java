package com.coderjerry.eds.core.logger.log4j2;

import java.io.File;

import com.coderjerry.eds.core.logger.Level;
import com.coderjerry.eds.core.logger.Logger;
import com.coderjerry.eds.core.logger.LoggerAdapter;

public class Log4j2LoggerAdapter implements LoggerAdapter {

    private File file ;
    
	public Logger getLogger(Class<?> key) {
		return new Log4j2Logger(org.apache.logging.log4j.LogManager.getLogger(key));
	}

	public Logger getLogger(String key) {
		return new Log4j2Logger(org.apache.logging.log4j.LogManager.getLogger(key));
	}

	public void setLevel(Level level) {
	    org.apache.logging.log4j.core.LoggerContext loggerContext = (org.apache.logging.log4j.core.LoggerContext)org.apache.logging.log4j.LogManager.getContext(false);
	    org.apache.logging.log4j.Level l = toLog4jLevel(level);
	    loggerContext.getConfiguration().getLoggerConfig(org.apache.logging.log4j.LogManager.ROOT_LOGGER_NAME).setLevel(l);
	}

	public Level getLevel() {
		return fromLog4jLevel(org.apache.logging.log4j.LogManager.getRootLogger().getLevel());
	}

	private static org.apache.logging.log4j.Level toLog4jLevel(Level level) {
		if (level == Level.ALL)
			return org.apache.logging.log4j.Level.ALL;
		if (level == Level.TRACE)
			return org.apache.logging.log4j.Level.TRACE;
		if (level == Level.DEBUG)
			return org.apache.logging.log4j.Level.DEBUG;
		if (level == Level.INFO)
			return org.apache.logging.log4j.Level.INFO;
		if (level == Level.WARN)
			return org.apache.logging.log4j.Level.WARN;
		if (level == Level.ERROR)
			return org.apache.logging.log4j.Level.ERROR;
		// if (level == Level.OFF)
			return org.apache.logging.log4j.Level.OFF;
	}

	private static Level fromLog4jLevel(org.apache.logging.log4j.Level level) {
		if (level == org.apache.logging.log4j.Level.ALL)
			return Level.ALL;
		if (level == org.apache.logging.log4j.Level.TRACE)
			return Level.TRACE;
		if (level == org.apache.logging.log4j.Level.DEBUG)
			return Level.DEBUG;
		if (level == org.apache.logging.log4j.Level.INFO)
			return Level.INFO;
		if (level == org.apache.logging.log4j.Level.WARN)
			return Level.WARN;
		if (level == org.apache.logging.log4j.Level.ERROR)
			return Level.ERROR;
		// if (level == org.apache.logging.log4j.Level.OFF)
			return Level.OFF;
	}
	
	@Override
	public File getFile() {
	    return file;
	}
	@Override
	public void setFile(File file) {
	    this.file = file ;
	}

}