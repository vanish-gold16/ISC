# Java status endpoint guide (step-by-step)

The frontend loads presence pills next to every post/profile by polling `GET /api/users/statuses?userIds=1,2,3`. It expects a simple JSON map: each key is a user ID and the value is a short DTO with `state` and optionally `lastActive`. Example:

```json
{
  "1": { "state": "ONLINE", "lastActive": "2024-12-03T15:04:05Z" },
  "2": { "state": "OFFLINE", "lastActive": "2024-12-03T14:58:00Z" }
}
```

The frontend:
1. Interprets `state` values `ONLINE`, `IDLE`/`AWAY`/`BUSY` (treated as “Away”) or anything else (`Last seen at ...`).
2. Uses `lastActive` to format the timestamp that appears next to “Last seen at”.
3. Contacts the endpoint every 20 seconds, so it must be quick and cache-friendly.

### Step 1: define the DTO/enum
```java
public enum PresenceState {
    ONLINE,
    IDLE,
    OFFLINE
}

public record UserStatusDto(PresenceState state, Instant lastActive) {}
```

### Step 2: track presence
- Keep a concurrent map between user IDs and `UserStatusDto`.
- On sign-in/interaction set state `ONLINE` and update `lastActive`.
- On session expiration (or if the app later supports private accounts) flip the value to `OFFLINE`.
- Optionally run a scheduled task that demotes `ONLINE` → `IDLE` after a grace period of inactivity.

### Step 3: expose the controller
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
Return `OFFLINE` for a user only if the account explicitly prevents showing online status (private accounts you might add later); otherwise default the DTO to `ONLINE`/`IDLE` based on recent activity.

### Step 4: secure the endpoint
- Protect `/api/users/statuses` with the same authentication as the rest of the site.
- Read `userIds` from the query string and answer only for the requested subset.
- Always respond with `application/json` and the simple map (no envelopes).

Once the service is wired into authentication events, the frontend updates the pill automatically—no additional frontend changes are necessary.
