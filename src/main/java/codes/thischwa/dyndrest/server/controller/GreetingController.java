package codes.thischwa.dyndrest.server.controller;

import codes.thischwa.dyndrest.model.config.AppConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

/** A simple controller that delivers a welcome page without basic-auth. */
@Controller
public class GreetingController {

  private final AppConfig appConfig;

  /**
   * Constructor for GreetingController.
   *
   * @param appConfig the application configuration
   */
  public GreetingController(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  /**
   * Delivers a welcome page.
   *
   * @return the welcome page
   */
  @SuppressWarnings("SameReturnValue")
  @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
  public String greeting() {
    if (!appConfig.greetingEnabled()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    return "about";
  }
}
