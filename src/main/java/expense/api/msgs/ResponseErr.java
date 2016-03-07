package expense.api.msgs;

// a service response message containing an error message.
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
