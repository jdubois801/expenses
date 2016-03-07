package expense.api.services.exceptions;

public class ValidationException extends Exception {
	private static final long serialVersionUID = -641863893501736075L;

	public ValidationException(String msg) {
		super(msg);
	}
	
	public ValidationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
