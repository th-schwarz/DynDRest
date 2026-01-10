package codes.thischwa.dyndrest.provider;

import java.io.Serial;

/** Thrown by the update hooks. */
public class UpdateHookException extends Exception {

  @Serial private static final long serialVersionUID = 1L;

  public UpdateHookException(String message) {
    super(message);
  }

  public UpdateHookException(String message, Throwable cause) {
    super(message, cause);
  }
}
