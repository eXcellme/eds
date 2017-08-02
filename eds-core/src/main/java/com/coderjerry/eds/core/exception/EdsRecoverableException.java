package com.coderjerry.eds.core.exception;

public class EdsRecoverableException extends EdsException{

	private static final long serialVersionUID = -112633572845426921L;
	
	public EdsRecoverableException(String message, Throwable t) {
		super(message,t);
	}
	
	public EdsRecoverableException(String message) {
		super(message);
	}
	
	public EdsRecoverableException(Throwable t){
		super(t);
	}

}
