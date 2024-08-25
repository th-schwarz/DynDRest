package codes.thischwa.dyndrest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface RouterRoutes {

  @Operation(
      summary =
          "Updates the desired IP addresses of the 'host'. If both parameters for IP addresses aren't set, an attempt is made to fetch the remote IP. It is an alternative route for routers which requires the GET method!")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "The IPs are still up to date."),
        @ApiResponse(
            responseCode = "201",
            description =
                "One or both IPs are changed. Update successful processed. (Dependent on the configuration the response code could be '200'! "),
        @ApiResponse(
            responseCode = "400",
            description =
                "At least one IP address isn't valid or the remote IP couldn't be determined."),
        @ApiResponse(
            responseCode = "403",
            description = "If the 'apiToken' doesn't belong to the host."),
        @ApiResponse(responseCode = "404", description = "The desired host doesn't exists."),
        @ApiResponse(responseCode = "500", description = "If the update failed.")
      })
  @GetMapping(value = "/router/{host}")
  ResponseEntity<Void> routerUpdateHost(
      @Schema(
              description =
                  "The host, for which the IPs must be updated. It has to be a full domain name.",
              type = "string",
              example = "mydyndns.domain.com")
          @PathVariable
          String host,
      @Schema(
              description = "The 'apiToken' to authenticate the changes of the IPs for this host.",
              type = "string")
          @RequestParam
          String apiToken,
      @Schema(description = "An IPv4 address.", type = "string", examples = "127.1.2.4")
          @RequestParam(name = "ipv4", required = false)
          InetAddress ipv4,
      @Schema(
              description = "An IPv6 address.",
              type = "string",
              examples = "2a03:4000:41:32:0:0:0:2")
          @RequestParam(name = "ipv6", required = false)
          InetAddress ipv6,
      HttpServletRequest req);
}
