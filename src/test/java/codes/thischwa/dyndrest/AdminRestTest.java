package codes.thischwa.dyndrest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.HostEnriched;
import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.provider.Provider;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.service.HostZoneService;
import codes.thischwa.dyndrest.service.ZoneUpdaterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@DisplayName("Integration tests: Rest - '/admin/'")
class AdminRestTest extends AbstractIntegrationTest {

  @Autowired
  private WebApplicationContext context;
  @Autowired
  private HostZoneService hostZoneService;
  @Autowired
  private ZoneUpdaterService zoneUpdaterService;
  @MockitoBean
  private Provider provider;

  private MockMvc mockMvc;

  @BeforeEach
  void setupMockMvc() {
    if (mockMvc == null) {
      mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
    }
  }

  @Test
  void testAddZone() throws Exception {
    mockMvc
      .perform(
        post("/admin/zones/my.info/ns.my.info")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isOk());
    Zone z = hostZoneService.getZone("my.info");
    assertNotNull(z);

    // test duplicate
    mockMvc
      .perform(
        post("/admin/zones/my.info/ns.my.info")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isConflict());

    // test malformed
    mockMvc
      .perform(
        post("/admin/zones/my.info/ns,my.info")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isBadRequest());
  }

  @Test
  void testDeleteZone() throws Exception {
    HostEnriched host = hostZoneService.findHostsOfZone("dynhost1.info").orElseThrow().get(0);
    zoneUpdaterService.addCurrentHosts(HostInfoHolder.of(host, new IpSetting("203.0.113.1")));
    zoneUpdaterService.addOrUpdateHost(HostInfoHolder.of(host, new IpSetting("203.0.113.2")));

    mockMvc
      .perform(
        delete("/admin/zones/dynhost1.info")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isOk());
    Zone z = hostZoneService.getZone("dynhost1.info");
    assertNull(z);
    assertFalse(zoneUpdaterService.hostExists("my1.dynhost1.info"));
    assertTrue(zoneUpdaterService.getHostsToUpdate("dynhost1.info").isEmpty());
    verify(provider).removeHostIpSettings("my1.dynhost1.info");

    // test unknown
    mockMvc
      .perform(
        delete("/admin/zones/unknown.info")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isBadRequest());
  }

  @Test
  void testDeleteZoneProviderErrorKeepsZoneAndHost() throws Exception {
    doThrow(new ProviderException("provider error"))
        .when(provider).removeHostIpSettings("my1.dynhost1.info");

    mockMvc
      .perform(
        delete("/admin/zones/dynhost1.info")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isInternalServerError());

    assertNotNull(hostZoneService.getZone("dynhost1.info"));
    assertTrue(hostZoneService.getHost("my1.dynhost1.info").isPresent());
  }

  @Test
  void testDeleteAndRecreateZoneMakesHostInitialAssignment() throws Exception {
    mockMvc
      .perform(
        delete("/admin/zones/dynhost1.info")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isOk());
    mockMvc
      .perform(
        post("/admin/zones/dynhost1.info/ns1.domain.info")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isOk());
    mockMvc
      .perform(
        post("/admin/zones/dynhost1.info/hosts/my1")
          .param("apiToken", "1234567890abcdef")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isOk());

    assertTrue(zoneUpdaterService.isInitialAssignment("my1.dynhost1.info"));
  }

  @Test
  void testListZones() throws Exception {
    mockMvc
      .perform(get("/admin/zones")
        .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[0].name").value("dynhost0.info"))
      .andExpect(jsonPath("$[0].ns").value("ns0.domain.info"))
      .andExpect(jsonPath("$[1].name").value("dynhost1.info"))
      .andExpect(jsonPath("$[1].ns").value("ns1.domain.info"));

    // wrong basic-auth
    mockMvc
      .perform(
        get("/admin/zones")
          .with(httpBasic("admin", "wrongPassword")))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void testListHostsOfZone() throws Exception {
    mockMvc
      .perform(
        get("/admin/zones/dynhost0.info/hosts")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      //.andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[0].fullHost").value("my0.dynhost0.info"))
      .andExpect(jsonPath("$[0].apiToken").value("1234567890abcdef"))
      .andExpect(jsonPath("$[1].fullHost").value("test0.dynhost0.info"))
      .andExpect(jsonPath("$[1].apiToken").value("1234567890abcdex"));

    // test wrong basic-auth
    mockMvc
      .perform(
        get("/admin/zones/dynhost0.info/hosts")
          .with(httpBasic("admin", "wrongPassword")))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void testAddHost() throws Exception {
    mockMvc
      .perform(
        post("/admin/zones/dynhost0.info/hosts/mynew")
          .param("apiToken", "token123")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isOk());
    Host h = hostZoneService.getHost("mynew.dynhost0.info").orElse(null);
    assertNotNull(h);

    // duplicate
    mockMvc
      .perform(
        post("/admin/zones/dynhost0.info/hosts/mynew")
          .param("apiToken", "token123")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isConflict());

    // missing apiToken
    mockMvc
      .perform(
        post("/admin/zones/dynhost0.info/hosts/mynew1.dynhost0.info")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isBadRequest());

    // malformed
    mockMvc
      .perform(
        post("/admin/zones/dynhost0.info/hosts/my,dynhost0.info")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isBadRequest());

    mockMvc
      .perform(
        post("/admin/zones/dynhost0.info/hosts/mynew.subdomain")
          .param("apiToken", "token456")
          .with(httpBasic("admin", "adm1n")))
      .andExpect(status().isBadRequest());
    assertNull(hostZoneService.getHost("mynew.subdomain.dynhost0.info").orElse(null));
  }
}
