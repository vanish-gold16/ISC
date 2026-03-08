(function () {
    const elements = Array.from(document.querySelectorAll('[data-user-id]'));
    if (!elements.length || !window.fetch) {
        return;
    }

    const containersMap = new Map();
    elements.forEach(el => {
        const userId = el.getAttribute('data-user-id');
        if (!userId) {
            return;
        }
        if (!containersMap.has(userId)) {
            containersMap.set(userId, []);
        }
        containersMap.get(userId).push(el);
    });

    const userIds = Array.from(containersMap.keys());
    if (!userIds.length) {
        return;
    }

    const STATUS_ENDPOINT = '/api/users/statuses';
    const POLL_INTERVAL = 20000;

    async function refreshStatuses() {
        try {
            const params = new URLSearchParams();
            params.set('userIds', userIds.join(','));
            const response = await fetch(`${STATUS_ENDPOINT}?${params.toString()}`, {
                headers: {
                    'Accept': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            if (!response.ok) {
                throw new Error('Failed to load user statuses');
            }
            const payload = await response.json();
            userIds.forEach(id => applyStatus(id, payload && payload[id] ? payload[id] : null));
        } catch (err) {
            userIds.forEach(id => applyStatus(id, null));
            console.warn('Unable to refresh user status', err);
        }
    }

    function applyStatus(id, data) {
        const normalized = normalizeState(data && data.state);
        const lastActive = parseLastActive(data && data.lastActive);
        const containers = containersMap.get(id);
        if (!containers) {
            return;
        }
        containers.forEach(container => {
            container.dataset.statusState = normalized;
            updateText(container, normalized, lastActive);
        });
    }

    function normalizeState(raw) {
        if (!raw) {
            return 'offline';
        }
        const value = raw.toString().toLowerCase();
        if (value === 'online') {
            return 'online';
        }
        if (value === 'idle' || value === 'away' || value === 'busy') {
            return 'idle';
        }
        return 'offline';
    }

    function parseLastActive(value) {
        if (!value) {
            return null;
        }
        const parsed = new Date(value);
        return Number.isNaN(parsed.getTime()) ? null : parsed;
    }

    function updateText(container, state, lastActive) {
        const primary = container.querySelector('.user-status__text');
        const secondary = container.querySelector('.user-status__secondary');
        if (primary) {
            primary.textContent = getPrimaryText(state, lastActive);
        }
        if (secondary) {
            const secondaryText = getSecondaryText(state, lastActive);
            secondary.textContent = secondaryText;
            secondary.hidden = !secondaryText;
        }
    }

    function getPrimaryText(state, lastActive) {
        if (state === 'online') {
            return 'Online';
        }
        if (state === 'idle') {
            return 'Away';
        }
        if (!lastActive) {
            return 'Offline';
        }
        return 'Last seen at';
    }

    function getSecondaryText(state, lastActive) {
        if (state === 'online') {
            return 'Active now';
        }
        if (state === 'offline' && !lastActive) {
            return '';
        }
        if (!lastActive) {
            return 'Unknown';
        }
        return formatAbsolute(lastActive);
    }

    function formatAbsolute(date) {
        const options = {
            hour: 'numeric',
            minute: 'numeric',
            day: 'numeric',
            month: 'short'
        };
        return new Intl.DateTimeFormat(undefined, options).format(date);
    }

    refreshStatuses();
    setInterval(refreshStatuses, POLL_INTERVAL);
})();
