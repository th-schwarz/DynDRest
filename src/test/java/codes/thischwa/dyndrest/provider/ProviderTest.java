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

    IpSetting ips = provider.info("mein-mail-server.de");
    assertEquals(
        "IpSetting(ipv4=mein-mail-server.de./37.120.183.249, ipv6=mein-mail-server.de./2a03:4000:8:750:0:0:0:2)",
        ips.toString());
  }

  private static class TestProvider extends GenericProvider {

    @Override
    public void validateHostZoneConfiguration() throws IllegalArgumentException {}

    @Override
    public void update(String host, IpSetting ipSetting) throws ProviderException {}

    @Override
    public void addHost(String zaneName, String host) throws ProviderException {}

    @Override
    public void removeHost(String host) throws ProviderException {}
  }
}
