package org.mule.test.util;

import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.extension.ExtensionManager;
import org.mule.runtime.extension.api.runtime.config.ConfigurationInstance;

public class ExtensionsTestUtils {

    public static ConfigurationInstance getConfigurationInstanceFromRegistry(String key, CoreEvent muleEvent,
                                                                             MuleContext muleContext) {
        ExtensionManager extensionManager = muleContext.getExtensionManager();
        return extensionManager.getConfiguration(key, muleEvent);
    }
}
