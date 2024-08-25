package codes.thischwa.dyndrest;


import codes.thischwa.dyndrest.util.NetUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Integration tests: Rest - other")
public class RestTestOther extends AbstractIntegrationTest{

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void init() {
        initUpdateLogDatabase();
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
                .andExpect(content().string(is("{\"status\":\"UP\"}")));
    }

    @Test
    void testBasicUnAuth_global() throws Exception {
        mockMvc
                .perform(get("/info/test.mein-virtuelles-blech.de"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testBaseUrl() {
        // feed the mock
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/");
        mockRequest.setServerPort(port);
        ServletRequestAttributes attrs = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attrs);

        assertEquals("http://localhost:" + port, NetUtil.getBaseUrl(false));
        assertEquals("https://localhost:" + port, NetUtil.getBaseUrl(true));
    }

    @Test
    void testFavicon() throws Exception {
        mockMvc.perform(get("/favicon.ico")).andExpect(status().isOk()).andExpect(content().string(""));
    }

}
