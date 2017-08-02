package com.coderjerry.eds.core.logger.log4j2;

import com.coderjerry.eds.core.logger.Logger;

public class Log4j2Logger implements Logger {

	private final org.apache.logging.log4j.Logger logger;

	public Log4j2Logger(org.apache.logging.log4j.Logger logger) {
		this.logger = logger;
	}

	public void trace(String msg) {
		logger.log(org.apache.logging.log4j.Level.TRACE, msg);
	}

	public void trace(Throwable e) {
		logger.log(org.apache.logging.log4j.Level.TRACE, e == null ? null : e.getMessage(), e);
	}

	public void trace(String msg, Throwable e) {
		logger.log(org.apache.logging.log4j.Level.TRACE, msg, e);
	}

	public void debug(String msg) {
		logger.log(org.apache.logging.log4j.Level.DEBUG, msg);
	}

	public void debug(Throwable e) {
		logger.log(org.apache.logging.log4j.Level.DEBUG, e == null ? null : e.getMessage(), e);
	}

	public void debug(String msg, Throwable e) {
		logger.log(org.apache.logging.log4j.Level.DEBUG, msg, e);
	}

	public void info(String msg) {
		logger.log(org.apache.logging.log4j.Level.INFO, msg);
	}

	public void info(Throwable e) {
		logger.log(org.apache.logging.log4j.Level.INFO, e == null ? null : e.getMessage(), e);
	}

	public void info(String msg, Throwable e) {
		logger.log(org.apache.logging.log4j.Level.INFO, msg, e);
	}

	public void warn(String msg) {
		logger.log(org.apache.logging.log4j.Level.WARN, msg);
	}

	public void warn(Throwable e) {
		logger.log(org.apache.logging.log4j.Level.WARN, e == null ? null : e.getMessage(), e);
	}

	public void warn(String msg, Throwable e) {
		logger.log(org.apache.logging.log4j.Level.WARN, msg, e);
	}

	public void error(String msg) {
		logger.log(org.apache.logging.log4j.Level.ERROR, msg);
	}

	public void error(Throwable e) {
		logger.log(org.apache.logging.log4j.Level.ERROR, e == null ? null : e.getMessage(), e);
	}

	public void error(String msg, Throwable e) {
		logger.log(org.apache.logging.log4j.Level.ERROR, msg, e);
	}

	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	public boolean isWarnEnabled() {
		return logger.isEnabled(org.apache.logging.log4j.Level.WARN);
	}
	
	public boolean isErrorEnabled() {
	    return logger.isEnabled(org.apache.logging.log4j.Level.ERROR);
	}

}