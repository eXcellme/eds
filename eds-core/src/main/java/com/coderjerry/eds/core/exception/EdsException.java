package com.coderjerry.eds.core.exception;

public class EdsException extends RuntimeException{

	private static final long serialVersionUID = -112633572845426921L;
	
	public EdsException(String message, Throwable t) {
		super(message,t);
	}
	
	public EdsException(String message) {
		super(message);
	}
	
	public EdsException(Throwable t){
		super(t);
	}

}
