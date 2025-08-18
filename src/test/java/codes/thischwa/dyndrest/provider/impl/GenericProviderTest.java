package codes.thischwa.dyndrest.provider.impl;

import codes.thischwa.dyndrest.model.IpSetting;
import codes.thischwa.dyndrest.provider.ProviderException;
import codes.thischwa.dyndrest.provider.UpdateHookException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenericProviderTest {

    private static class RecordingProvider extends GenericProvider {
        boolean beforeCalled = false;
        boolean updateCalled = false;
        boolean afterCalled = false;
        boolean throwBefore = false;
        boolean throwAfter = false;

        @Override
        public void validateHostZoneConfiguration() throws IllegalArgumentException {
            // not relevant for these tests
        }

        @Override
        public void updateBeforeHook(String host, IpSetting ipSetting) throws UpdateHookException {
            beforeCalled = true;
            if (throwBefore) {
                throw new UpdateHookException("before failed");
            }
        }

        @Override
        public void update(String host, IpSetting ipSetting) throws ProviderException {
            updateCalled = true;
        }

        @Override
        public void updateAfterHook(String host, IpSetting ipSetting) throws UpdateHookException {
            afterCalled = true;
            if (throwAfter) {
                throw new UpdateHookException("after failed");
            }
        }

        @Override
        public void addHost(String zoneName, String host) throws ProviderException { /* noop */ }

        @Override
        public void removeHostIpSettings(String host) throws ProviderException { /* noop */ }
    }

    @Test
    void processUpdate_callsHooksAndUpdate_inOrderAndSuccess() throws ProviderException {
        RecordingProvider provider = new RecordingProvider();
        IpSetting ips = new IpSetting();

        provider.processUpdate("example.com", ips);

        assertTrue(provider.beforeCalled, "before hook should be called");
        assertTrue(provider.updateCalled, "update should be called");
        assertTrue(provider.afterCalled, "after hook should be called");
    }

    @Test
    void processUpdate_swallowsBeforeHookException_andStillUpdatesAndCallsAfter() throws ProviderException {
        RecordingProvider provider = new RecordingProvider();
        provider.throwBefore = true;
        IpSetting ips = new IpSetting();

        provider.processUpdate("example.com", ips);

        assertTrue(provider.beforeCalled, "before hook should be attempted");
        assertTrue(provider.updateCalled, "update should be called even if before fails");
        assertTrue(provider.afterCalled, "after hook should still be called");
    }

    @Test
    void processUpdate_swallowsAfterHookException_updateStillRan() throws ProviderException {
        RecordingProvider provider = new RecordingProvider();
        provider.throwAfter = true;
        IpSetting ips = new IpSetting();

        provider.processUpdate("example.com", ips);

        assertTrue(provider.beforeCalled, "before hook should be called");
        assertTrue(provider.updateCalled, "update should be called");
        assertTrue(provider.afterCalled, "after hook should be attempted");
    }

    @Test
    void info_wrapsIOException() {
        RecordingProvider provider = new RecordingProvider();
        assertThrows(IllegalArgumentException.class, () -> provider.info("invalid/hostname"));
    }
}
