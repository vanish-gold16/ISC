document.addEventListener("DOMContentLoaded", () => {
    const dayOrder = {
        MONDAY: 0,
        TUESDAY: 1,
        WEDNESDAY: 2,
        THURSDAY: 3,
        FRIDAY: 4,
        SATURDAY: 5,
        SUNDAY: 6
    };

    const dayLabels = {
        MONDAY: "Monday",
        TUESDAY: "Tuesday",
        WEDNESDAY: "Wednesday",
        THURSDAY: "Thursday",
        FRIDAY: "Friday",
        SATURDAY: "Saturday",
        SUNDAY: "Sunday"
    };

    const priorityMap = new Map([
        ["_00FF00", { label: "Low priority", color: "#00FF00" }],
        ["_FFFF00", { label: "Medium priority", color: "#FFFF00" }],
        ["_FF0000", { label: "High priority", color: "#FF0000" }]
    ]);

    const homeworkModal = document.getElementById("homework-modal");
    const homeworkTitleInput = document.getElementById("homework-title");
    const homeworkDetailsInput = document.getElementById("homework-details");
    const homeworkPriorityInput = document.getElementById("homework-priority");
    const homeworkStatusInput = document.getElementById("homework-status");
    const homeworkContext = document.getElementById("homework-modal-context");
    const homeworkSaveButton = document.getElementById("homework-save");
    const homeworkDeleteButton = document.getElementById("homework-delete");
    const closeHomeworkTriggers = Array.from(document.querySelectorAll("[data-close-homework-modal]"));
    const toastStack = document.querySelector("[data-homework-toasts]");
    const subjectModal = document.getElementById("subject-modal");
    const subjectModalPreview = document.getElementById("subject-modal-preview");
    const subjectModalTitle = document.getElementById("subject-modal-title");
    const subjectModalShortName = document.getElementById("subject-modal-short-name");
    const subjectModalRoom = document.getElementById("subject-modal-room");
    const subjectModalTeacher = document.getElementById("subject-modal-teacher");
    const subjectModalLesson = document.getElementById("subject-modal-lesson");
    const closeSubjectTriggers = Array.from(document.querySelectorAll("[data-close-subject-modal]"));

    const homeworkCache = new Map();
    let activeLessonCell = null;
    let subjectModalClosing = false;

    function formatDateLabel(date) {
        return new Intl.DateTimeFormat("ru-RU", {
            day: "2-digit",
            month: "2-digit"
        }).format(date);
    }

    function formatFullDateLabel(date) {
        return new Intl.DateTimeFormat("ru-RU", {
            weekday: "long",
            day: "2-digit",
            month: "2-digit"
        }).format(date);
    }

    function toIsoDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    }

    function getMonday(date) {
        const monday = new Date(date);
        const day = monday.getDay();
        const diff = day === 0 ? -6 : 1 - day;
        monday.setHours(0, 0, 0, 0);
        monday.setDate(monday.getDate() + diff);
        return monday;
    }

    function addDays(date, days) {
        const nextDate = new Date(date);
        nextDate.setDate(nextDate.getDate() + days);
        return nextDate;
    }

    function getPriorityColor(priority) {
        if (!priority) return null;
        if (priorityMap.has(priority)) return priorityMap.get(priority).color;
        if (priority.startsWith("_")) return `#${priority.slice(1)}`;
        if (priority.startsWith("#")) return priority;
        return null;
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
        if (contentType.includes("application/json")) return response.json();
        return null;
    }

    function makeCacheKey(weekStart, daySubjectId) {
        if (!weekStart || !daySubjectId) return null;
        return `${weekStart}|${daySubjectId}`;
    }

    function getLessonIdentifiers(cell) {
        if (!cell) return null;
        const weekStart = cell.dataset.weekStart || "";
        const daySubjectId = cell.dataset.daySubjectId || "";
        return { weekStart, daySubjectId };
    }

    function getCachedHomework(cell) {
        const identifiers = getLessonIdentifiers(cell);
        if (!identifiers) return null;
        const key = makeCacheKey(identifiers.weekStart, identifiers.daySubjectId);
        return key ? homeworkCache.get(key) || null : null;
    }

    function setHomeworkIndicator(cell, homework) {
        const indicator = cell.querySelector("[data-homework-indicator]");
        if (!indicator) return;

        indicator.classList.remove("hidden");
        indicator.style.removeProperty("--homework-color");

        if (!homework || (!homework.title && !homework.details)) {
            indicator.classList.add("hidden");
            return;
        }

        const color = getPriorityColor(homework.priority);
        if (color) indicator.style.setProperty("--homework-color", color);
    }

    function refreshHomeworkIndicators(scope) {
        const root = scope || document;
        root.querySelectorAll(".hub-timetable__cell--filled[data-week-start]").forEach((cell) => {
            const homework = getCachedHomework(cell);
            setHomeworkIndicator(cell, homework);
        });
    }

    function showToast(type, message) {
        if (!toastStack) return;
        const toast = document.createElement("div");
        toast.className = `homework-toast homework-toast--${type}`;
        toast.innerHTML = `
            <p class="homework-toast__title">Homework</p>
            <p class="homework-toast__message">${message}</p>
        `;
        toastStack.appendChild(toast);

        window.setTimeout(() => {
            toast.classList.add("is-leaving");
            window.setTimeout(() => toast.remove(), 220);
        }, 2600);
    }

    /* ─────────────────────────────────────────────────
       SUBJECT MODAL — smooth open / close
    ───────────────────────────────────────────────── */

    function populateSubjectModalFields(cell) {
        const subjectName = cell.dataset.subjectName || "Subject";
        const shortName = cell.dataset.subjectShortName || subjectName;
        const room = cell.dataset.subjectRoom
            || cell.querySelector(".hub-timetable__room")?.textContent?.trim()
            || "Room not set";
        const teacher = cell.dataset.subjectTeacher
            || cell.querySelector(".hub-timetable__teacher")?.textContent?.trim()
            || "Not assigned";
        const lessonOrder = cell.dataset.lessonOrder || "";
        const dayKey = cell.dataset.dayKey;
        const lessonDate = cell.dataset.lessonDate ? new Date(cell.dataset.lessonDate) : null;
        const dateLabel = lessonDate ? formatFullDateLabel(lessonDate) : (dayLabels[dayKey] || "");

        if (subjectModalTitle) subjectModalTitle.textContent = subjectName;
        if (subjectModalShortName) subjectModalShortName.textContent = shortName || "No short name";
        if (subjectModalRoom) subjectModalRoom.textContent = room;
        if (subjectModalTeacher) subjectModalTeacher.textContent = teacher;
        if (subjectModalLesson) {
            const lbl = lessonOrder ? `Lesson #${lessonOrder}` : "Lesson";
            subjectModalLesson.textContent = dateLabel ? `${lbl} • ${dateLabel}` : lbl;
        }

        const accent = getComputedStyle(cell).getPropertyValue("--preview-accent").trim();
        subjectModal.style.setProperty("--subject-color", accent || "#7aa2ff");
    }

    function closeSubjectModal() {
        if (!subjectModal || subjectModalClosing) return;
        subjectModalClosing = true;

        const dialog = subjectModal.querySelector(".subject-detail-modal__dialog");
        const backdrop = subjectModal.querySelector(".subject-modal__backdrop");

        if (backdrop) {
            backdrop.style.transition = "opacity 280ms ease";
            backdrop.style.opacity = "0";
        }
        if (dialog) {
            dialog.style.transition = "opacity 240ms ease, transform 240ms cubic-bezier(0.4, 0, 1, 1)";
            dialog.style.opacity = "0";
            dialog.style.transform = "translateX(18px)";
        }
        if (subjectModalPreview) {
            subjectModalPreview.style.transition = "opacity 200ms ease";
            subjectModalPreview.style.opacity = "0";
        }

        window.setTimeout(() => {
            subjectModal.classList.add("hidden");
            subjectModal.setAttribute("aria-hidden", "true");
            subjectModalClosing = false;

            // Reset all inline styles
            if (subjectModalPreview) {
                subjectModalPreview.removeAttribute("style");
                subjectModalPreview.innerHTML = "";
            }
            if (dialog) {
                dialog.removeAttribute("style");
            }
            if (backdrop) {
                backdrop.removeAttribute("style");
            }
        }, 290);
    }

    function animateSubjectOpen(cell) {
        if (!cell) return;
        if (cell.classList.contains("is-animating")) return;
        if (subjectModal && !subjectModal.classList.contains("hidden")) return;

        const rect = cell.getBoundingClientRect();

        // ── Animation parameters ──────────────────────
        const SCALE    = 1.13;
        const TARGET_X = 80;   // px from left viewport edge
        const TARGET_Y = 72;   // px from top viewport edge
        const ANIM_MS  = 720;  // card travel duration

        const finalW = rect.width * SCALE;
        const finalH = rect.height * SCALE;

        // ── Populate modal data ───────────────────────
        populateSubjectModalFields(cell);

        // ── Create animated clone ─────────────────────
        const clone = cell.cloneNode(true);
        Object.assign(clone.style, {
            position:        "fixed",
            left:            `${rect.left}px`,
            top:             `${rect.top}px`,
            width:           `${rect.width}px`,
            height:          `${rect.height}px`,
            margin:          "0",
            zIndex:          "9998",
            pointerEvents:   "none",
            transformOrigin: "top left",
            transition:      "none",
            willChange:      "transform, box-shadow",
            boxShadow:       "0 6px 20px rgba(17,32,63,0.13)",
            borderRadius:    "16px",
        });
        document.body.appendChild(clone);

        // Hide the original cell (fades out via CSS class)
        cell.classList.add("is-animating");

        // ── Reveal modal overlay (but visually hidden) ─
        subjectModal.classList.remove("hidden");
        subjectModal.setAttribute("aria-hidden", "false");

        const dialog   = subjectModal.querySelector(".subject-detail-modal__dialog");
        const backdrop = subjectModal.querySelector(".subject-modal__backdrop");

        // Set backdrop starting state
        if (backdrop) {
            backdrop.style.opacity    = "0";
            backdrop.style.transition = "none";
        }

        // Determine dialog position: right of card if space allows, else below
        const GAP          = 22;
        const dialogLeft   = TARGET_X + finalW + GAP;
        const rightSpace   = window.innerWidth - dialogLeft - 40;
        const belowTop     = TARGET_Y + finalH + GAP;
        const belowSpace   = window.innerHeight - belowTop - 40;

        let dTop, dLeft, dWidth, dMaxH;
        if (rightSpace >= 300) {
            dTop   = TARGET_Y;
            dLeft  = dialogLeft;
            dWidth = Math.min(660, rightSpace);
            dMaxH  = `calc(100vh - ${TARGET_Y + 40}px)`;
        } else {
            dTop   = belowTop;
            dLeft  = TARGET_X;
            dWidth = Math.min(660, window.innerWidth - TARGET_X - 40);
            dMaxH  = `calc(100vh - ${belowTop + 40}px)`;
        }

        if (dialog) {
            Object.assign(dialog.style, {
                position:  "fixed",
                top:       `${dTop}px`,
                left:      `${dLeft}px`,
                width:     `${dWidth}px`,
                maxHeight: dMaxH,
                opacity:   "0",
                transform: "translateX(26px)",
                transition:"none",
            });
        }

        // Floating preview starting state
        if (subjectModalPreview) {
            subjectModalPreview.innerHTML = "";
            Object.assign(subjectModalPreview.style, {
                left:      `${TARGET_X}px`,
                top:       `${TARGET_Y}px`,
                width:     `${finalW}px`,
                height:    `${finalH}px`,
                opacity:   "0",
                transition:"none",
            });
        }

        // ── Kick off animation (two rAF to bypass paint flush) ───
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                const tx = TARGET_X - rect.left;
                const ty = TARGET_Y - rect.top;

                // Card clone travels to upper-left
                clone.style.transition = [
                    `transform ${ANIM_MS}ms cubic-bezier(0.25, 0.85, 0.2, 1)`,
                    `box-shadow ${ANIM_MS}ms ease`,
                ].join(", ");
                clone.style.transform  = `translate(${tx}px, ${ty}px) scale(${SCALE})`;
                clone.style.boxShadow  = "0 22px 60px rgba(17,32,63,0.22)";

                // Backdrop fades in simultaneously
                if (backdrop) {
                    backdrop.style.transition = "opacity 480ms ease";
                    backdrop.style.opacity    = "1";
                }
            });
        });

        // ── Dialog slides in while card is still moving (~55% through) ──
        window.setTimeout(() => {
            if (dialog) {
                dialog.style.transition = [
                    "opacity 380ms cubic-bezier(0.25, 0.8, 0.25, 1)",
                    "transform 380ms cubic-bezier(0.25, 0.8, 0.25, 1)",
                ].join(", ");
                dialog.style.opacity   = "1";
                dialog.style.transform = "translateX(0)";
            }
        }, Math.round(ANIM_MS * 0.55));

        // ── Animation complete: swap clone → real preview ────────────
        window.setTimeout(() => {
            clone.remove();
            cell.classList.remove("is-animating");

            if (subjectModalPreview) {
                const preview = cell.cloneNode(true);
                Object.assign(preview.style, {
                    width:        "100%",
                    height:       "100%",
                    display:      "block",
                    cursor:       "default",
                    pointerEvents:"none",
                    minHeight:    "unset",
                    borderRadius: "16px",
                    boxSizing:    "border-box",
                });
                subjectModalPreview.appendChild(preview);
                subjectModalPreview.style.transition = "opacity 120ms ease";
                subjectModalPreview.style.opacity    = "1";
            }
        }, ANIM_MS + 50);
    }

    /* ─────────────────────────────────────────────────
       HOMEWORK MODAL
    ───────────────────────────────────────────────── */

    async function loadHomeworkForWeek(weekStart, scope) {
        if (!weekStart) return;
        try {
            const data = await requestJson(`/scholar-hub/homework?weekStart=${encodeURIComponent(weekStart)}`, {
                method: "GET"
            });
            const list = Array.isArray(data) ? data : [];
            const prefix = `${weekStart}|`;
            Array.from(homeworkCache.keys()).forEach((key) => {
                if (key.startsWith(prefix)) homeworkCache.delete(key);
            });
            list.forEach((item) => {
                if (!item || !item.daySubjectId) return;
                const key = makeCacheKey(weekStart, item.daySubjectId);
                if (key) homeworkCache.set(key, item);
            });
            refreshHomeworkIndicators(scope);
        } catch (error) {
            console.error(error);
            showToast("error", "Failed to load homework for the selected week.");
        }
    }

    function closeHomeworkModal() {
        if (!homeworkModal) return;
        homeworkModal.classList.add("hidden");
        homeworkModal.setAttribute("aria-hidden", "true");
        activeLessonCell = null;
    }

    function openHomeworkModal(cell) {
        if (!homeworkModal || !homeworkTitleInput || !homeworkDetailsInput || !homeworkPriorityInput || !homeworkContext) return;

        activeLessonCell = cell;
        const homework = getCachedHomework(cell);
        const subjectName = cell.dataset.subjectShortName || cell.dataset.subjectName || "Lesson";
        const lessonOrder = cell.dataset.lessonOrder || "";
        const lessonDate = cell.dataset.lessonDate ? new Date(cell.dataset.lessonDate) : null;
        const dateLabel = lessonDate ? formatFullDateLabel(lessonDate) : (dayLabels[cell.dataset.dayKey] || "Current day");

        homeworkContext.textContent = `${subjectName} • lesson ${lessonOrder} • ${dateLabel}`;
        homeworkTitleInput.value = homework ? homework.title || "" : "";
        homeworkDetailsInput.value = homework ? homework.details || "" : "";
        homeworkPriorityInput.value = homework?.priority || homeworkPriorityInput.value || "_00FF00";
        if (homeworkStatusInput) {
            homeworkStatusInput.value = homework?.status || homeworkStatusInput.value || "Pending";
        }
        if (homeworkDeleteButton) {
            homeworkDeleteButton.disabled = !homework;
        }

        homeworkModal.classList.remove("hidden");
        homeworkModal.setAttribute("aria-hidden", "false");
        window.setTimeout(() => homeworkTitleInput.focus(), 0);
    }

    async function saveHomework() {
        if (!activeLessonCell || !homeworkTitleInput || !homeworkDetailsInput || !homeworkPriorityInput) return;

        const identifiers = getLessonIdentifiers(activeLessonCell);
        if (!identifiers || !identifiers.weekStart) {
            showToast("error", "Homework week is missing. Update schedule context first.");
            return;
        }
        if (!identifiers.daySubjectId) {
            showToast("error", "Day subject id is missing. Backend needs to expose it in schedule cells.");
            return;
        }

        const title = String(homeworkTitleInput.value || "").trim();
        const details = String(homeworkDetailsInput.value || "").trim();
        const priority = homeworkPriorityInput.value || "_00FF00";
        const status = homeworkStatusInput ? (homeworkStatusInput.value || "Pending") : "Pending";

        if (!title) {
            showToast("error", "Homework title is required.");
            return;
        }

        const payload = {
            title,
            details,
            priority,
            daySubjectId: Number(identifiers.daySubjectId),
            status,
            weekStart: identifiers.weekStart
        };

        const cached = getCachedHomework(activeLessonCell);
        const homeworkId = cached && cached.id ? cached.id : null;

        try {
            if (homeworkId) {
                await requestJson(`/scholar-hub/homework/${encodeURIComponent(homeworkId)}`, {
                    method: "PUT",
                    body: JSON.stringify(payload)
                });
            } else {
                await requestJson("/scholar-hub/homework", {
                    method: "POST",
                    body: JSON.stringify(payload)
                });
            }
            await loadHomeworkForWeek(identifiers.weekStart, activeLessonCell.closest(".hub-timetable"));
            showToast("success", "Homework saved.");
            closeHomeworkModal();
        } catch (error) {
            console.error(error);
            showToast("error", "Failed to save homework.");
        }
    }

    async function deleteHomework() {
        if (!activeLessonCell) return;

        const identifiers = getLessonIdentifiers(activeLessonCell);
        if (!identifiers || !identifiers.weekStart) {
            showToast("error", "Homework week is missing. Update schedule context first.");
            return;
        }

        const cached = getCachedHomework(activeLessonCell);
        if (!cached || !cached.id) {
            showToast("error", "Homework id is missing. Backend needs to return it for delete.");
            return;
        }

        try {
            await requestJson(`/scholar-hub/homework/${encodeURIComponent(cached.id)}`, {
                method: "DELETE"
            });
            await loadHomeworkForWeek(identifiers.weekStart, activeLessonCell.closest(".hub-timetable"));
            showToast("success", "Homework deleted.");
            closeHomeworkModal();
        } catch (error) {
            console.error(error);
            showToast("error", "Failed to delete homework.");
        }
    }

    /* ─────────────────────────────────────────────────
       WEEK NAVIGATION
    ───────────────────────────────────────────────── */

    document.querySelectorAll("[data-week-nav]").forEach((weekNav) => {
        const rangeLabel = weekNav.querySelector("[data-week-range]");
        const timetable = weekNav.parentElement ? weekNav.parentElement.querySelector(".hub-timetable") : null;
        if (!rangeLabel || !timetable) return;

        const dayIndexes = Array.from(timetable.querySelectorAll("[data-day-key]"))
            .map((element) => dayOrder[element.dataset.dayKey])
            .filter((value) => Number.isInteger(value))
            .sort((left, right) => left - right);

        const firstDayIndex = dayIndexes.length > 0 ? dayIndexes[0] : 0;
        const lastDayIndex  = dayIndexes.length > 0 ? dayIndexes[dayIndexes.length - 1] : 5;
        const url = new URL(window.location.href);
        let weekOffset = Number.parseInt(url.searchParams.get("weekOffset") || "0", 10);
        if (Number.isNaN(weekOffset)) weekOffset = 0;

        function applyWeekContext() {
            const monday = getMonday(new Date());
            monday.setDate(monday.getDate() + weekOffset * 7);

            const start = addDays(monday, firstDayIndex);
            const end   = addDays(monday, lastDayIndex);
            rangeLabel.textContent = `${formatDateLabel(start)}-${formatDateLabel(end)}`;

            timetable.querySelectorAll(".hub-timetable__cell--filled").forEach((cell) => {
                const dayIndex = dayOrder[cell.dataset.dayKey];
                if (!Number.isInteger(dayIndex)) return;

                const lessonDate = addDays(monday, dayIndex);
                cell.dataset.weekStart  = toIsoDate(monday);
                cell.dataset.lessonDate = lessonDate.toISOString();
            });

            void loadHomeworkForWeek(toIsoDate(monday), timetable);
            refreshHomeworkIndicators(timetable);
        }

        function syncUrl() {
            if (weekOffset === 0) {
                url.searchParams.delete("weekOffset");
            } else {
                url.searchParams.set("weekOffset", String(weekOffset));
            }
            window.history.replaceState({}, "", url);
        }

        weekNav.querySelectorAll("[data-week-shift]").forEach((button) => {
            button.addEventListener("click", () => {
                const shift = Number.parseInt(button.dataset.weekShift || "0", 10);
                if (Number.isNaN(shift) || shift === 0) return;
                weekOffset += shift;
                applyWeekContext();
                syncUrl();
            });
        });

        applyWeekContext();
        syncUrl();
    });

    /* ─────────────────────────────────────────────────
       EVENT LISTENERS
    ───────────────────────────────────────────────── */

    document.querySelectorAll("[data-homework-trigger]").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.preventDefault();
            event.stopPropagation();
            const lessonCell = button.closest(".hub-timetable__cell--filled");
            if (!lessonCell) return;
            openHomeworkModal(lessonCell);
        });
    });

    // Click on a filled lesson cell → animate open
    document.querySelectorAll(".hub-timetable__cell--filled").forEach((cell) => {
        cell.addEventListener("click", (event) => {
            if (event.target.closest("a, button, input, textarea, select, label")) return;
            animateSubjectOpen(cell);
        });
    });

    closeHomeworkTriggers.forEach((trigger) => {
        trigger.addEventListener("click", () => closeHomeworkModal());
    });

    closeSubjectTriggers.forEach((trigger) => {
        trigger.addEventListener("click", () => closeSubjectModal());
    });

    if (homeworkSaveButton) {
        homeworkSaveButton.addEventListener("click", () => { void saveHomework(); });
    }

    if (homeworkDeleteButton) {
        homeworkDeleteButton.addEventListener("click", () => { void deleteHomework(); });
    }

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            if (homeworkModal && !homeworkModal.classList.contains("hidden")) {
                closeHomeworkModal();
            }
            if (subjectModal && !subjectModal.classList.contains("hidden")) {
                closeSubjectModal();
            }
        }
    });
});