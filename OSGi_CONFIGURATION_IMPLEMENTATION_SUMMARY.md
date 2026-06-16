# OSGi Configuration Implementation for PrivateFolderPermissionChangeListener

## Summary

You now have a fully configurable JCR event listener for monitoring permission changes on private folders in AEM. The listener can be enabled/disabled and the monitored root path is now configurable, along with granular control over which event types to listen for.

## Files Created/Modified

### New Files Created:

1. **PrivateFolderPermissionChangeListenerConfig.java**
   - Location: `core/src/main/java/com/cat/csc/core/listeners/`
   - OSGi configuration interface with @ObjectClassDefinition
   - Defines all configurable properties:
     - `enabled` - Enable/disable the listener
     - `monitoredRootPath` - Configurable path (default: `/content/dam`)
     - `monitorPropertyChanges` - Listen for property modifications
     - `monitorPropertyAdditions` - Listen for new properties
     - `monitorPropertyRemovals` - Listen for removed properties
     - `monitorNestedPaths` - Monitor child paths
     - `logLevel` - Logger verbosity

2. **Configuration Files**
   - `core/src/main/content/jcr_root/apps/catcsc/config/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
     - Default/global configuration
   - `core/src/main/content/jcr_root/apps/catcsc/config.author/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
     - Author-specific configuration
   - `core/src/main/content/jcr_root/apps/catcsc/config.publish/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
     - Publish-specific configuration (disabled by default)

3. **Documentation Files**
   - `core/src/main/java/com/cat/csc/core/listeners/PRIVATE_FOLDER_PERMISSION_LISTENER_README.md`
     - Listener implementation documentation
   - `OSGI_CONFIGURATION_GUIDE.md` (Root project directory)
     - Complete OSGi configuration guide with examples
   - `com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.config.json`
     - JSON reference for configuration properties

### Modified Files:

1. **PrivateFolderPermissionChangeListener.java**
   - Updated to accept OSGi configuration
   - Added `@Designate(ocd = PrivateFolderPermissionChangeListenerConfig.class)` annotation
   - Modified `activate()` method to accept configuration parameter
   - Added `buildEventTypeMask()` method to dynamically build event types based on config
   - Modified `deactivate()` method to handle disabled state
   - Now uses `monitoredPath` from configuration instead of hardcoded `/content/dam`
   - Respects the `enabled` flag - logs and returns early if disabled

## Configuration Properties

### enabled (Boolean)
- **Default:** `true`
- **Description:** Enable or disable the listener
- **Impact:** When false, listener is not activated, no events monitored
- **XML:** `<enabled>{Boolean}true</enabled>`

### monitoredRootPath (String)
- **Default:** `/content/dam`
- **Description:** Root path to monitor for permission changes
- **Examples:**
  - `/content/dam` - Monitor all DAM
  - `/content/dam/private` - Monitor private folder only
  - `/content` - Monitor all content
- **XML:** `<monitoredRootPath>/content/dam</monitoredRootPath>`

### monitorPropertyChanges (Boolean)
- **Default:** `true`
- **Description:** Monitor when existing permission properties are modified
- **XML:** `<monitorPropertyChanges>{Boolean}true</monitorPropertyChanges>`

### monitorPropertyAdditions (Boolean)
- **Default:** `true`
- **Description:** Monitor when new permission properties are added
- **XML:** `<monitorPropertyAdditions>{Boolean}true</monitorPropertyAdditions>`

### monitorPropertyRemovals (Boolean)
- **Default:** `true`
- **Description:** Monitor when permission properties are removed
- **XML:** `<monitorPropertyRemovals>{Boolean}true</monitorPropertyRemovals>`

### monitorNestedPaths (Boolean)
- **Default:** `true`
- **Description:** Monitor nested child paths under the root path
- **When false:** Only root path changes are monitored
- **XML:** `<monitorNestedPaths>{Boolean}true</monitorNestedPaths>`

### logLevel (String)
- **Default:** `INFO`
- **Valid values:** `DEBUG`, `INFO`, `WARN`, `ERROR`
- **XML:** `<logLevel>INFO</logLevel>`

## Configuration Examples

### Example 1: Monitor All DAM with Default Settings

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          enabled="{Boolean}true"
          monitoredRootPath="/content/dam"
          monitorPropertyChanges="{Boolean}true"
          monitorPropertyAdditions="{Boolean}true"
          monitorPropertyRemovals="{Boolean}true"
          monitorNestedPaths="{Boolean}true"
          logLevel="INFO"/>
```

### Example 2: Monitor Private Folder Only

```xml
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          enabled="{Boolean}true"
          monitoredRootPath="/content/dam/private"
          monitorPropertyChanges="{Boolean}true"
          monitorPropertyAdditions="{Boolean}true"
          monitorPropertyRemovals="{Boolean}true"
          monitorNestedPaths="{Boolean}true"
          logLevel="INFO"/>
```

### Example 3: Disable on Publish

```xml
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          enabled="{Boolean}false"
          monitoredRootPath="/content/dam"
          logLevel="INFO"/>
```

### Example 4: Monitor Only Property Changes

