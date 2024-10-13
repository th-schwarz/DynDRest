package codes.thischwa.dyndrest;

import codes.thischwa.dyndrest.model.Host;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.UpdateLog;
import codes.thischwa.dyndrest.model.Zone;
import codes.thischwa.dyndrest.repository.UpdateLogRepo;
import codes.thischwa.dyndrest.service.HostZoneService;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
// causes the re-initialization of the database for each test class.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public abstract class AbstractIntegrationTest {
  protected static final LocalDateTime START_DATETIME = LocalDateTime.of(2023, 11, 30, 13, 30);

  @Value("${local.server.port}")
  protected int port;

  @Autowired protected TestRestTemplate restTemplate;
  protected LocalDate currentDate;
  protected Integer z1ID;
  protected Integer z2ID;
  protected Integer h1z1ID;
  protected Integer h2z1ID;
  @Autowired private HostZoneService hostZoneService;
  @Autowired private UpdateLogRepo updateLogRepo;

  @PostConstruct
  void initUpdateLogDatabase() {
    try {
      if (!updateLogRepo.findAll().isEmpty()) {
        return;
      }
      // generate test data
      Zone z1 = hostZoneService.addZone("dynhost0.info", "ns0.domain.info");
      currentDate = z1.getChanged().toLocalDate();
      z1ID = z1.getId();
      Zone z2 = hostZoneService.addZone("dynhost1.info", "ns1.domain.info");
      z2ID = z2.getId();
      Host h1z1 = hostZoneService.addHost(z1, "my0", "1234567890abcdef");
      h1z1ID = h1z1.getId();
      Host h2z1 = hostZoneService.addHost(z1, "test0", "1234567890abcdex");
      h2z1ID = h2z1.getId();
      hostZoneService.addHost(z2, "my1", "1234567890abcdef");
      hostZoneService.addHost(z1, "test1", "1234567890abcdex");
      LocalDateTime dateTime = START_DATETIME;
      updateLogRepo.save(
          UpdateLog.getInstance(
              h1z1ID,
              new IpSetting("198.0.1.0", "2a03:4000:41:32:0:0:1:0"),
              UpdateLog.Status.failed,
              null,
              dateTime));
      for (int i = 1; i <= 20; i++) {
        dateTime = dateTime.plusMinutes(10);
        updateLogRepo.save(
            UpdateLog.getInstance(
                h1z1ID,
                new IpSetting("198.0.1." + i),
                UpdateLog.Status.success,
                dateTime,
                dateTime));
      }
      dateTime = dateTime.plusMinutes(10);
      updateLogRepo.save(
          UpdateLog.getInstance(
              h2z1ID,
              new IpSetting("198.0.2.0", "2a03:4000:41:32:0:0:2:0"),
              UpdateLog.Status.failed,
              null,
              dateTime));
      for (int i = 1; i <= 20; i++) {
        dateTime = dateTime.plusMinutes(10);
        updateLogRepo.save(
            UpdateLog.getInstance(
                h2z1ID,
                new IpSetting("198.0.2." + i),
                UpdateLog.Status.success,
                dateTime,
                dateTime));
      }
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  String getBaseUrl() {
    return "http://localhost:" + port + "/";
  }

  URI getBaseUri() throws URISyntaxException {
    return new URI(getBaseUrl());
  }
}
