# Private Folder Permission Change Listener

## Overview

The `PrivateFolderPermissionChangeListener` is a JCR event listener that monitors all permission changes on folders under `/content/dam` in your AEM instance. This listener is useful for tracking access control modifications, auditing permission changes, or triggering custom business logic when DAM folder permissions are modified.

## Features

- **Automatic Permission Monitoring**: Continuously monitors permission changes under `/content/dam`
- **ACL Change Detection**: Detects changes to `rep:policy` (Access Control Lists) nodes
- **Property Level Tracking**: Detects both property additions, removals, and modifications
- **User Attribution**: Logs which user made the permission change
- **Deep Path Monitoring**: Listens to all nested folders under `/content/dam`
- **Proper Lifecycle Management**: Uses OSGi @Activate and @Deactivate lifecycle methods

## How It Works

### Event Types Monitored

The listener monitors the following JCR event types:
- `PROPERTY_CHANGED`: When permission properties are modified
- `PROPERTY_ADDED`: When new permission properties are added
- `PROPERTY_REMOVED`: When permission properties are removed

### Permission Detection

The listener identifies permission-related changes by:
1. Checking if the event path contains `rep:policy` (JCR Access Control List nodes)
2. Checking if the event is on known permission-related properties:
   - `jcr:access`
   - `rep:principalName`
   - `rep:glob`
   - `rep:restrictions`
   - `jcr:uuid`

### Path Examples Monitored

```
/content/dam/folder/jcr:content/rep:policy
/content/dam/private/folder/jcr:content/rep:policy/allow
/content/dam/assets/jcr:content/rep:policy/deny
```

## Implementation Details

### OSGi Component Registration

The listener is registered as an OSGi service:

```java
@Component(service = PrivateFolderPermissionChangeListener.class)
@ServiceDescription("JCR Event Listener for private folder permission changes under /content/dam")
public class PrivateFolderPermissionChangeListener implements EventListener
```

### Service Activation

When the component is activated:
1. A service session is created via `SlingRepository.loginService()`
2. The `ObservationManager` registers the event listener
3. The listener begins monitoring events on `/content/dam`

### Service Deactivation

When the component is deactivated:
1. The event listener is unregistered from `ObservationManager`
2. The service session is logged out
3. Resources are properly cleaned up

## Customization

### Adding Custom Business Logic

To add your own logic when permission changes are detected, modify the `handlePermissionChange()` method:

```java
private void handlePermissionChange(Event event) throws RepositoryException {
    String path = event.getPath();
    String userId = event.getUserID();
    String folderPath = extractFolderPath(path);
    
    // Add your custom logic here:
    // - Send email notifications
    // - Update audit logs
    // - Trigger workflows
    // - Update indexes
    // - Call external APIs
    
    LOGGER.info("Permission modified: {} by {}", folderPath, userId);
}
```

### Filtering Specific Paths

To listen only to specific folders, modify the activation code:

```java
// Listen only to /content/dam/private
observationManager.addEventListener(
    this,
    eventTypes,
    "/content/dam/private",  // Change this path
    deep,
    null,
    nodeTypes,
    noLocal
);
```

### Filtering Specific Event Types

To listen only to property changes (not additions/removals):

```java
int eventTypes = Event.PROPERTY_CHANGED;  // Only changes, not additions or removals
```

### Filtering Specific Node Types

To listen only to specific node types:

```java
String[] nodeTypes = {"cq:Page", "cq:Asset"};
observationManager.addEventListener(
    this,
    eventTypes,
    DAM_PATH,
    deep,
    null,
    nodeTypes,  // Now filtering by node type
    noLocal
);
```

## Logging Output

The listener produces the following log messages:

```
INFO: PrivateFolderPermissionChangeListener activated. Listening for permission changes under /content/dam

INFO: Permission change event - Type: PROPERTY_CHANGED, Path: /content/dam/folder/jcr:content/rep:policy, User: admin

DEBUG: Permission change detected at path: /content/dam/folder/jcr:content/rep:policy (event type: 3)

DEBUG: Affected folder: /content/dam/folder

INFO: PrivateFolderPermissionChangeListener deactivated
```

## Usage Example

### Basic Usage

The listener is automatically registered and starts monitoring when the bundle is deployed:

1. Deploy the bundle containing `PrivateFolderPermissionChangeListener`
2. The listener is activated automatically
3. Permission changes under `/content/dam` will be logged
4. Check the logs for permission change events

### Integration with Audit Service

```java
private void handlePermissionChange(Event event) throws RepositoryException {
    String path = event.getPath();
    String userId = event.getUserID();
    String folderPath = extractFolderPath(path);
    
    // Log to audit service
    auditService.log(
        "DAM_PERMISSION_CHANGE",
        folderPath,
        userId,
        "Permission changed"
    );
}
```

### Integration with Notification Service

```java
private void handlePermissionChange(Event event) throws RepositoryException {
    String path = event.getPath();
    String userId = event.getUserID();
    String folderPath = extractFolderPath(path);
    
    // Send notification
    notificationService.notifyAdmins(
        "Permission Changed",
        "User " + userId + " modified permissions on " + folderPath
    );
}
```

## Testing

Unit tests are provided in `PrivateFolderPermissionChangeListenerTest.java`:

```bash
# Run tests
mvn clean test

# Run specific test
mvn clean test -Dtest=PrivateFolderPermissionChangeListenerTest
```

Test coverage includes:
- Component activation
- Event listener registration
- Permission change event handling
- Non-permission event filtering
- Component deactivation
- Resource cleanup

## Performance Considerations

1. **Session Management**: The listener uses a service session that persists during the component's lifetime
2. **Event Processing**: Events are processed synchronously; consider async processing for heavy operations
3. **Filter Specificity**: More specific path/node type filters improve performance
4. **Repository Load**: Large numbers of permission changes may impact system performance; implement rate limiting if needed

## Troubleshooting

### Listener Not Detecting Changes

1. Verify the listener is activated: Check OSGi console → Components
2. Check bundle status: Ensure the bundle is in "Active" state
3. Verify paths: Ensure permission changes occur under `/content/dam`
4. Check logs: Look for activation errors in `error.log`

### Session Timeout

If experiencing session timeout issues:

```java
// Use a session with appropriate timeout settings
Map<String, Object> sessionOptions = new HashMap<>();
session = repository.loginService("repoWriteService", "default", sessionOptions);
```

### High Memory Usage

Consider:
1. Reducing the scope of monitoring (more specific paths)
2. Implementing asynchronous processing of events
3. Adding event batching/throttling

## Dependencies

- `javax.jcr:jcr` - JCR API
- `org.apache.sling:org.apache.sling.jcr.api` - Sling JCR API
- `org.osgi:org.osgi.service.component.annotations` - OSGi Component Service annotations
- `org.slf4j:slf4j-api` - SLF4J logging

## Related Resources

- [AEM JCR Event Listeners](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/event-listener.html)
- [JCR Observation API](https://www.day.com/content/dam/day/specs/jcr/2.0/jcr-2.0-spec.pdf)
- [ACL (Access Control Lists) in AEM](https://experienceleague.adobe.com/docs/experience-manager-64/administering/security/permissions.html)
- [OSGi Component Service](https://osgi.org/specification/osgi.cmpn/7.0.0/service.component.html)

