package jeople.errors;

public class InternalError extends Error {
	private static final long serialVersionUID = 3591012939244790673L;

	public InternalError(String message) {
		super("An unexpected condition has occurred : " + message);
	}

	public InternalError(Throwable cause) {
		super("An unexpected condition has occurred : " + cause.getMessage(),
				cause);
	}

	public InternalError(String message, Throwable cause) {
		super("An unexpected condition has occurred : " + message, cause);
	}

}
