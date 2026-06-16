# Configuration Templates - Copy & Paste Ready

All configurations below are ready to use. Simply copy the entire XML block and replace the content in your configuration file at:
- `/apps/catcsc/config/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml` (global)
- `/apps/catcsc/config.author/...` (author only)
- `/apps/catcsc/config.publish/...` (publish only)

---

## Template 1: Default (Monitor All DAM with All Events)

**Use this if:** You want to monitor all permission changes under `/content/dam`

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

---

## Template 2: Private Folder Only

**Use this if:** You only want to monitor `/content/dam/private` folder

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

---

## Template 3: Monitor Changes Only (No Adds/Removes)

**Use this if:** You only care about when existing permissions are modified

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

---

## Template 4: Disabled Listener

**Use this if:** You want to temporarily disable the listener

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          enabled="{Boolean}false"
          monitoredRootPath="/content/dam"
          monitorPropertyChanges="{Boolean}true"
          monitorPropertyAdditions="{Boolean}true"
          monitorPropertyRemovals="{Boolean}true"
          monitorNestedPaths="{Boolean}true"
          logLevel="INFO"/>
```

---

## Template 5: Debug Mode

**Use this if:** You need to troubleshoot permission changes (generate detailed logs)

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

---

## Template 6: Author Instance Configuration

**Use this if:** You want specific config for author instances (normally enabled)

**File location:** `/apps/catcsc/config.author/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`

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

---

## Template 7: Publish Instance Configuration

**Use this if:** You want to disable the listener on publish (recommended)

**File location:** `/apps/catcsc/config.publish/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:privateType="sling:OsgiConfig"
          enabled="{Boolean}false"
          monitoredRootPath="/content/dam"
          monitorPropertyChanges="{Boolean}true"
          monitorPropertyAdditions="{Boolean}true"
          monitorPropertyRemovals="{Boolean}true"
          monitorNestedPaths="{Boolean}true"
          logLevel="INFO"/>
```

---

## Template 8: Company-Specific Monitoring

**Use this if:** You want to monitor specific company folders

**Change this:**
```xml
monitoredRootPath="/content/dam/company-a/private"
```

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

---

## Template 9: Monitor All Content (Not Just DAM)

**Use this if:** You want to monitor permission changes anywhere under `/content/`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
          xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          enabled="{Boolean}true"
          monitoredRootPath="/content"
          monitorPropertyChanges="{Boolean}true"
          monitorPropertyAdditions="{Boolean}true"
          monitorPropertyRemovals="{Boolean}true"
          monitorNestedPaths="{Boolean}true"
          logLevel="INFO"/>
```

---

## Template 10: Minimal Monitoring (Only Changes, No Nested)

**Use this if:** You want minimal overhead - only track direct permission modifications

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
          monitorNestedPaths="{Boolean}false"
          logLevel="WARN"/>
```

---

## How to Use These Templates

### Step 1: Choose a Template
Pick the template that matches your needs from above.

### Step 2: Copy the XML
Copy the entire XML block (from `<?xml...` to `/>`)

### Step 3: Decide Location
- **Global config:** `/apps/catcsc/config/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
- **Author only:** `/apps/catcsc/config.author/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
- **Publish only:** `/apps/catcsc/config.publish/com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`

### Step 4: Apply Configuration

**Option A: Via Package (Recommended)**
1. Put XML in code: `core/src/main/content/jcr_root/apps/catcsc/config/`
2. Build: `mvn clean install`
3. Deploy: `mvn clean install -PautoInstallSinglePackage`

**Option B: Via UI**
1. Go to: **Tools → Deployment → Configuration**
2. Search: `PrivateFolderPermissionChangeListener`
3. Edit each property
4. Save

**Option C: Manual Upload**
1. Go to: **CRXDE Lite**
2. Navigate to: `/apps/catcsc/config/`
3. Create new file: `com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
4. Paste XML content
5. Save

### Step 5: Verify
1. Check logs for: `PrivateFolderPermissionChangeListener activated`
2. Or go to: **Tools → OSGi Web Console → Components**
3. Search: `PrivateFolderPermissionChangeListener`
4. Status should be: **Active** or **Satisfied**

---

## Customization Guide

### Change the Monitored Path

In any template, replace:
```xml
monitoredRootPath="/content/dam"
```

With your desired path:
```xml
monitoredRootPath="/content/dam/private"
```

### Change Event Types

To monitor only specific events, use:

**Only changes (most common):**
```xml
monitorPropertyChanges="{Boolean}true"
monitorPropertyAdditions="{Boolean}false"
monitorPropertyRemovals="{Boolean}false"
```

**Only additions and removals (not changes):**
```xml
monitorPropertyChanges="{Boolean}false"
monitorPropertyAdditions="{Boolean}true"
monitorPropertyRemovals="{Boolean}true"
```

**Only additions:**
```xml
monitorPropertyChanges="{Boolean}false"
monitorPropertyAdditions="{Boolean}true"
monitorPropertyRemovals="{Boolean}false"
```

### Change Logging Level

Replace:
```xml
logLevel="INFO"
```

With:
```xml
logLevel="DEBUG"    <!-- Most verbose -->
logLevel="INFO"     <!-- Standard -->
logLevel="WARN"     <!-- Warnings and errors only -->
logLevel="ERROR"    <!-- Errors only -->
```

---

## Property Reference

| Property | Possible Values | Default |
|----------|-----------------|---------|
| `enabled` | `{Boolean}true` or `{Boolean}false` | true |
| `monitoredRootPath` | Any valid JCR path | `/content/dam` |
| `monitorPropertyChanges` | `{Boolean}true` or `{Boolean}false` | true |
| `monitorPropertyAdditions` | `{Boolean}true` or `{Boolean}false` | true |
| `monitorPropertyRemovals` | `{Boolean}true` or `{Boolean}false` | true |
| `monitorNestedPaths` | `{Boolean}true` or `{Boolean}false` | true |
| `logLevel` | `DEBUG`, `INFO`, `WARN`, `ERROR` | `INFO` |

---

## Quick Troubleshooting

**Configuration not working?**
1. Verify file is in correct location: `/apps/catcsc/config/`
2. Verify filename is exact: `com.cat.csc.core.listeners.PrivateFolderPermissionChangeListener.xml`
3. Check XML syntax is valid
4. Wait 2-3 seconds for OSGi to refresh
5. Check logs: `grep -i PrivateFolderPermissionChangeListener /path/to/error.log`

**Want to check current config?**
1. Go to: **Tools → OSGi Web Console → Configuration**
2. Search: `PrivateFolderPermissionChangeListener`
3. Click to view current settings

---

## Common Path Examples

```
/content/dam                              ← All DAM content
/content/dam/private                      ← Private folder only
/content/dam/company-a                    ← Specific company
/content/dam/company-a/private            ← Specific company private
/content                                  ← All content
/content/dam/assets                       ← Assets subfolder
```

---

## Before & After Examples

### Before
```xml
monitoredRootPath="/content/dam"
monitorPropertyAdditions="{Boolean}true"
```

### After (to monitor only direct folder)
```xml
monitoredRootPath="/content/dam/private"
monitorPropertyAdditions="{Boolean}false"
```

---

**Having issues?** Check QUICK_START_GUIDE.md for more help!

