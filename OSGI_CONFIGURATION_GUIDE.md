# OSGi Configuration Guide for PrivateFolderPermissionChangeListener

## Overview

The `PrivateFolderPermissionChangeListener` is now fully configurable via OSGi configuration. This allows administrators to:

1. **Enable/Disable** the listener without code changes
2. **Configure the monitored path** (instead of being hardcoded to `/content/dam`)
3. **Select which event types** to monitor (property changes, additions, removals)
4. **Control whether to monitor nested paths**
5. **Set logging levels** for debugging

## Configuration Files

### File Locations

OSGi configurations can be placed in different locations to apply to different instances:

```
/apps/catcsc/config/                              # Applied to all instances
/apps/catcsc/config.author/                       # Applied only to author instances
/apps/catcsc/config.publish/                      # Applied only to publish instances
/apps/catcsc/config.author.prod/                  # Instance-specific configs
/apps/catcsc/config.author.dev/
/apps/catcsc/config.publish.prod/
/apps/catcsc/config.publish.dev/
```

### Supported Configuration Formats

The listener supports configuration via:

1. **XML Files** (Recommended for AEM)
   - File: `com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
   - Location: `/apps/catcsc/config/` (or variant)

2. **JSON Files** (Reference only, not directly used by AEM)
   - File: `com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.config.json`
   - Useful for documentation and configuration management tools

## Configuration Properties

### enabled (Boolean)
- **Default:** `true`
- **Description:** Enable or disable the permission change listener
- **When disabled:** The listener component is not activated; no events are monitored
- **Use case:** Temporarily disable monitoring without uninstalling the bundle

```xml
<enabled>{Boolean}true</enabled>
```

### monitoredRootPath (String)
- **Default:** `/content/dam`
- **Description:** The repository path to monitor for permission changes
- **Scope:** All nested paths under this root are monitored (unless `monitorNestedPaths` is false)
- **Format:** Must be a valid JCR path

```xml
<monitoredRootPath>/content/dam</monitoredRootPath>
```

### monitorPropertyChanges (Boolean)
- **Default:** `true`
- **Description:** Monitor PROPERTY_CHANGED events
- **When enabled:** Detects when existing permission properties are modified
- **Example:** User updates ACL on a folder

```xml
<monitorPropertyChanges>{Boolean}true</monitorPropertyChanges>
```

### monitorPropertyAdditions (Boolean)
- **Default:** `true`
- **Description:** Monitor PROPERTY_ADDED events
- **When enabled:** Detects when new permission properties are added
- **Example:** New ACE (Access Control Entry) is added

```xml
<monitorPropertyAdditions>{Boolean}true</monitorPropertyAdditions>
```

### monitorPropertyRemovals (Boolean)
- **Default:** `true`
- **Description:** Monitor PROPERTY_REMOVED events
- **When enabled:** Detects when permission properties are removed
- **Example:** ACE is removed or permission is revoked

```xml
<monitorPropertyRemovals>{Boolean}true</monitorPropertyRemovals>
```

### monitorNestedPaths (Boolean)
- **Default:** `true`
- **Description:** Monitor nested child paths under the root path
- **When true:** All child paths under `monitoredRootPath` are monitored
- **When false:** Only changes to the root path itself are monitored
- **Performance:** Setting to false may improve performance on large trees

```xml
<monitorNestedPaths>{Boolean}true</monitorNestedPaths>
```

### logLevel (String)
- **Default:** `INFO`
- **Valid values:** `DEBUG`, `INFO`, `WARN`, `ERROR`
- **Description:** Controls the verbosity of logging output

| Level | Behavior |
|-------|----------|
| DEBUG | Most verbose; logs every event including filtered ones |
| INFO | Logs important events and state changes |
| WARN | Logs only warnings and errors |
| ERROR | Logs only error messages |

```xml
<logLevel>INFO</logLevel>
```

## Common Configuration Scenarios

### Scenario 1: Default Configuration (Monitor All DAM)

Monitor all permission changes under `/content/dam`:

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

### Scenario 2: Monitor Only Specific Folder

Monitor only `/content/dam/private`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
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

### Scenario 3: Monitor Only Property Changes

Monitor only when existing permissions are modified, not when added or removed:

```xml
<?xml version="1.0" encoding="UTF-8"?>
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

### Scenario 4: Disable on Publish

Publish instances typically don't need permission monitoring (permissions are changed on author):

**File:** `/apps/catcsc/config.publish/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          enabled="{Boolean}false"
          monitoredRootPath="/content/dam"
          logLevel="INFO"/>
```

### Scenario 5: Debug Mode

Enable detailed logging for troubleshooting:

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
          logLevel="DEBUG"/>
