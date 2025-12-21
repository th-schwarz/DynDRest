package codes.thischwa.dyndrest.provider.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.model.config.AppConfig;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.UpdateHookException;
import codes.thischwa.dyndrest.server.config.DynamicSecurityChainManager;
import codes.thischwa.dyndrest.service.HostZoneService;
import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenericProviderTest {

  @Mock private AppConfig appConfig;

  @Mock private DynamicSecurityChainManager securityChainManager;

  @Mock private HostZoneService hostZoneService;

  @Test
  void testProcessUpdateCallsHooks() throws ProviderException, UnknownHostException, UpdateHookException {
    // Arrange
    TestProvider provider = spy(new TestProvider(appConfig, securityChainManager, hostZoneService));
    String host = "test.example.com";
    IpSetting ipSetting = new IpSetting("192.168.1.1");

    // Act
    provider.processUpdate(host, ipSetting);

    // Assert
    InOrder inOrder = inOrder(provider);
    inOrder.verify(provider).updateBeforeHook(host, ipSetting);
    inOrder.verify(provider).update(host, ipSetting);
    inOrder.verify(provider).updateAfterHook(host, ipSetting);
  }

  @Test
  void testProcessUpdateCallsUpdateEvenWhenBeforeHookThrows()
      throws ProviderException, UpdateHookException, UnknownHostException {
    // Arrange
    TestProvider provider = spy(new TestProvider(appConfig, securityChainManager, hostZoneService));
    String host = "test.example.com";
    IpSetting ipSetting = new IpSetting("192.168.1.1");

    doThrow(new UpdateHookException("Before hook failed"))
        .when(provider)
        .updateBeforeHook(host, ipSetting);

    // Act
    provider.processUpdate(host, ipSetting);

    // Assert
    InOrder inOrder = inOrder(provider);
    inOrder.verify(provider).updateBeforeHook(host, ipSetting);
    inOrder.verify(provider).update(host, ipSetting);
    inOrder.verify(provider).updateAfterHook(host, ipSetting);
  }

  @Test
  void testProcessUpdateCallsAfterHookEvenWhenItThrows()
      throws ProviderException, UpdateHookException, UnknownHostException {
    // Arrange
    TestProvider provider = spy(new TestProvider(appConfig, securityChainManager, hostZoneService));
    String host = "test.example.com";
    IpSetting ipSetting = new IpSetting("192.168.1.1");

    doThrow(new UpdateHookException("After hook failed"))
        .when(provider)
        .updateAfterHook(host, ipSetting);

    // Act
    provider.processUpdate(host, ipSetting);

    // Assert
    InOrder inOrder = inOrder(provider);
    inOrder.verify(provider).updateBeforeHook(host, ipSetting);
    inOrder.verify(provider).update(host, ipSetting);
    inOrder.verify(provider).updateAfterHook(host, ipSetting);
  }

  @Test
  void testProcessUpdatePropagatesUpdateException()
      throws ProviderException, UnknownHostException, UpdateHookException {
    // Arrange
    TestProvider provider = spy(new TestProvider(appConfig, securityChainManager, hostZoneService));
    String host = "test.example.com";
    IpSetting ipSetting = new IpSetting("192.168.1.1");

    doThrow(new ProviderException("Update failed")).when(provider).update(host, ipSetting);

    // Act & Assert
    assertThrows(ProviderException.class, () -> provider.processUpdate(host, ipSetting));

    verify(provider).updateBeforeHook(host, ipSetting);
    verify(provider).update(host, ipSetting);
    verify(provider, never()).updateAfterHook(host, ipSetting);
  }

  /** Test implementation of GenericProvider for testing purposes. */
  private static class TestProvider extends GenericProvider {

    protected TestProvider(
        AppConfig appConfig,
        DynamicSecurityChainManager securityChainManager,
        HostZoneService hostZoneService) {
      super(appConfig, securityChainManager, hostZoneService);
    }

    @Override
    public void validateHostZoneConfiguration() throws IllegalArgumentException {
      // Test implementation
    }

    @Override
    public void update(String host, IpSetting ipSetting) throws ProviderException {
      // Test implementation
    }

    @Override
    public void updateBeforeHook(String host, IpSetting ipSetting) throws UpdateHookException {
    }

    @Override
    public void updateAfterHook(String host, IpSetting ipSetting) throws UpdateHookException {
    }

    @Override
    public void addHost(String zoneName, String host) throws ProviderException {
      // Test implementation
    }

    @Override
    public void removeHostIpSettings(String host) throws ProviderException {
      // Test implementation
    }
  }
}
