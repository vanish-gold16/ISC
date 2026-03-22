(function () {
    const profileMenu = document.querySelector(".profile-menu");
    const openSettingsButton = document.querySelector("[data-open-header-settings-modal]");
    const settingsModal = document.getElementById("header-settings-modal");
    if (!profileMenu || !openSettingsButton || !settingsModal || settingsModal.dataset.bound === "true") return;
    settingsModal.dataset.bound = "true";

    const LOCAL_SETTINGS_KEY = "isc.userSettingsCache";
    const LEGACY_GRADE_KEY = "scholarHub.gradePreferences";
    const supportedGradeSystems = [
        "Numeric_Grading_1_to_5",
        "Numeric_Grading_1_to_12",
        "Numeric_Grading_1_to_10",
        "Numeric_Grading_1_to_20",
        "Numeric_Grading_5_to_1",
        "Numeric_Grading_6_to_1",
        "Percentage_Grading",
        "Letter_Grading",
        "GPA_4_Point_Scale",
        "Pass_Fail"
    ];

    const sectionButtons = Array.from(settingsModal.querySelectorAll("[data-settings-section-target]"));
    const panes = Array.from(settingsModal.querySelectorAll("[data-settings-pane]"));
    const closeTriggers = settingsModal.querySelectorAll("[data-close-header-settings-modal]");
    const dialog = settingsModal.querySelector(".header-settings-modal__dialog");
    const statusNode = document.getElementById("header-settings-status");
    const resetButton = document.getElementById("header-settings-reset");
    const saveButton = document.getElementById("header-settings-save");
    const scholarHubGradeSystem = document.getElementById("settings-scholarhub-grade-system");
    const appearanceTheme = document.getElementById("settings-appearance-theme");
    const appearanceDensity = document.getElementById("settings-appearance-density");
    const appearanceReduceMotion = document.getElementById("settings-appearance-reduce-motion");
    const notificationsDesktop = document.getElementById("settings-notifications-desktop");
    const notificationsSound = document.getElementById("settings-notifications-sound");

    let savedSettings = defaultSettings();
    let draftSettings = defaultSettings();
    let loadPromise = null;
    let saveInProgress = false;
    let activeSection = "scholarHub";

    function defaultSettings() {
        return {
            scholarHub: {
                preferredGradeSystem: "Numeric_Grading_1_to_5"
            },
            appearance: {
                theme: "system",
                reduceMotion: false,
                density: "comfortable"
            },
            notifications: {
                desktop: true,
                sound: true
            }
        };
    }

    function cloneSettings(settings) {
        return JSON.parse(JSON.stringify(settings));
    }

    function normalizeSettings(settings) {
        const defaults = defaultSettings();
        const source = settings && typeof settings === "object" ? settings : {};
        const normalized = cloneSettings(defaults);

        const preferredGradeSystem = source?.scholarHub?.preferredGradeSystem;
        if (supportedGradeSystems.includes(preferredGradeSystem)) {
            normalized.scholarHub.preferredGradeSystem = preferredGradeSystem;
        }

        const theme = source?.appearance?.theme;
        if (theme === "system" || theme === "light" || theme === "dark") {
            normalized.appearance.theme = theme;
        }

        const density = source?.appearance?.density;
        if (density === "comfortable" || density === "compact") {
            normalized.appearance.density = density;
        }

        if (typeof source?.appearance?.reduceMotion === "boolean") {
            normalized.appearance.reduceMotion = source.appearance.reduceMotion;
        }

        if (typeof source?.notifications?.desktop === "boolean") {
            normalized.notifications.desktop = source.notifications.desktop;
        }

        if (typeof source?.notifications?.sound === "boolean") {
            normalized.notifications.sound = source.notifications.sound;
        }

        return normalized;
    }

    function settingsEqual(left, right) {
        return JSON.stringify(left) === JSON.stringify(right);
    }

    function getCsrfHeaders() {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
        const headerName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
        if (!token || !headerName) return {};
        return { [headerName]: token };
    }

    async function requestJson(url, options = {}) {
        const headers = {
            "Content-Type": "application/json",
            ...getCsrfHeaders(),
            ...(options.headers || {})
        };
        const response = await fetch(url, { ...options, headers });
        if (!response.ok) {
            const fallback = await response.text();
            throw new Error(fallback || `Request failed: ${response.status}`);
        }
        if (response.status === 204) return null;
        const contentType = response.headers.get("content-type") || "";
        return contentType.includes("application/json") ? response.json() : null;
    }

    function readLocalSettings() {
        try {
            const raw = window.localStorage.getItem(LOCAL_SETTINGS_KEY);
            if (!raw) return null;
            return normalizeSettings(JSON.parse(raw));
        } catch (error) {
            return null;
        }
    }

    function writeLocalSettings(settings) {
        try {
            window.localStorage.setItem(LOCAL_SETTINGS_KEY, JSON.stringify(settings));
        } catch (error) {
            console.error(error);
        }
        syncScholarHubLocalPreference(settings);
    }

    function syncScholarHubLocalPreference(settings) {
        try {
            window.localStorage.setItem(LEGACY_GRADE_KEY, JSON.stringify({
                preferredSystem: settings.scholarHub.preferredGradeSystem
            }));
        } catch (error) {
            console.error(error);
        }
    }

    function dispatchSettingsUpdated(settings) {
        window.dispatchEvent(new CustomEvent("isc:settings-updated", {
            detail: cloneSettings(settings)
        }));
    }

    function setStatus(message, tone) {
        if (!statusNode) return;
        statusNode.textContent = message;
        statusNode.classList.toggle("is-error", tone === "error");
        statusNode.classList.toggle("is-success", tone === "success");
    }

    function setActiveSection(section) {
        activeSection = section;
        sectionButtons.forEach((button) => {
            button.classList.toggle("is-active", button.dataset.settingsSectionTarget === section);
        });
        panes.forEach((pane) => {
            pane.classList.toggle("is-active", pane.dataset.settingsPane === section);
        });
    }

    function renderForm() {
        const normalized = normalizeSettings(draftSettings);
        scholarHubGradeSystem.value = normalized.scholarHub.preferredGradeSystem;
        appearanceTheme.value = normalized.appearance.theme;
        appearanceDensity.value = normalized.appearance.density;
        appearanceReduceMotion.checked = normalized.appearance.reduceMotion;
        notificationsDesktop.checked = normalized.notifications.desktop;
        notificationsSound.checked = normalized.notifications.sound;
        resetButton.disabled = settingsEqual(savedSettings, normalized) || saveInProgress;
        saveButton.disabled = saveInProgress;
        saveButton.textContent = saveInProgress ? "Saving..." : "Save changes";
    }

    function collectForm() {
        return normalizeSettings({
            scholarHub: {
                preferredGradeSystem: scholarHubGradeSystem.value
            },
            appearance: {
                theme: appearanceTheme.value,
                reduceMotion: appearanceReduceMotion.checked,
                density: appearanceDensity.value
            },
            notifications: {
                desktop: notificationsDesktop.checked,
                sound: notificationsSound.checked
            }
        });
    }

    function syncDraftFromForm() {
        draftSettings = collectForm();
        renderForm();
        if (settingsEqual(savedSettings, draftSettings)) {
            setStatus("All changes are already synced.", "success");
            return;
        }
        setStatus("Unsaved changes. Save once to sync this JSON object to your account.", null);
    }

    async function loadSettings() {
        if (loadPromise) return loadPromise;

        const cached = readLocalSettings();
        if (cached) {
            savedSettings = cloneSettings(cached);
            draftSettings = cloneSettings(cached);
            renderForm();
            setStatus("Loaded cached settings. Refreshing from server...", null);
        } else {
            savedSettings = defaultSettings();
            draftSettings = defaultSettings();
            renderForm();
            setStatus("Loading settings from server...", null);
        }

        loadPromise = requestJson("/api/settings/me", { method: "GET" })
            .then((response) => {
                const normalized = normalizeSettings(response);
                savedSettings = cloneSettings(normalized);
                draftSettings = cloneSettings(normalized);
                writeLocalSettings(normalized);
                dispatchSettingsUpdated(normalized);
                renderForm();
                setStatus("Settings synced with your account.", "success");
                return normalized;
            })
            .catch((error) => {
                console.error(error);
                const fallback = readLocalSettings() || defaultSettings();
                savedSettings = cloneSettings(fallback);
                draftSettings = cloneSettings(fallback);
                renderForm();
                setStatus("Server sync failed. Using cached settings until the next successful load.", "error");
                return fallback;
            })
            .finally(() => {
                loadPromise = null;
            });

        return loadPromise;
    }

    async function saveSettings() {
        if (saveInProgress) return;

        saveInProgress = true;
        draftSettings = collectForm();
        renderForm();
        setStatus("Saving settings to your account...", null);

        try {
            const response = await requestJson("/api/settings/me", {
                method: "PUT",
                body: JSON.stringify(draftSettings)
            });
            const normalized = normalizeSettings(response);
            savedSettings = cloneSettings(normalized);
            draftSettings = cloneSettings(normalized);
            writeLocalSettings(normalized);
            dispatchSettingsUpdated(normalized);
            renderForm();
            setStatus("Settings saved and synced across devices.", "success");
        } catch (error) {
            console.error(error);
            renderForm();
            setStatus("Failed to save settings. Your last synced version is still kept locally.", "error");
        } finally {
            saveInProgress = false;
            renderForm();
        }
    }

    function resetDraft() {
        draftSettings = cloneSettings(savedSettings);
        renderForm();
        setStatus("Reverted unsaved changes.", null);
    }

    function openModal() {
        profileMenu.removeAttribute("open");
        settingsModal.hidden = false;
        settingsModal.setAttribute("aria-hidden", "false");
        requestAnimationFrame(() => {
            settingsModal.classList.add("is-visible");
        });
        void loadSettings();
    }

    function closeModal() {
        settingsModal.classList.remove("is-visible");
        window.setTimeout(() => {
            settingsModal.hidden = true;
            settingsModal.setAttribute("aria-hidden", "true");
        }, 220);
    }

    openSettingsButton.addEventListener("click", (event) => {
        event.preventDefault();
        event.stopPropagation();
        openModal();
    });

    closeTriggers.forEach((trigger) => {
        trigger.addEventListener("click", () => {
            closeModal();
        });
    });

    dialog?.addEventListener("click", (event) => {
        event.stopPropagation();
    });

    sectionButtons.forEach((button) => {
        button.addEventListener("click", () => {
            setActiveSection(button.dataset.settingsSectionTarget || "scholarHub");
        });
    });

    [
        scholarHubGradeSystem,
        appearanceTheme,
        appearanceDensity,
        appearanceReduceMotion,
        notificationsDesktop,
        notificationsSound
    ].forEach((field) => {
        field?.addEventListener("change", syncDraftFromForm);
    });

    resetButton?.addEventListener("click", resetDraft);
    saveButton?.addEventListener("click", () => {
        void saveSettings();
    });

    document.addEventListener("click", (event) => {
        if (!profileMenu.hasAttribute("open")) return;
        if (profileMenu.contains(event.target)) return;
        profileMenu.removeAttribute("open");
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            if (!settingsModal.hidden) {
                closeModal();
                return;
            }
            profileMenu.removeAttribute("open");
        }
    });

    setActiveSection(activeSection);
    const initialSettings = readLocalSettings() || defaultSettings();
    savedSettings = cloneSettings(initialSettings);
    draftSettings = cloneSettings(initialSettings);
    renderForm();
})();
