package codes.thischwa.dyndrest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Reproduces <a href="https://github.com/th-schwarz/DynDRest/issues/79">issue #79</a>: If no
 * dedicated users are configured for '/admin/**' and '/log-ui', these routes must not be
 * accessible by the general user.
 *
 * <p>Only read-only requests are used, because write requests would modify the shared test
 * database as long as the bug exists.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "dyndrest.admin-user-name=",
      "dyndrest.admin-user-password=",
      "dyndrest.update-log-user-name=",
      "dyndrest.update-log-user-password="
    })
@DisplayName("Mock tests: Security without dedicated users")
class SecurityWithoutDedicatedUsersTest {

  private static final String GENERAL_USER = "dyndns";
  private static final String GENERAL_PASSWORD = "test123";

  @Autowired private WebApplicationContext context;

  private MockMvc mockMvc;

  @BeforeEach
  void setupMockMvc() {
    if (mockMvc == null) {
      mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
  }

  @Test
  void testGeneralUser_admin() throws Exception {
    mockMvc
        .perform(get("/admin/zones").with(httpBasic(GENERAL_USER, GENERAL_PASSWORD)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void testGeneralUser_logUi() throws Exception {
    mockMvc
        .perform(get("/log-ui/0").with(httpBasic(GENERAL_USER, GENERAL_PASSWORD)))
        .andExpect(status().is4xxClientError());
  }
}
