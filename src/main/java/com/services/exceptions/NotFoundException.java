package com.services.exceptions;

public class NotFoundException extends Exception {

	private static final long serialVersionUID = -2039373425669556455L;

	public NotFoundException(String msg) {
		super(msg);
	}
}
