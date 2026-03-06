# Java status endpoint guide

This project now renders a live status pill next to every profile and the author line on posts. The frontend requests user presence via `GET /api/users/statuses` and expects the following contract:

1. **Request**: query parameter `userIds` with comma-separated numeric ids (e.g. `?userIds=1,2,3`).
2. **Response**: application/json body that maps each id to a small DTO:

```json
{
  "1": { "state": "ONLINE", "lastActive": "2024-12-03T15:04:05Z" },
  "2": { "state": "OFFLINE", "lastActive": "2024-12-03T14:58:00Z" }
}
```

- `state` should be one of `ONLINE`, `IDLE`/`AWAY`/`BUSY` (the UI treats these as `Away`), or anything else (falls back to `Offline`).
- `lastActive` is optional but should be an ISO-8601 timestamp (UTC) when available.

The frontend polls this endpoint every 20 seconds, so the implementation must be fast.

## Suggested Java pieces

```java
public enum PresenceState {
    ONLINE,
    IDLE,
    OFFLINE
}

public record UserStatusDto(PresenceState state, Instant lastActive) {}
```

Keep a concurrent map of `userId -> UserStatusDto` that is updated whenever the user interacts with the application:

- on authentication success mark the id `ONLINE` and remember `Instant.now()`;
- on logout or session expiration mark `OFFLINE` and set the timestamp;
- optionally, use a scheduled task to degrade `ONLINE` -> `IDLE` if no traffic arrives for a configurable grace period.

Expose a controller that accepts the `userIds` parameter, resolves the subset of cached statuses, and returns them as a map. For example:

```java
@RestController
@RequestMapping("/api/users")
public class UserStatusController {
    private final UserPresenceService presenceService;

    public UserStatusController(UserPresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping("/statuses")
    public Map<Long, UserStatusDto> list(@RequestParam List<Long> userIds) {
        return presenceService.getStatuses(userIds);
    }
}
```

`UserPresenceService#getStatuses` should return the best known status for every requested id (default to `OFFLINE` with no `lastActive` if unknown).

### Additional notes

- Protect the endpoint with the same authentication used by the rest of the site so only signed-in users can poll statuses.
- If you already track active websocket sessions or HTTP requests via filters, reuse that data for `lastActive` instead of duplicating state.
- The frontend script expects the response to be a simple JSON object; do not wrap it in another envelope.
- After implementing the service, no additional frontend changes are necessary; the new pill will update automatically.
