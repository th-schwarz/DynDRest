package codes.thischwa.dyndrest.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import codes.thischwa.dyndrest.AbstractIntegrationTest;
import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.impl.GenericProvider;
import org.junit.jupiter.api.Test;

class ProviderTest extends AbstractIntegrationTest {

  @Test
  final void test() throws ProviderException {
    TestProvider provider = new TestProvider();

    IpSetting ips = provider.info("mein-email-fach.de");
    assertEquals(
        "IpSetting(ipv4=mein-email-fach.de./37.120.183.96, ipv6=mein-email-fach.de./2a03:4000:4d:e8f:0:0:0:2)",
        ips.toString());
  }

  static class TestProvider extends GenericProvider {

    @Override
    public void validateHostConfiguration() throws IllegalArgumentException {}

    @Override
    public void update(String host, IpSetting ipSetting) throws ProviderException {}

  }
}
