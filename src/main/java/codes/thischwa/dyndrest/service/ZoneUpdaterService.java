package codes.thischwa.dyndrest.service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@Profile("!test")
@Slf4j
@ConditionalOnProperty(name = "dyndrest.zone-updater-delay")
public class ZoneUpdaterService {

    @Scheduled(fixedDelayString = "${dyndrest.zone-updater-delay}", timeUnit = TimeUnit.SECONDS)
    void process() {
        log.info("*** process: {}", LocalDateTime.now());
    }
}
