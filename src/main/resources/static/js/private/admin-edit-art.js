(function () {
    const queue = document.getElementById('admin-edit-queue');
    const queueMode = document.getElementById('admin-edit-mode');
    const queueCount = document.getElementById('admin-edit-count');
    const searchForm = document.getElementById('admin-edit-search-form');
    const searchInput = document.getElementById('admin-edit-search');
    const searchReset = document.getElementById('admin-edit-search-reset');
    const csrfToken = document.getElementById('admin-csrf');
    const form = document.getElementById('admin-edit-form');
    const emptyState = document.getElementById('admin-edit-empty');
    const emptyStateTitle = document.getElementById('admin-edit-empty-title');
    const emptyStateText = document.getElementById('admin-edit-empty-text');
    const content = document.getElementById('admin-edit-content');
    const idInput = document.getElementById('admin-edit-art-id');
    const artNumberInput = document.getElementById('admin-edit-art-number');
    const createdAtInput = document.getElementById('admin-edit-created-at');
    const createdDisplayInput = document.getElementById('admin-edit-created-display');
    const updatedDisplayInput = document.getElementById('admin-edit-updated-display');
    const coverUrlInput = document.getElementById('admin-edit-cover-url');
    const nameInput = document.getElementById('admin-edit-name');
    const authorInput = document.getElementById('admin-edit-author');
    const typeInput = document.getElementById('admin-edit-type');
    const descriptionInput = document.getElementById('admin-edit-description');
    const cover = document.getElementById('admin-edit-cover');
    const coverFileInput = document.getElementById('admin-edit-cover-file');
    const coverUpload = document.getElementById('admin-edit-cover-upload');
    const coverRemove = document.getElementById('admin-edit-cover-remove');
    const coverHint = document.getElementById('admin-edit-cover-hint');
    const message = document.getElementById('admin-edit-message');
    const status = document.getElementById('admin-edit-status');
    const feedback = document.getElementById('admin-edit-feedback');
    const save = document.getElementById('admin-edit-save');
    const deleteButton = document.getElementById('admin-edit-delete');
    const maxCoverBytes = 5 * 1024 * 1024;

    if (!queue || !form) {
        return;
    }

    let items = [];
    let selectedId = null;
    let busy = false;
    let pendingCoverDraft = null;

    function currentSearch() {
        return searchInput ? searchInput.value.trim() : '';
    }

    function current() {
        return items.find((item) => item.id === selectedId) || null;
    }

    function csrfHeaders() {
        return csrfToken && csrfToken.value ? { 'X-CSRF-TOKEN': csrfToken.value } : {};
    }

    function safeText(value, fallback) {
        if (value == null) {
            return fallback;
        }
        const normalized = String(value).trim();
        return normalized ? normalized : fallback;
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function toTitleCase(value) {
        return String(value || '')
            .toLowerCase()
            .split('_')
            .filter(Boolean)
            .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
            .join(' ');
    }

    function formatType(value) {
        return toTitleCase(value) || 'Unknown';
    }

    function displayName(item) {
        return item && item.name ? item.name : 'Untitled work';
    }

    function displayAuthor(item) {
        return item && item.author ? item.author : 'Unknown author';
    }

    function initialsFor(item) {
        const source = displayName(item).trim();
        const parts = source.split(/\s+/).filter(Boolean).slice(0, 2);
        if (!parts.length) {
            return 'AR';
        }
        return parts.map((part) => part.charAt(0).toUpperCase()).join('');
    }

    function formatDateTime(value) {
        if (!value) {
            return 'Not available';
        }

        const parsed = new Date(value);
        if (Number.isNaN(parsed.getTime())) {
            return String(value).replace('T', ' ');
        }

        return parsed.toLocaleString(undefined, {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    function toLocalDateTimeString() {
        const now = new Date();
        const pad = (value) => String(value).padStart(2, '0');
        return [
            now.getFullYear(),
            pad(now.getMonth() + 1),
            pad(now.getDate())
        ].join('-') + 'T' + [
            pad(now.getHours()),
            pad(now.getMinutes()),
            pad(now.getSeconds())
        ].join(':');
    }

    function normalizeItem(item) {
        return {
            id: item && item.id != null ? item.id : (item && item.artId != null ? item.artId : null),
            type: safeText(item && item.type, ''),
            name: safeText(item && item.name, ''),
            author: safeText(item && item.author, ''),
            description: safeText(item && item.description, ''),
            coverUrl: safeText(item && item.coverUrl, ''),
            createdAt: safeText(item && item.createdAt, ''),
            editedAt: safeText(item && (item.editedAt || item.updatedAt), '')
        };
    }

    function mergeNormalizedItem(base, incoming) {
        const source = base || {};
        const patch = incoming || {};
        return {
            id: patch.id != null ? patch.id : (source.id != null ? source.id : null),
            type: patch.type || source.type || '',
            name: patch.name || source.name || '',
            author: patch.author || source.author || '',
            description: patch.description !== '' ? patch.description : (source.description || ''),
            coverUrl: patch.coverUrl !== '' ? patch.coverUrl : (source.coverUrl || ''),
            createdAt: patch.createdAt || source.createdAt || '',
            editedAt: patch.editedAt || source.editedAt || ''
        };
    }

    function setFeedback(mode, text) {
        feedback.className = 'admin-feedback';
        if (mode) {
            feedback.classList.add('is-' + mode);
        }
        feedback.textContent = text || '';
    }

    function setEmptyState(title, text) {
        emptyStateTitle.textContent = title;
        emptyStateText.textContent = text;
    }

    function setCoverHint(text, isError) {
        coverHint.textContent = text;
        coverHint.classList.toggle('is-error', Boolean(isError));
    }

    function clearPendingCoverDraft() {
        if (pendingCoverDraft && pendingCoverDraft.previewUrl) {
            URL.revokeObjectURL(pendingCoverDraft.previewUrl);
        }
        pendingCoverDraft = null;
        if (coverFileInput) {
            coverFileInput.value = '';
        }
    }

    function hasPendingCoverDraft() {
        return Boolean(pendingCoverDraft && pendingCoverDraft.file);
    }

    function selectedCoverUrl() {
        if (pendingCoverDraft && pendingCoverDraft.previewUrl) {
            return pendingCoverDraft.previewUrl;
        }
        return coverUrlInput.value.trim();
    }

    function hasSelectedCover() {
        return Boolean(selectedCoverUrl());
    }

    function syncCoverHint() {
        if (!selectedId) {
            setCoverHint('Select an artwork to edit its cover.', false);
            return;
        }
        if (hasPendingCoverDraft()) {
            setCoverHint('New cover selected. Save changes to upload it.', false);
            return;
        }
        if (coverUrlInput.value.trim()) {
            setCoverHint('Current cover stays until you replace or remove it.', false);
            return;
        }
        setCoverHint('This artwork has no cover yet. You can upload one.', false);
    }

    function syncControls() {
        const hasSelection = Boolean(selectedId && current());
        const formFields = [
            nameInput,
            authorInput,
            typeInput,
            descriptionInput,
            coverFileInput
        ];
        const actionButtons = [
            save,
            deleteButton,
            coverUpload
        ];

        formFields.forEach((field) => {
            if (field) {
                field.disabled = busy || !hasSelection;
            }
        });
        actionButtons.forEach((button) => {
            if (button) {
                button.disabled = busy || !hasSelection;
            }
        });
        if (coverRemove) {
            coverRemove.disabled = busy || !hasSelection || !hasSelectedCover();
        }
        if (searchInput) {
            searchInput.disabled = busy;
        }
        if (searchReset) {
            searchReset.disabled = busy;
        }
    }

    function setBusy(nextBusy) {
        busy = Boolean(nextBusy);
        syncControls();
    }

    function renderPreview(item) {
        const previewUrl = selectedCoverUrl();
        if (previewUrl) {
            cover.classList.add('has-image');
            cover.innerHTML = '<img src="' + escapeHtml(previewUrl) + '" alt="" data-cover-image />';
            if (window.__opuscoreApplyCoverFrame) {
                window.__opuscoreApplyCoverFrame(cover);
            }
        } else {
            cover.classList.remove('has-image');
            cover.innerHTML = escapeHtml(item ? initialsFor(item) : 'No cover');
            cover.style.removeProperty('--cover-frame-aspect');
            cover.style.removeProperty('aspect-ratio');
            delete cover.dataset.coverShape;
        }

        if (!item) {
            message.textContent = 'No art selected.';
            return;
        }

        const description = item.description
            ? item.description
            : 'No description yet.';
        message.textContent = 'Art #' + item.id + ' | ' + formatType(item.type) + ' | ' + displayAuthor(item) + '. ' + description;
    }

    function renderEditor() {
        const item = current();

        if (!item) {
            emptyState.hidden = false;
            content.hidden = true;
            idInput.value = '';
            artNumberInput.value = '';
            createdAtInput.value = '';
            createdDisplayInput.value = '';
            updatedDisplayInput.value = '';
            coverUrlInput.value = '';
            nameInput.value = '';
            authorInput.value = '';
            typeInput.value = '';
            descriptionInput.value = '';
            status.textContent = 'Catalog item';
            renderPreview(null);
            syncCoverHint();
            syncControls();
            return;
        }

        emptyState.hidden = true;
        content.hidden = false;
        idInput.value = item.id != null ? String(item.id) : '';
        artNumberInput.value = item.id != null ? '#' + item.id : '';
        createdAtInput.value = item.createdAt || '';
        createdDisplayInput.value = formatDateTime(item.createdAt);
        updatedDisplayInput.value = formatDateTime(item.editedAt || item.createdAt);
        coverUrlInput.value = item.coverUrl || '';
        nameInput.value = item.name || '';
        authorInput.value = item.author || '';
        typeInput.value = item.type || '';
        descriptionInput.value = item.description || '';
        status.textContent = item.id != null ? 'Art #' + item.id : 'Catalog item';
        renderPreview(item);
        syncCoverHint();
        syncControls();
    }

    function renderQueue() {
        const query = currentSearch();

        queueMode.textContent = query ? 'Search results' : 'Catalog search';
        queueCount.textContent = query
            ? items.length + (items.length === 1 ? ' result' : ' results')
            : 'Type to search';

        if (!items.length) {
            const emptyMessage = query
                ? 'No artworks matched this search.'
                : 'Search by work name or author to see catalog items here.';
            queue.innerHTML = '<div class="admin-queue__empty">' + escapeHtml(emptyMessage) + '</div>';
            return;
        }

        queue.innerHTML = items.map((item) => {
            const preview = item.coverUrl
                ? '<img src="' + escapeHtml(item.coverUrl) + '" alt="" />'
                : escapeHtml(initialsFor(item));
            const activeClass = item.id === selectedId ? ' is-active' : '';
            const coverClass = item.coverUrl ? ' has-image' : '';
            return '' +
                '<button class="admin-queue__item' + activeClass + '" type="button" data-admin-edit-id="' + escapeHtml(item.id) + '">' +
                    '<div class="admin-queue__cover' + coverClass + '">' + preview + '</div>' +
                    '<div class="admin-queue__body">' +
                        '<span class="admin-queue__title">' + escapeHtml(displayName(item)) + '</span>' +
                        '<span class="admin-queue__meta">' + escapeHtml(displayAuthor(item)) + ' | ' + escapeHtml(formatType(item.type)) + '</span>' +
                    '</div>' +
                    '<span class="admin-queue__status">Art #' + escapeHtml(item.id) + '</span>' +
                '</button>';
        }).join('');

        queue.querySelectorAll('[data-admin-edit-id]').forEach((button) => {
            button.addEventListener('click', () => {
                if (busy) {
                    return;
                }
                const id = Number(button.getAttribute('data-admin-edit-id'));
                if (!Number.isFinite(id)) {
                    return;
                }
                loadArt(id, false);
            });
        });
    }

    async function sendJson(url, options) {
        const requestOptions = options || {};
        const headers = {
            Accept: 'application/json',
            ...csrfHeaders(),
            ...(requestOptions.body ? { 'Content-Type': 'application/json' } : {}),
            ...(requestOptions.headers || {})
        };
        const response = await fetch(url, {
            credentials: 'same-origin',
            ...requestOptions,
            headers
        });
        if (!response.ok) {
            throw new Error('Request failed: ' + response.status);
        }
        if (response.status === 204) {
            return null;
        }
        const text = await response.text();
        return text ? JSON.parse(text) : null;
    }

    async function uploadCoverFile(file) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch('/admin/api/art-requests/cover', {
            method: 'POST',
            credentials: 'same-origin',
            headers: csrfHeaders(),
            body: formData
        });

        if (!response.ok) {
            throw new Error('Cover upload failed: ' + response.status);
        }

        return (await response.text()).trim();
    }

    function buildPayload() {
        return {
            artId: selectedId,
            type: typeInput.value || null,
            name: nameInput.value.trim(),
            author: authorInput.value.trim(),
            description: descriptionInput.value.trim(),
            coverUrl: coverUrlInput.value.trim(),
            createdAt: createdAtInput.value.trim() || null,
            editedAt: toLocalDateTimeString()
        };
    }

    function validateEditor() {
        if (!selectedId) {
            setFeedback('error', 'Select an artwork first.');
            return false;
        }
        if (!typeInput.value) {
            setFeedback('error', 'Type is required.');
            typeInput.focus();
            return false;
        }
        if (!nameInput.value.trim()) {
            setFeedback('error', 'Work name is required.');
            nameInput.focus();
            return false;
        }
        if (!authorInput.value.trim()) {
            setFeedback('error', 'Author is required.');
            authorInput.focus();
            return false;
        }
        return true;
    }

    async function loadArt(id, keepFeedback) {
        if (selectedId !== id) {
            clearPendingCoverDraft();
        }

        selectedId = id;
        renderQueue();
        setBusy(true);
        if (!keepFeedback) {
            setFeedback('loading', 'Loading artwork #' + id + '...');
        }

        try {
            const data = await sendJson('/admin/api/art/' + id, { method: 'GET' });
            const existingItem = current();
            const nextItem = mergeNormalizedItem(existingItem, normalizeItem(data || {}));
            nextItem.id = nextItem.id != null ? nextItem.id : id;
            const index = items.findIndex((item) => item.id === id);
            if (index >= 0) {
                items[index] = nextItem;
            } else {
                items.unshift(nextItem);
            }
            selectedId = id;
            renderQueue();
            renderEditor();
            if (!keepFeedback) {
                setFeedback('success', 'Artwork #' + id + ' is ready to edit.');
            }
        } catch (error) {
            setFeedback('error', 'Failed to load artwork #' + id + '.');
        } finally {
            setBusy(false);
            renderEditor();
        }
    }

    async function searchCatalog() {
        const query = currentSearch();

        if (!query) {
            items = [];
            selectedId = null;
            clearPendingCoverDraft();
            renderQueue();
            setEmptyState('Start with a search', 'Search by work name or author to open an existing catalog item.');
            renderEditor();
            setFeedback(null, 'Enter a name or author to search the catalog.');
            return;
        }

        setBusy(true);
        setFeedback('loading', 'Searching catalog...');

        try {
            const data = await sendJson('/opuscore/api/artworks/search?query=' + encodeURIComponent(query), { method: 'GET' });
            items = Array.isArray(data) ? data.map(normalizeItem) : [];
            if (selectedId && !items.some((item) => item.id === selectedId)) {
                selectedId = null;
                clearPendingCoverDraft();
            }
            renderQueue();
            if (selectedId) {
                await loadArt(selectedId, true);
                setFeedback('success', 'Catalog refreshed around artwork #' + selectedId + '.');
                return;
            }
            setEmptyState(
                items.length ? 'Choose an artwork' : 'Nothing found',
                items.length
                    ? 'Select a search result to open the catalog form.'
                    : 'No artworks matched this search.'
            );
            renderEditor();
            setFeedback(items.length ? null : 'success', items.length
                ? 'Select a result to start editing.'
                : 'No artworks matched this search.');
        } catch (error) {
            items = [];
            selectedId = null;
            clearPendingCoverDraft();
            renderQueue();
            setEmptyState('Search unavailable', 'The editor will appear here after the catalog search loads successfully.');
            renderEditor();
            setFeedback('error', 'Failed to search the catalog.');
        } finally {
            setBusy(false);
            renderEditor();
        }
    }

    function updateCurrentItem(updates) {
        const index = items.findIndex((item) => item.id === selectedId);
        if (index < 0) {
            return;
        }
        items[index] = { ...items[index], ...updates };
    }

    function removeCurrent(successMessage) {
        clearPendingCoverDraft();
        items = items.filter((item) => item.id !== selectedId);
        selectedId = null;
        renderQueue();
        setEmptyState(
            items.length ? 'Choose another artwork' : 'Search again',
            items.length
                ? 'The previous artwork is gone. Select another result from the search list.'
                : 'Search by work name or author to open another catalog item.'
        );
        renderEditor();
        setFeedback('success', successMessage);
    }

    form.addEventListener('submit', (event) => {
        event.preventDefault();
    });

    if (searchForm) {
        searchForm.addEventListener('submit', (event) => {
            event.preventDefault();
            if (busy) {
                return;
            }
            searchCatalog();
        });
    }

    if (searchReset) {
        searchReset.addEventListener('click', () => {
            if (busy) {
                return;
            }
            if (searchInput) {
                searchInput.value = '';
            }
            items = [];
            selectedId = null;
            clearPendingCoverDraft();
            renderQueue();
            setEmptyState('Start with a search', 'Search by work name or author to open an existing catalog item.');
            renderEditor();
            setFeedback(null, 'Enter a name or author to search the catalog.');
        });
    }

    if (coverUpload && coverFileInput) {
        coverUpload.addEventListener('click', () => {
            if (busy || !selectedId) {
                return;
            }
            coverFileInput.value = '';
            coverFileInput.click();
        });

        coverFileInput.addEventListener('change', () => {
            const file = coverFileInput.files && coverFileInput.files[0];
            if (!file || busy || !selectedId) {
                return;
            }
            if (!file.type || !file.type.startsWith('image/')) {
                coverFileInput.value = '';
                setCoverHint('Choose a valid image file.', true);
                setFeedback('error', 'Choose a valid image file.');
                return;
            }
            if (file.size > maxCoverBytes) {
                coverFileInput.value = '';
                setCoverHint('Cover file is too large. Max 5 MB.', true);
                setFeedback('error', 'Cover file is too large. Max 5 MB.');
                return;
            }

            clearPendingCoverDraft();
            pendingCoverDraft = {
                file,
                previewUrl: URL.createObjectURL(file)
            };
            renderPreview(current());
            syncCoverHint();
            syncControls();
            setFeedback('success', 'New cover selected. Save changes to upload it.');
        });
    }

    if (coverRemove) {
        coverRemove.addEventListener('click', () => {
            if (busy || !selectedId || !hasSelectedCover()) {
                return;
            }
            clearPendingCoverDraft();
            coverUrlInput.value = '';
            updateCurrentItem({ coverUrl: '' });
            renderEditor();
            setFeedback('success', 'Cover removed from the draft. Save changes to persist it.');
        });
    }

    save.addEventListener('click', async () => {
        if (busy || !validateEditor()) {
            return;
        }

        setBusy(true);
        setFeedback('loading', 'Saving artwork changes...');

        try {
            if (hasPendingCoverDraft()) {
                setFeedback('loading', 'Uploading replacement cover...');
                const uploadedUrl = await uploadCoverFile(pendingCoverDraft.file);
                coverUrlInput.value = uploadedUrl;
            }

            const payload = buildPayload();
            const response = await sendJson('/admin/api/art/' + selectedId + '/edit', {
                method: 'PATCH',
                body: JSON.stringify(payload)
            });
            const nextItem = mergeNormalizedItem(normalizeItem(payload), normalizeItem(response || {}));
            nextItem.id = nextItem.id != null ? nextItem.id : selectedId;
            nextItem.createdAt = nextItem.createdAt || createdAtInput.value;
            nextItem.editedAt = nextItem.editedAt || payload.editedAt;
            updateCurrentItem(nextItem);
            clearPendingCoverDraft();
            renderQueue();
            renderEditor();
            setFeedback('success', 'Artwork #' + selectedId + ' was saved.');
        } catch (error) {
            setFeedback('error', 'Failed to save artwork changes.');
        } finally {
            setBusy(false);
            renderEditor();
        }
    });

    deleteButton.addEventListener('click', async () => {
        if (busy || !selectedId) {
            return;
        }

        const confirmed = window.confirm('Delete artwork #' + selectedId + '?');
        if (!confirmed) {
            return;
        }

        setBusy(true);
        setFeedback('loading', 'Deleting artwork #' + selectedId + '...');

        try {
            await fetch('/admin/api/art/' + selectedId + '/delete', {
                method: 'DELETE',
                credentials: 'same-origin',
                headers: csrfHeaders()
            }).then((response) => {
                if (!response.ok) {
                    throw new Error('Delete failed: ' + response.status);
                }
            });
            removeCurrent('Artwork #' + selectedId + ' was deleted.');
        } catch (error) {
            setFeedback('error', 'Failed to delete artwork #' + selectedId + '.');
        } finally {
            setBusy(false);
            renderEditor();
        }
    });

    renderQueue();
    setEmptyState('Start with a search', 'Search by work name or author to open an existing catalog item.');
    renderEditor();
    setFeedback(null, 'Enter a name or author to search the catalog.');
})();
