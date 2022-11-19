package codes.thischwa.dyndrest.provider;

public class ProviderException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProviderException(String message) {
		super(message);
	}

	public ProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProviderException(Throwable cause) {
		super(cause);
	}
}
