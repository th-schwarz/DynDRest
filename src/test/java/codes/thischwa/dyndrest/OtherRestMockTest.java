package codes.thischwa.dyndrest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Mock tests: Rest - other")
class OtherRestMockTest {

    @Autowired private WebApplicationContext context;

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
    void testNoAuth_Greeting() throws Exception {
        mockMvc
                .perform(get("/"))
                // .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(new MediaType("text", "html", StandardCharsets.UTF_8)))
                .andExpect(content().string(containsString("DynDRest :: Default landing page")));
    }

    @Test
    void testLogRedirect() throws Exception {
        mockMvc
                .perform(get("/log-ui").with(httpBasic("log-dev", "l0g-dev")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/log-ui/0"));
    }

    @Test
    void testBasicAuth_log() throws Exception {
        mockMvc
                .perform(get("/log-ui/0").with(httpBasic("log-dev", "l0g-dev")))
                //			.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(new MediaType("text", "html", StandardCharsets.UTF_8)))
                .andExpect(content().string(containsString("DynDRest :: Log View")));
    }

    @Test
    void testBasicAuth_health() throws Exception {
        mockMvc
                .perform(get("/manage/health").with(httpBasic("health", "hea1th")))
                //			.andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        content().contentType(new MediaType("application", "vnd.spring-boot.actuator.v3+json")))
                .andExpect(content().string(is("{\"groups\":[\"liveness\",\"readiness\"],\"status\":\"UP\"}")));
    }

    @Test
    void testBasicUnAuth_global() throws Exception {
        mockMvc
                .perform(get("/info/test.mein-virtuelles-blech.de"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testFavicon() throws Exception {
        mockMvc.perform(get("/favicon.ico")).andExpect(status().isOk()).andExpect(content().string(""));
    }

}
