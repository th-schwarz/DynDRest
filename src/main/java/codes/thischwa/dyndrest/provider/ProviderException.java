package codes.thischwa.dyndrest.provider;

/**
 * The provider exception can be thrown while calls to the provider.
 * It should be used by the implementations of the {@link Provider} interface.
 */
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
