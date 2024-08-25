package codes.thischwa.dyndrest;

import codes.thischwa.dyndrest.model.FullHost;
import codes.thischwa.dyndrest.model.Zone;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface AdminRoutes {
  @Operation(summary = "Adds a zone with the specified name and name server.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The desired Zone was successfully created."),
        @ApiResponse(responseCode = "403", description = "The 'adminToken' is wrong."),
        @ApiResponse(
            responseCode = "409",
            description = "A zone with the specified name already exists.")
      })
  @PostMapping(value = "/admin/zones/{zoneName}/{ns}")
  ResponseEntity<Void> addZone(
      @Schema(
              description = "The name of the zone, must be a valid domain name.",
              type = "string",
              example = "domain.com")
          @PathVariable
          String zoneName,
      @Schema(
              description = "The name server of the zone, must be a valid domain name.",
              type = "string",
              example = "ns1.domain.info")
          @PathVariable
          String ns,
      @Schema(description = "The 'admin-token' to authorize the operation.", type = "string")
          @RequestParam
          String adminToken);

  @Operation(summary = "Deletes the zone with the specified name.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "The Zone deleted successful."),
        @ApiResponse(responseCode = "404", description = "The zone doesn't exists."),
        @ApiResponse(responseCode = "403", description = "The 'adminToken' is wrong."),
      })
  @DeleteMapping(value = "/admin/zones/{name}")
  ResponseEntity<Void> deleteZone(
      @Schema(
              description = "The name of the zone, must be a valid domain name.",
              type = "string",
              example = "domain.com")
          @PathVariable
          String name,
      @Schema(description = "The 'admin-token' to authorize the operation.", type = "string")
          @RequestParam
          String adminToken);

  @Operation(summary = "Returns a list of all configured zones.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Zones listed successful.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            value =
                                "[{"
                                    + "    \"name\": \"mydomain.net\",\n"
                                    + "    \"ns\": \"a1.nameserver.net\",\n"
                                    + "    \"changed\": \"2024-03-11T09:07:59.022057\"\n"
                                    + "  }]"))),
        @ApiResponse(
            responseCode = "403",
            description = "The 'adminToken' is wrong.",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @GetMapping(value = "/admin/zones", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<List<Zone>> listZones(
      @Schema(description = "The 'admin-token' to authorize the operation.", type = "string")
          @RequestParam
          String adminToken);

  @Operation(summary = "Returns a list of hosts of the desired zone.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The hosts of the desired zones fetched successful.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            value =
                                "[{"
                                    + "    \"name\": \"master\",\n"
                                    + "    \"apiToken\": \"secureApiToken\",\n"
                                    + "    \"zone\": \"mydomain.net\",\n"
                                    + "    \"ns\": \"a1.nameserver.net\",\n"
                                    + "    \"fullHost\": \"master.mydomain.net\",\n"
                                    + "    \"changed\": \"2024-03-11T09:07:59.037688\"\n"
                                    + "  }]"))),
        @ApiResponse(
            responseCode = "403",
            description = "The 'adminToken' is wrong.",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @GetMapping(value = "/admin/zones/{zoneName}/hosts", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<List<FullHost>> listHostsOfZone(
      @Schema(
              description = "The name of the zone for which the hosts are to be listed.",
              type = "string",
              example = "domain.com")
          @PathVariable
          String zoneName,
      @Schema(description = "The 'admin-token' to authorize the operation.", type = "string")
          @RequestParam
          String adminToken);

  @Operation(summary = "Adds a host with the specified host for the desired zone.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The desired host was successfully created."),
        @ApiResponse(responseCode = "403", description = "The 'adminToken' is wrong."),
        @ApiResponse(responseCode = "404", description = "The zone doesn't exists."),
        @ApiResponse(responseCode = "409", description = "The desired host already exists."),
        @ApiResponse(responseCode = "500", description = "If the operation has failed.")
      })
  @PostMapping(value = "/admin/zones/{zoneName}/hosts/{host}")
  ResponseEntity<Void> addHost(
      @Schema(
              description = "The name of the zone for which the host is to be created.",
              type = "string",
              example = "domain.com")
          @PathVariable
          String zoneName,
      @Schema(
              description = "The name of the host to create, it's just the sub-domain part.",
              type = "string",
              example = "host1")
          @PathVariable
          String host,
      @Schema(
              description =
                  "The 'api-token' to authenticate IP changes of the created host. Should be a strong one!",
              type = "string")
          @RequestParam
          String apiToken,
      @Schema(description = "The 'admin-token' to authorize the operation.", type = "string")
          @RequestParam
          String adminToken);

  @Operation(summary = "Deletes the host with the specified host name.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The desired host was successfully deleted."),
        @ApiResponse(responseCode = "403", description = "The 'adminToken' is wrong."),
        @ApiResponse(responseCode = "404", description = "The desired host doesn't exists."),
        @ApiResponse(responseCode = "500", description = "If the operation has failed.")
      })
  @DeleteMapping(value = "/admin/zones/hosts/{host}")
  ResponseEntity<Void> deleteHost(
      @Schema(description = "The full name of the host.", example = "mydyndns.domain.com")
          @PathVariable
          String host,
      @Schema(description = "The 'admin-token' to authorize the operation.", type = "string")
          @RequestParam
          String adminToken);
}
