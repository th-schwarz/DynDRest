package codes.thischwa.dyndrest;

import codes.thischwa.dyndrest.model.IpSetting;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// @formatter:off
@OpenAPIDefinition(
    info =
        @Info(
            title = "A Dynamic DNS REST Service.",
            description = "The routes of the dynamic DNS API",
            version = "0.2",
            contact =
                @Contact(
                    name = "Thilo Schwarz",
                    email = "osp (at) thischwa.codes",
                    url = "https://github.com/th-schwarz/DynDRest"),
            license =
                @License(
                    name = "MIT Licence",
                    url = "https://github.com/th-schwarz/DynDRest/blob/develop/LICENSE")),
    externalDocs =
        @ExternalDocumentation(
            description = "DynDRest on Github",
            url = "https://github.com/th-schwarz/DynDRest"))
// @formatter:on
// naming conventions: https://restfulapi.net/resource-naming/
interface ApiRoutes {

  @Operation(
      summary =
          "Updates the desired IP addresses of the 'host'. If both parameters for IP addresses aren't set, an attempt is made to fetch the remote IP.")
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
  @PutMapping(value = "/api/ips/{host}")
  ResponseEntity<Void> updateHost(
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
      @Schema(
              description = "An IPv4 address.",
              type = "string",
              examples = "127.1.2.4")
          @RequestParam(name = "ipv4", required = false)
          InetAddress ipv4,
      @Schema(
              description = "An IPv6 address.",
              type = "string",
              examples = "2a03:4000:41:32:0:0:0:2")
      @RequestParam(name = "ipv6", required = false) InetAddress ipv6,
      HttpServletRequest req);

  @Operation(summary = "Determines the IP settings of the 'host' and returns it in a JSON object.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "A JSON object with the IP settings of the 'host'.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            value =
                                "{\"ipv4\":\"127.1.2.4\",\"ipv6\":\"2a03:4000:41:32:0:0:0:2\"}"))),
        @ApiResponse(
            responseCode = "403",
            description =
                "If the 'apiToken' doesn't belong to the host, IP addresses aren't valid or the remote IP couldn't determine.",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "404",
            description = "If the 'host' isn't configured.",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "If the zone info fails.",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @GetMapping(value = "/api/ips/{host}", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<IpSetting> fetchHostIpSetting(
      @Schema(
              description = "The host, for which the IPs must be determined.",
              type = "string",
              example = "mydyndns.domain.com")
          @PathVariable
          String host,
      @Schema(
              description = "The 'apiToken', which must belong to the host'.",
              type = "string")
          @RequestParam
          String apiToken);
}