```

### Scenario 6: Company-Specific Monitoring

Monitor only a specific company's private content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          enabled="{Boolean}true"
          monitoredRootPath="/content/dam/company-a/private"
          monitorPropertyChanges="{Boolean}true"
          monitorPropertyAdditions="{Boolean}true"
          monitorPropertyRemovals="{Boolean}true"
          monitorNestedPaths="{Boolean}true"
          logLevel="INFO"/>
```

## Deployment Methods

### Method 1: Package Configuration in Code

Place configuration files in version control:

```
core/src/main/content/jcr_root/apps/catcsc/config/
├── com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml
core/src/main/content/jcr_root/apps/catcsc/config.author/
├── com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml
core/src/main/content/jcr_root/apps/catcsc/config.publish/
├── com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml
```

Build and deploy with your main package.

### Method 2: Manual Configuration via UI

1. Go to **Tools → Deployment → Configuration**
2. Search for: `com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener`
3. Edit the properties via the AEM UI
4. Save the configuration

### Method 3: Cloud Configuration via YAML

For AEM as a Cloud Service, use `config/` directory:

```
config/
├── com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener/
│   └── config.yaml
```

With content:

```yaml
enabled: true
monitoredRootPath: "/content/dam"
monitorPropertyChanges: true
monitorPropertyAdditions: true
monitorPropertyRemovals: true
monitorNestedPaths: true
logLevel: "INFO"
```

### Method 4: Configuration Management Tools

Export/import configurations using:
- CRX Package Manager
- Configuration management tools
- CI/CD pipelines with automated deployment

## Verifying Configuration

### Check Configuration via UI

1. Go to **OSGi Console** (Tools → OSGi Web Console)
2. Navigate to **Configuration**
3. Search for: `PrivateFolderPermissionChangeListener`
4. View current active configuration

### Check Listener Status

1. Go to **OSGi Console** → **Components**
2. Search for: `PrivateFolderPermissionChangeListener`
3. Check if component is **Active** or **Disabled**

### Check Logs

Monitor logs for activation messages:

```
tail -f crx-quickstart/logs/error.log | grep PrivateFolderPermissionChangeListener
```

Expected output when enabled:
```
INFO  PrivateFolderPermissionChangeListener activated. Listening for permission changes under path: /content/dam
```

Expected output when disabled:
```
INFO  PrivateFolderPermissionChangeListener is disabled via configuration
```

## Configuration Change Behavior

When you modify the OSGi configuration:

1. **Deactivate** - Old listener is unregistered and session is closed
2. **Activate** - New listener is registered with updated configuration
3. **No restart required** - Configuration changes take effect immediately
4. **No event loss** - Events generated during transition are handled properly

## Troubleshooting

### Listener Not Starting

1. Check if `enabled` is set to `true`
2. Verify the configuration file is in the correct location
3. Check OSGi console to see if component is active
4. Review error logs for exceptions

### Not Detecting Permission Changes

1. Verify `monitoredRootPath` is correct
2. Check if appropriate event type flags are enabled
3. Confirm `monitorNestedPaths` is true if monitoring child paths
4. Enable DEBUG logging and review logs

### High CPU/Memory Usage

1. Consider reducing the scope with a more specific path
2. Disable event types you don't need
3. Consider setting `monitorNestedPaths` to false if applicable
4. Distribute monitoring across multiple listeners with different paths

### Configuration Not Applied

1. Verify file is in correct location: `/apps/catcsc/config/` (not root level)
2. Check file naming: `com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
3. Verify XML syntax is valid
4. Restart bundle or instance
5. Clear AEM cache if needed

## Best Practices

1. **Version Control:** Keep configurations in Git
2. **Instance-Specific:** Use `config.author/` and `config.publish/` folders
3. **Document Changes:** Comment configuration files with purpose and date
4. **Test First:** Test configuration changes in dev before production
5. **Monitor Logs:** Regularly review logs after configuration changes
6. **Run Unit Tests:** Verify listener behavior after configuration changes
7. **Narrow Scope:** Monitor specific paths when possible rather than broad paths
8. **Production:** Disable on publish to reduce overhead
9. **Audit Trail:** Log permission changes for compliance

## Performance Considerations

### Scope Impact
- **Narrow paths** (e.g., `/content/dam/private`) = Lower overhead
- **Broad paths** (e.g., `/content`) = Higher overhead

### Event Type Impact
- **All events enabled** = Maximum overhead
- **Single event type** = Lower overhead

### Nested Path Impact
- **monitoring nested paths enabled** = Detects changes at any level
- **Only root path** = Detects changes only at root

