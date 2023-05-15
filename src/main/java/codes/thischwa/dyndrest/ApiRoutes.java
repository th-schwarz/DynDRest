package codes.thischwa.dyndrest;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLogPage;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

//@formatter:off
@OpenAPIDefinition(
    info = @Info(
        title = "A Dynamic DNS REST Service.",
        description = "The routes of the dynamic DNS API",
        version = "0.1",
        contact = @Contact(
            name = "Thilo Schwarz",
            email = "osp (at) thischwa.codes",
            url = "https://github.com/th-schwarz/DynDRest"
        ),
        license = @License(
            name = "MIT Licence",
            url = "https://github.com/th-schwarz/DynDRest/blob/develop/LICENSE")),
    externalDocs = @ExternalDocumentation(
        description = "DynDRest on Github",
        url = "https://github.com/th-schwarz/DynDRest"
    )
)
//@formatter:on

interface ApiRoutes {

  @Operation(summary = "Updates the desired IP addresses of the 'host', if the 'apitoken' belongs to the host. If both parameters for IP addresses aren't set, an attempt is made to fetch the remote IP.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "The IPs are still up to date.", content = @Content),
      @ApiResponse(responseCode = "201", description = "One or both IPs are changed. Update successful processed.", content = @Content),
      @ApiResponse(responseCode = "400", description = "If the 'apitoken' doesn't belong to the host, IP addresses aren't valid or the remote IP couldn't determine.", content = @Content),
      @ApiResponse(responseCode = "500", description = "If the update fails.", content = @Content)})
  @GetMapping(value = "/update/{host}", produces = MediaType.TEXT_PLAIN_VALUE)
  ResponseEntity<?> update(
      @Parameter(description = "The host, for which the IPs must be updated.", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, examples = {
          @ExampleObject(value = "mydyndns.domain.com")})) @PathVariable String host,
      @Parameter(description = "The 'apitoken' which belongs to the 'host'.", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)) @RequestParam String apitoken,
      @RequestParam(name = "ipv4", required = false) InetAddress ipv4,
      @RequestParam(name = "ipv6", required = false) InetAddress ipv6, HttpServletRequest req);

  @Operation(summary = "Determines the IP settings of the 'host' and returns it in a JSON object.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "A JSON object with the IP settings of the 'host'.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"ipv4\":\"127.1.2.4\",\"ipv6\":\"2a03:4000:41:32:0:0:0:2\"}"))),
      @ApiResponse(responseCode = "400", description = "If the 'apitoken' doesn't belong to the host, IP addresses aren't valid or the remote IP couldn't determine.", content = @Content),
      @ApiResponse(responseCode = "404", description = "If the 'host' isn't configured.", content = @Content),
      @ApiResponse(responseCode = "500", description = "If the zone info fails.", content = @Content)})
  @GetMapping(value = "/info/{host}", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<IpSetting> info(
      @Parameter(description = "The host, for which the IPs must be determined.", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, examples = {
          @ExampleObject(value = "mydyndns.domain.com")})) @PathVariable String host,
      @Parameter(description = "The 'apitoken' which belongs to the 'host'.", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)) @RequestParam String apitoken);

  @Operation(summary = "Delivers pageable update logs.")
  @ApiResponse(responseCode = "200", description = "Ordered list of update logs.")
  @GetMapping(value = "info/update-log", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<UpdateLogPage> deliverLogs(
      @Parameter(description = "Page number to fetch. If it is not committed, it will be '1'.", content = @Content(mediaType = "int")) @Nullable @RequestParam(required = false) Integer page,
      @Parameter(description = "String to search for. It belongs to 'host' and 'timestamp'.", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)) @RequestParam(required = false) String search);
}