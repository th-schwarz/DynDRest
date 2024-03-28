package codes.thischwa.dyndrest.model;

import static org.junit.jupiter.api.Assertions.*;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UpdateLogTest {

    @Test
    void testEquals() throws UnknownHostException {
        IpSetting setting = new IpSetting("192.168.1.1");
        LocalDateTime now = LocalDateTime.now();
        UpdateLog l1 = UpdateLog.getInstance(1, setting, UpdateLog.Status.failed, null, now);
        UpdateLog l2 = UpdateLog.getInstance(1, setting, UpdateLog.Status.failed, null, now);
        assertEquals(l1, l2);

        l2.setStatus(UpdateLog.Status.success);
        assertNotEquals(l1, l2);
    }
}
