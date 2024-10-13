package codes.thischwa.dyndrest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.service.HostZoneService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@DisplayName("Integration tests: Rest - '/admin/'")
class AdminRestTest extends AbstractIntegrationTest {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired private MockMvc mockMvc;

  @Autowired private HostZoneService hostZoneService;

  @BeforeAll
  void init() {
    initUpdateLogDatabase();
  }

  @Test
  void testAddZone() throws Exception {
    mockMvc
        .perform(
            post("/admin/zones/my.info/ns.my.info")
                .param("adminToken", "token123")
                .with(httpBasic("admin", "adm1n")))
        .andExpect(status().isOk());
    Zone z = hostZoneService.getZone("my.info");
    assertNotNull(z);

    // test forbidden
    mockMvc
            .perform(
                    post("/admin/zones/my1.info/ns.my.info")
                            .param("adminToken", "token123_")
                            .with(httpBasic("admin", "adm1n")))
            .andExpect(status().isUnauthorized());

    // test duplicate
    mockMvc
        .perform(
            post("/admin/zones/my.info/ns.my.info")
                .param("adminToken", "token123")
                .with(httpBasic("admin", "adm1n")))
        .andExpect(status().isConflict());

    // test malformed
    mockMvc
        .perform(
            post("/admin/zones/my.info/ns,my.info")
                .param("adminToken", "token123")
                .with(httpBasic("admin", "adm1n")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testDeleteZone() throws Exception {
    mockMvc
        .perform(
            delete("/admin/zones/dynhost1.info")
                .param("adminToken", "token123")
                .with(httpBasic("admin", "adm1n")))
        .andExpect(status().isOk());
    Zone z = hostZoneService.getZone("dynhost1.info");
    assertNull(z);

    // test unknown
    mockMvc
        .perform(
            delete("/admin/zones/unknown.info")
                .param("adminToken", "token123")
                .with(httpBasic("admin", "adm1n")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testListZones() throws Exception {
    mockMvc
          .perform(get("/admin/zones")
                  .param("adminToken", "token123")
                  .with(httpBasic("admin", "adm1n")))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[0].name").value("dynhost0.info"))
          .andExpect(jsonPath("$[0].ns").value("ns0.domain.info"))
          .andExpect(jsonPath("$[1].name").value("dynhost1.info"))
          .andExpect(jsonPath("$[1].ns").value("ns1.domain.info"));


    // wrong admin token
    mockMvc
          .perform(
              get("/admin/zones")
                  .param("adminToken", "wrongToken")
                  .with(httpBasic("admin", "adm1n")))
          .andExpect(status().isForbidden());

    // wrong basic-auth
    mockMvc
          .perform(
              get("/admin/zones")
                  .param("adminToken", "token123")
                  .with(httpBasic("admin", "wrongPassword")))
          .andExpect(status().isUnauthorized());
  }

  @Test
  void testListHostsOfZone() throws Exception {
    mockMvc
          .perform(
              get("/admin/zones/dynhost0.info/hosts")
                  .param("adminToken", "token123")
                  .with(httpBasic("admin", "adm1n")))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          //.andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].fullHost").value("my0.dynhost0.info"))
          .andExpect(jsonPath("$[0].apiToken").value("1234567890abcdef"))
          .andExpect(jsonPath("$[1].fullHost").value("test0.dynhost0.info"))
          .andExpect(jsonPath("$[1].apiToken").value("1234567890abcdex"));

  // test wrong adminToken
  mockMvc
          .perform(
              get("/admin/zones/dynhost0.info/hosts")
                  .param("adminToken", "wrongToken")
                  .with(httpBasic("admin", "adm1n")))
          .andExpect(status().isForbidden());

  // test wrong basic-auth
  mockMvc
          .perform(
              get("/admin/zones/dynhost0.info/hosts")
                  .param("adminToken", "token123")
                  .with(httpBasic("admin", "wrongPassword")))
          .andExpect(status().isUnauthorized());
  }

  @Test
  void testAddHost() throws Exception {
    mockMvc
            .perform(
                    post("/admin/zones/dynhost0.info/hosts/mynew")
                            .param("adminToken", "token123")
                            .param("apiToken", "token123")
                            .with(httpBasic("admin", "adm1n")))
            .andExpect(status().isOk());
    Host h = hostZoneService.getHost("mynew.dynhost0.info").orElse(null);
    assertNotNull(h);

    // duplicate
    mockMvc
            .perform(
                    post("/admin/zones/dynhost0.info/hosts/mynew")
                            .param("adminToken", "token123")
                            .param("apiToken", "token123")
                            .with(httpBasic("admin", "adm1n")))
            .andExpect(status().isConflict());

    // missing apiToken
    mockMvc
            .perform(
                    post("/admin/zones/dynhost0.info/hosts/mynew1.dynhost0.info")
                            .param("adminToken", "token123_")
                            .with(httpBasic("admin", "adm1n")))
            .andExpect(status().isBadRequest());

    // malformed
    mockMvc
            .perform(
                    post("/admin/zones/dynhost0.info/hosts/my,dynhost0.info")
                            .param("adminToken", "token123")
                            .with(httpBasic("admin", "adm1n")))
            .andExpect(status().isBadRequest());
  }
}
