package com.msgs;

public class ResponseId extends ResponseMsg {

	private String id;
	
	public ResponseId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
