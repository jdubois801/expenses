package expense.api.services.exceptions;

public class OutOfBoundsException extends Exception {
	private static final long serialVersionUID = 314350582364045515L;

	public OutOfBoundsException(String msg) {
		super(msg);
	}
}
