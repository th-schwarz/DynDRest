package codes.thischwa.dyndrest;

import codes.thischwa.dyndrest.util.NetUtil;
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Integration & server tests")
class IntegrationTest extends GenericIntegrationTest {

	@Autowired private MockMvc mockMvc;

	@Test
	final void testNoAuth_Greeting() throws Exception {
		mockMvc.perform(get("/"))
				//.andDo(print())
				.andExpect(status().isOk()).andExpect(content().contentType(new MediaType("text", "html", StandardCharsets.UTF_8)))
				.andExpect(content().string(containsString("DynDRest :: Default landing page")));
	}

	@Test
	final void testBasicAuth_log() throws Exception {
		mockMvc.perform(get("/log").with(httpBasic("log-dev", "l0g-dev")))
				//			.andDo(print())
				.andExpect(status().isOk()).andExpect(content().contentType(new MediaType("text", "html", StandardCharsets.UTF_8)))
				.andExpect(content().string(containsString("DynDRest :: Log View")));
	}

  @Test
  final void testBasicAuth_health() throws Exception {
    mockMvc
        .perform(get("/manage/health").with(httpBasic("health", "hea1th")))
        //			.andDo(print())
        .andExpect(status().isOk())
        .andExpect(
            content()
                .contentType(
                    new MediaType(
                        "application", "vnd.spring-boot.actuator.v3+json")))
        .andExpect(content().string(is("{\"status\":\"UP\"}")));
  }

  @Test
  final void testBasicUnAuth_global() throws Exception {
    mockMvc
        .perform(get("/info/test.mein-virtuelles-blech.de"))
        .andExpect(status().isUnauthorized());
  }

	@Test
	final void testBasicAuth_global() throws Exception {
		mockMvc.perform(get("/info/update-log").queryParam("page", "0").with(httpBasic("dyndns", "test123")))
				//			.andDo(print())
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().string(startsWith("{\"total\":38,\"totalPage\":10,\"page\":1,\"pageSize\":4")));
	}

	@Test
	final void testBaseUrl() {
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
	final void testFavicon() throws Exception {
		mockMvc.perform(get("/favicon.ico")).andExpect(status().isOk()).andExpect(content().string(""));
	}
}
