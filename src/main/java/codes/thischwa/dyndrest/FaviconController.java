package codes.thischwa.dyndrest;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for favicon.ico, just for reducing 404 errors in the logs. */
@RestController
public class FaviconController {

  @Operation(hidden = true)
  @GetMapping("favicon.ico")
  void returnNoFavicon() {
    // see class comment
  }
}
