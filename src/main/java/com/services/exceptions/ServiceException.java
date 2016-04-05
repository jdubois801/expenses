package com.services.exceptions;

public class ServiceException extends Exception {
	private static final long serialVersionUID = -520436494063108848L;

	public ServiceException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
