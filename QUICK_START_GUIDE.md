# Quick Start Guide: Configuring PrivateFolderPermissionChangeListener

## TL;DR (Quick Reference)

| Task | How |
|------|-----|
| **Enable Listener** | Set `enabled="{Boolean}true"` |
| **Disable Listener** | Set `enabled="{Boolean}false"` |
| **Change Path** | Set `monitoredRootPath="/content/dam/private"` |
| **Monitor Only Changes** | Set `monitorPropertyAdditions="{Boolean}false"` and `monitorPropertyRemovals="{Boolean}false"` |
| **Debug Issues** | Set `logLevel="DEBUG"` |
| **Disable on Publish** | Use config in `config.publish/` folder |

## Default Configuration (Out of the Box)

```xml
enabled: true
monitoredRootPath: /content/dam
monitorPropertyChanges: true
monitorPropertyAdditions: true
monitorPropertyRemovals: true
monitorNestedPaths: true
logLevel: INFO
```

## Common Tasks

### Task 1: Monitor Only Private Folder (Not All DAM)

**Location:** `/apps/catcsc/config/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`

**Change this:**
```xml
monitoredRootPath="/content/dam"
```

**To this:**
```xml
monitoredRootPath="/content/dam/private"
```

**Then:** Save and wait ~2 seconds for changes to take effect.

### Task 2: Turn Off the Listener

**Change this:**
```xml
enabled="{Boolean}true"
```

**To this:**
```xml
enabled="{Boolean}false"
```

**Then:** Save. Listener will not run (but bundle stays installed).

### Task 3: Monitor Only When Permissions are Modified (Not Added/Removed)

**Change this:**
```xml
monitorPropertyChanges="{Boolean}true"
monitorPropertyAdditions="{Boolean}true"
monitorPropertyRemovals="{Boolean}true"
```

**To this:**
```xml
monitorPropertyChanges="{Boolean}true"
monitorPropertyAdditions="{Boolean}false"
monitorPropertyRemovals="{Boolean}false"
```

### Task 4: Enable Debug Logging for Troubleshooting

**Change this:**
```xml
logLevel="INFO"
```

**To this:**
```xml
logLevel="DEBUG"
```

**Then:** Check logs via **Tools → Deployment → Logs** and search for `PrivateFolderPermissionChangeListener`

### Task 5: Different Config for Author vs Publish

**Author Configuration**
- Location: `/apps/catcsc/config.author/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
- Enabled: `true` (default)

**Publish Configuration**
- Location: `/apps/catcsc/config.publish/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
- Enabled: `false` (recommended)

## Verifying Your Configuration

### Via OSGi Console (Easiest)

1. Go to: **Tools → OSGi Web Console**
2. Click: **Configuration**
3. Search for: `com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener`
4. Current settings will be displayed

### Via Logs

```bash
tail -f crx-quickstart/logs/error.log | grep PrivateFolderPermissionChangeListener
```

Look for:
```
INFO  PrivateFolderPermissionChangeListener activated. Listening for permission changes under path: /content/dam
```

### Via OSGi Components

1. Go to: **Tools → OSGi Web Console**
2. Click: **Components**
3. Search for: `PrivateFolderPermissionChangeListener`
4. Status shows: **Active** or **Satisfied**

## Properties Explained (In Plain English)

| Property | Means |
|----------|-------|
| `enabled` | Is the listener turned on? true/false |
| `monitoredRootPath` | Which folder to watch (e.g., `/content/dam`) |
| `monitorPropertyChanges` | Watch when permissions change? true/false |
| `monitorPropertyAdditions` | Watch when permissions are added? true/false |
| `monitorPropertyRemovals` | Watch when permissions are removed? true/false |
| `monitorNestedPaths` | Watch sub-folders too? true/false |
| `logLevel` | How much detail in logs? DEBUG/INFO/WARN/ERROR |

## Real-World Examples

### Example 1: Monitor Only Company A's Private Content

```xml
monitoredRootPath="/content/dam/company-a/private"
monitoring everything else...
```

### Example 2: Disable Listener Temporarily for Maintenance

```xml
enabled="{Boolean}false"
```

Wait for logs to show:
```
INFO  PrivateFolderPermissionChangeListener is disabled via configuration
```

Then re-enable when done.

### Example 3: Focus on Modifications Only (Ignore Adds/Removes)

```xml
monitorPropertyChanges="{Boolean}true"
monitorPropertyAdditions="{Boolean}false"
monitorPropertyRemovals="{Boolean}false"
```

This is useful if you only care about when existing permissions change, not when permissions are created or deleted.

### Example 4: Production Setup (Author + Publish)

**Author (/apps/catcsc/config.author/)**
```xml
enabled="{Boolean}true"
monitoredRootPath="/content/dam"
```

**Publish (/apps/catcsc/config.publish/)**
```xml
enabled="{Boolean}false"
```

This way:
- Author monitors all permission changes
- Publish ignores permission changes (they don't happen there anyway)

## Troubleshooting Checklist

| Problem | Solution |
|---------|----------|
| Listener not starting | Check if `enabled=true`. Check OSGi console for component status |
| Not detecting changes | Verify `monitoredRootPath` is correct. Check if event types are enabled |
| High log verbosity | Change `logLevel` from DEBUG to INFO |
| Can't find configuration | Check if file is in correct folder: `/apps/catcsc/config/` (not root) |
| Changes not taking effect | Wait 2-3 seconds. Reload OSGi console or check logs |

## File Locations

```
/apps/catcsc/config/                                    ← Default config (all instances)
/apps/catcsc/config.author/                             ← Author config (overrides default)
/apps/catcsc/config.publish/                            ← Publish config (overrides default)
```

## File Name

Always use this exact filename:
```
com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml
```

## Configuration Changes Take Effect In

- ✅ ~2 seconds (usually)
- ✅ No restart needed
- ✅ No bundle restart needed
- ✅ Can change while system is running

## All Properties and Defaults

```xml
enabled="{Boolean}true"
monitoredRootPath="/content/dam"
monitorPropertyChanges="{Boolean}true"
monitorPropertyAdditions="{Boolean}true"
monitorPropertyRemovals="{Boolean}true"
monitorNestedPaths="{Boolean}true"
logLevel="INFO"
```

Copy & paste into your configuration file to get started!

## Next Steps

1. **Decide your configuration** - What path? All event types? Debug mode?
2. **Update the XML file** - `/apps/catcsc/config/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
3. **Deploy/Save** - Package deployment or manual save via UI
4. **Verify** - Check OSGi console that component is Active
5. **Monitor logs** - Check `/crx-quickstart/logs/error.log` for confirmation
6. **Test** - Make a permission change and verify it's logged

## Still Need Help?

See full guides:
- **OSGI_CONFIGURATION_GUIDE.md** - Comprehensive configuration guide
- **OSGi_CONFIGURATION_IMPLEMENTATION_SUMMARY.md** - Technical summary
- **PRIVATE_FOLDER_PERMISSION_LISTENER_README.md** - How the listener works internally

