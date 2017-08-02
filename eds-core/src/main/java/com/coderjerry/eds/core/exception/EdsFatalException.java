package com.coderjerry.eds.core.exception;

public class EdsFatalException extends EdsException{

	private static final long serialVersionUID = -112633572845426921L;
	
	public EdsFatalException(String message, Throwable t) {
		super(message,t);
	}
	
	public EdsFatalException(String message) {
		super(message);
	}
	
	public EdsFatalException(Throwable t){
		super(t);
	}

}
