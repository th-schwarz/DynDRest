package codes.thischwa.dyndrest.provider;

import java.io.Serial;

/**
 * The provider exception can be thrown while calls to the provider.
 * It should be used by the implementations of the {@link Provider} interface.
 */
public class ProviderException extends Exception {

  @Serial
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