```xml
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          enabled="{Boolean}true"
          monitoredRootPath="/content/dam"
          monitorPropertyChanges="{Boolean}true"
          monitorPropertyAdditions="{Boolean}false"
          monitorPropertyRemovals="{Boolean}false"
          monitorNestedPaths="{Boolean}true"
          logLevel="INFO"/>
```

### Example 5: Debug Mode

```xml
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          enabled="{Boolean}true"
          monitoredRootPath="/content/dam"
          monitorPropertyChanges="{Boolean}true"
          monitorPropertyAdditions="{Boolean}true"
          monitorPropertyRemovals="{Boolean}true"
          monitorNestedPaths="{Boolean}true"
          logLevel="DEBUG"/>
```

## How to Deploy

### Option 1: Package Configuration with Code (Recommended)

Configuration files are already included in:
```
core/src/main/content/jcr_root/apps/catcsc/config/
└── com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml
```

Just build and deploy:
```bash
mvn clean install -PautoInstallSinglePackage
```

### Option 2: Update Configuration After Deployment

1. Go to **Tools → Deployment → Configuration**
2. Search for: `PrivateFolderPermissionChangeListener`
3. Edit properties in the UI
4. Save

### Option 3: Use AEM Web Console

1. Go to **System → OSGi Console → Configuration**
2. Search for: `com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener`
3. Click to edit
4. Update properties
5. Save

## Verifying Configuration

### Check Configuration via Console

1. **OSGi Console** → **Components**
2. Search for: `PrivateFolderPermissionChangeListener`
3. Status should be **Active** or **Satisfied**

### Check Logs

```bash
tail -f crx-quickstart/logs/error.log | grep PrivateFolderPermissionChangeListener
```

**When enabled:**
```
INFO  PrivateFolderPermissionChangeListener activated. Listening for permission changes under path: /content/dam, deep: true, event types: [PROPERTY_CHANGED: true, PROPERTY_ADDED: true, PROPERTY_REMOVED: true]
```

**When disabled:**
```
INFO  PrivateFolderPermissionChangeListener is disabled via configuration
```

## Testing the Configuration

### Build the Project

```bash
cd /Users/feng.xiao/Source/CAT/catcsc
mvn clean install
```

### Run Unit Tests

```bash
mvn clean test -Dtest=PrivateFolderPermissionChangeListenerTest
```

## Benefits of This Implementation

✅ **Enable/Disable without Code Changes:** Toggle listener via configuration
✅ **Flexible Path Monitoring:** Monitor any path, not just `/content/dam`
✅ **Granular Event Control:** Choose which event types to listen for
✅ **Production vs Dev:** Different configs for author/publish
✅ **Performance Tuning:** Disable unnecessary events to reduce overhead
✅ **Easy Debugging:** Enable debug logging via configuration
✅ **Zero Downtime:** Configuration changes take effect without restart
✅ **Standard OSGi:** Uses standard OSGi metatype annotations
✅ **Well Documented:** Complete configuration guide included

## Key Implementation Details

### Configuration Interface Pattern
- Uses `@ObjectClassDefinition` for self-documenting configuration
- Provides sensible defaults
- IDE shows configuration options with descriptions

### Activate Method
```java
@Activate
protected void activate(final PrivateFolderPermissionChangeListenerConfig configuration) {
    this.config = configuration;
    
    // Check if enabled
    if (!config.enabled()) {
        LOGGER.info("PrivateFolderPermissionChangeListener is disabled via configuration");
        return;
    }
    
    // Use configuration values
    this.monitoredPath = config.monitoredRootPath();
    // ... rest of activation
}
```

### Dynamic Event Type Building
```java
private int buildEventTypeMask() {
    int eventTypes = 0;
    
    if (config.monitorPropertyChanges()) {
        eventTypes |= Event.PROPERTY_CHANGED;
    }
    if (config.monitorPropertyAdditions()) {
        eventTypes |= Event.PROPERTY_ADDED;
    }
    if (config.monitorPropertyRemovals()) {
        eventTypes |= Event.PROPERTY_REMOVED;
    }
    
    return eventTypes;
}
```

## Backward Compatibility

- The default path is still `/content/dam`
- All event types are monitored by default
- Existing behavior is preserved with default configuration
- No breaking changes to the listener interface

## Next Steps

1. **Review Configuration:** Check the configuration files in the config folders
2. **Customize Path:** Update `monitoredRootPath` if needed
3. **Deploy:** Build and deploy with `mvn clean install -PautoInstallSinglePackage`
4. **Verify:** Check OSGi console and logs
5. **Test:** Run unit tests to verify functionality
6. **Add Business Logic:** Implement `handlePermissionChange()` method with your custom logic

## Support Files

- **OSGI_CONFIGURATION_GUIDE.md** - Detailed configuration guide
- **PRIVATE_FOLDER_PERMISSION_LISTENER_README.md** - Implementation details
- **PrivateFolderPermissionChangeListenerTest.java** - Unit tests
- **PrivateFolderPermissionChangeListenerConfig.java** - Configuration interface
- **PrivateFolderPermissionChangeListener.java** - Updated listener implementation

## Questions?

Refer to:
1. OSGI_CONFIGURATION_GUIDE.md for configuration details
2. PRIVATE_FOLDER_PERMISSION_LISTENER_README.md for implementation details
3. PrivateFolderPermissionChangeListenerConfig.java for available properties
4. OSGi web console for current configuration status

