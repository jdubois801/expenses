package com.msgs;

public class ResponseErr extends ResponseMsg {

	private String error;
	
	public ResponseErr(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
