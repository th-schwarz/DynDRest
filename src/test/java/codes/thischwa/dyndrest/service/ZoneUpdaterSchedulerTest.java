package codes.thischwa.dyndrest.service;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import codes.thischwa.dyndrest.model.HostInfoHolder;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.Provider;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {"dyndrest.update-interval-seconds=1"})
@DisplayName("ZoneUpdaterScheduler - scheduled execution tests")
class ZoneUpdaterSchedulerTest {

  @MockitoBean
  private HostOrderService hostOrderService;

  @MockitoBean
  private Provider provider;

  @BeforeEach
  void setUp() throws Exception {
    HostInfoHolder holder = new HostInfoHolder();
    holder.setSld("test-host");
    holder.setIpSetting(new IpSetting(InetAddress.getByName("192.168.1.1"), null));

    when(hostOrderService.getHostsToUpdate()).thenReturn(List.of(holder));
    when(hostOrderService.getHostsToDelete()).thenReturn(Collections.emptyList());
  }

  @Test
  @DisplayName("Scheduler triggers and calls hostOrderService.getHostsToUpdate()")
  void schedulerTriggersAutomatically() {
    await()
        .atMost(Duration.ofSeconds(1))
        .untilAsserted(() ->
            verify(hostOrderService, atLeastOnce()).getHostsToUpdate()
        );
  }
}
