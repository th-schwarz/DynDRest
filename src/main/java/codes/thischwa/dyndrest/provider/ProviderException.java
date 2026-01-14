package codes.thischwa.dyndrest.provider;

import java.io.Serial;

/**
 * The provider exception can be thrown while calls to the provider. It should be used by the
 * implementations of the {@link Provider} interface.
 */
public class ProviderException extends Exception {

  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructs a new ProviderException with the specified detail message.
   *
   * @param message the detail message
   */
  public ProviderException(String message) {
    super(message);
  }

  /**
   * Constructs a new ProviderException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause   the cause
   */
  public ProviderException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new ProviderException with the specified cause.
   *
   * @param cause the cause
   */
  public ProviderException(Throwable cause) {
    super(cause);
  }
}
