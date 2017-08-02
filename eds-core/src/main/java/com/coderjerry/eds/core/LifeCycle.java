package com.coderjerry.eds.core;

public interface LifeCycle {
	
	String id();
	
	void initialize() ;
	
	void destroy();
	
}
