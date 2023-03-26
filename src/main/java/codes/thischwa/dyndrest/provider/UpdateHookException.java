package codes.thischwa.dyndrest.provider;

public class UpdateHookException extends Exception {

  private static final long serialVersionUID = 1L;

  public UpdateHookException(String message) {
    super(message);
  }

  public UpdateHookException(String message, Throwable cause) {
    super(message, cause);
  }
}
