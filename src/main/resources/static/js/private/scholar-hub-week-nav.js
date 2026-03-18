document.addEventListener("DOMContentLoaded", () => {
    const dayOrder = {
        MONDAY: 0, TUESDAY: 1, WEDNESDAY: 2, THURSDAY: 3,
        FRIDAY: 4, SATURDAY: 5, SUNDAY: 6
    };

    const dayLabels = {
        MONDAY: "Monday", TUESDAY: "Tuesday", WEDNESDAY: "Wednesday",
        THURSDAY: "Thursday", FRIDAY: "Friday", SATURDAY: "Saturday", SUNDAY: "Sunday"
    };

    const priorityMap = new Map([
        ["_00FF00", { label: "Low priority",    color: "#00FF00" }],
        ["_FFFF00", { label: "Medium priority", color: "#FFFF00" }],
        ["_FF0000", { label: "High priority",   color: "#FF0000" }]
    ]);

    const homeworkModal        = document.getElementById("homework-modal");
    const homeworkTitleInput   = document.getElementById("homework-title");
    const homeworkDetailsInput = document.getElementById("homework-details");
    const homeworkPriorityInput= document.getElementById("homework-priority");
    const homeworkStatusInput  = document.getElementById("homework-status");
    const homeworkContext      = document.getElementById("homework-modal-context");
    const homeworkSaveButton   = document.getElementById("homework-save");
    const homeworkDeleteButton = document.getElementById("homework-delete");
    const closeHomeworkTriggers= Array.from(document.querySelectorAll("[data-close-homework-modal]"));
    const toastStack           = document.querySelector("[data-homework-toasts]");
    const subjectModal         = document.getElementById("subject-modal");
    const subjectModalPreview  = document.getElementById("subject-modal-preview");
    const subjectModalTitle    = document.getElementById("subject-modal-title");
    const subjectModalShortName= document.getElementById("subject-modal-short-name");
    const subjectModalRoom     = document.getElementById("subject-modal-room");
    const subjectModalTeacher  = document.getElementById("subject-modal-teacher");
    const subjectModalLesson   = document.getElementById("subject-modal-lesson");
    const closeSubjectTriggers = Array.from(document.querySelectorAll("[data-close-subject-modal]"));

    const homeworkCache = new Map();
    let activeLessonCell = null;

    // Stores { cell, clone, rect } while the subject modal is open
    let activeCardAnim = null;
    let cardAnimBusy   = false;

    /* ─────────────────────────────────────────────────
       UTILS
    ───────────────────────────────────────────────── */

    function formatDateLabel(date) {
        return new Intl.DateTimeFormat("ru-RU", { day: "2-digit", month: "2-digit" }).format(date);
    }

    function formatFullDateLabel(date) {
        return new Intl.DateTimeFormat("ru-RU", { weekday: "long", day: "2-digit", month: "2-digit" }).format(date);
    }

    function toIsoDate(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, "0");
        const d = String(date.getDate()).padStart(2, "0");
        return `${y}-${m}-${d}`;
    }

    function getMonday(date) {
        const monday = new Date(date);
        const day = monday.getDay();
        monday.setHours(0, 0, 0, 0);
        monday.setDate(monday.getDate() + (day === 0 ? -6 : 1 - day));
        return monday;
    }

    function addDays(date, days) {
        const d = new Date(date);
        d.setDate(d.getDate() + days);
        return d;
    }

    function getPriorityColor(priority) {
        if (!priority) return null;
        if (priorityMap.has(priority)) return priorityMap.get(priority).color;
        if (priority.startsWith("_")) return `#${priority.slice(1)}`;
        if (priority.startsWith("#")) return priority;
        return null;
    }

    function getCsrfHeaders() {
        const token      = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
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
        const ct = response.headers.get("content-type") || "";
        if (ct.includes("application/json")) return response.json();
        return null;
    }

    function makeCacheKey(weekStart, daySubjectId) {
        if (!weekStart || !daySubjectId) return null;
        return `${weekStart}|${daySubjectId}`;
    }

    function getLessonIdentifiers(cell) {
        if (!cell) return null;
        return { weekStart: cell.dataset.weekStart || "", daySubjectId: cell.dataset.daySubjectId || "" };
    }

    function getCachedHomework(cell) {
        const ids = getLessonIdentifiers(cell);
        if (!ids) return null;
        const key = makeCacheKey(ids.weekStart, ids.daySubjectId);
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
        (scope || document).querySelectorAll(".hub-timetable__cell--filled[data-week-start]").forEach((cell) => {
            setHomeworkIndicator(cell, getCachedHomework(cell));
        });
    }

    function showToast(type, message) {
        if (!toastStack) return;
        const toast = document.createElement("div");
        toast.className = `homework-toast homework-toast--${type}`;
        toast.innerHTML = `<p class="homework-toast__title">Homework</p><p class="homework-toast__message">${message}</p>`;
        toastStack.appendChild(toast);
        window.setTimeout(() => {
            toast.classList.add("is-leaving");
            window.setTimeout(() => toast.remove(), 220);
        }, 2600);
    }

    /* ─────────────────────────────────────────────────
       SUBJECT MODAL — populate fields
    ───────────────────────────────────────────────── */

    function populateSubjectModalFields(cell) {
        const subjectName = cell.dataset.subjectName || "Subject";
        const shortName   = cell.dataset.subjectShortName || subjectName;
        const room        = cell.dataset.subjectRoom
            || cell.querySelector(".hub-timetable__room")?.textContent?.trim()
            || "Room not set";
        const teacher     = cell.dataset.subjectTeacher
            || cell.querySelector(".hub-timetable__teacher")?.textContent?.trim()
            || "Not assigned";
        const lessonOrder = cell.dataset.lessonOrder || "";
        const dayKey      = cell.dataset.dayKey;
        const lessonDate  = cell.dataset.lessonDate ? new Date(cell.dataset.lessonDate) : null;
        const dateLabel   = lessonDate ? formatFullDateLabel(lessonDate) : (dayLabels[dayKey] || "");

        if (subjectModalTitle)      subjectModalTitle.textContent     = subjectName;
        if (subjectModalShortName)  subjectModalShortName.textContent = shortName || "No short name";
        if (subjectModalRoom)       subjectModalRoom.textContent      = room;
        if (subjectModalTeacher)    subjectModalTeacher.textContent   = teacher;
        if (subjectModalLesson) {
            const lbl = lessonOrder ? `Lesson #${lessonOrder}` : "Lesson";
            subjectModalLesson.textContent = dateLabel ? `${lbl} • ${dateLabel}` : lbl;
        }

        const accent = getComputedStyle(cell).getPropertyValue("--preview-accent").trim();
        subjectModal.style.setProperty("--subject-color", accent || "#7aa2ff");
    }

    /* ─────────────────────────────────────────────────
       SUBJECT MODAL — open animation

       The key fix for "content shifts during scale":
       CSS transform on a display:grid element causes the
       browser to re-evaluate grid track sizes against the
       VISUAL size, so text/layout "floats" during animation.

       Solution: wrap the clone in a plain div with
       overflow:hidden and a fixed explicit size. Scale is
       applied to the WRAPPER (no grid/flex inside it), so
       the clone inside is rendered like a frozen bitmap —
       zero layout side-effects, content stays pixel-perfect.
    ───────────────────────────────────────────────── */

    function animateSubjectOpen(cell) {
        if (!cell || cardAnimBusy) return;
        cardAnimBusy = true;

        const rect     = cell.getBoundingClientRect();
        const SCALE    = 1.13;
        const TARGET_X = 80;   // final card left (px from viewport left)
        const TARGET_Y = 72;   // final card top  (px from viewport top)
        const OPEN_MS  = 680;

        const finalW = rect.width  * SCALE;
        const finalH = rect.height * SCALE;

        populateSubjectModalFields(cell);

        /* Build clone ────────────────────────────────────
           Scale applied DIRECTLY to the clone — no wrapper,
           no intermediate rasterization, stays crisp at any scale.

           Fix 1 (blurriness): removed the overflow:hidden wrapper.
           That wrapper forced GPU rasterization at 1x then scaled
           the bitmap → blur. Direct transform on the element lets
           the compositor scale vector-quality graphics.

           Fix 2 (content shift): `contain: layout style paint`
           fully isolates the clone's internals. The browser treats
           it as a self-contained box — grid/flex tracks inside never
           re-evaluate against viewport or parent during the scale.

           Fix 3 (color lost): the clone moves to document.body,
           losing DOM ancestry, so inherited CSS custom properties
           (--preview-accent from the grid row) reset. We read the
           computed value BEFORE detaching and write it inline.     */
        const clone = cell.cloneNode(true);

        // Must read BEFORE appending to body (loses parent chain)
        const accentValue = getComputedStyle(cell)
            .getPropertyValue("--preview-accent").trim() || "#7aa2ff";

        clone.style.cssText = [
            "position:fixed",
            `left:${rect.left}px`,
            `top:${rect.top}px`,
            `width:${rect.width}px`,
            `height:${rect.height}px`,
            "margin:0",
            "min-height:unset",
            "box-sizing:border-box",
            "contain:layout style paint",
            "transform-origin:top left",
            "transform:none",
            "pointer-events:none",
            "transition:none",
            "z-index:9998",
            "box-shadow:0 6px 20px rgba(17,32,63,0.13)",
            "will-change:transform,box-shadow",
            `--preview-accent:${accentValue}`,
        ].join(";");

        document.body.appendChild(clone);

        // Keep original cell invisible while modal is open (restored on close)
        cell.classList.add("is-animating");

        // Save for close animation — clone is the animated element (no wrapper)
        activeCardAnim = { cell, clone, rect };

        /* Open modal — keep invisible until transitions start ─ */
        subjectModal.classList.remove("hidden");
        subjectModal.setAttribute("aria-hidden", "false");

        const dialog   = subjectModal.querySelector(".subject-detail-modal__dialog");
        const backdrop = subjectModal.querySelector(".subject-modal__backdrop");

        if (backdrop) {
            backdrop.style.transition = "none";
            backdrop.style.opacity    = "0";
        }

        // Clear preview div — clone IS the visible floating card
        if (subjectModalPreview) {
            subjectModalPreview.innerHTML = "";
            subjectModalPreview.removeAttribute("style");
        }

        /* Position dialog right of final card, fall back to below ── */
        const GAP        = 24;
        const dLeft0     = TARGET_X + finalW + GAP;
        const rightRoom  = window.innerWidth  - dLeft0 - 32;
        const belowTop   = TARGET_Y + finalH  + GAP;
        let dTop, dLeft, dWidth, dMaxH;
        if (rightRoom >= 300) {
            dTop   = TARGET_Y;
            dLeft  = dLeft0;
            dWidth = Math.min(660, rightRoom);
            dMaxH  = `calc(100vh - ${TARGET_Y + 32}px)`;
        } else {
            dTop   = belowTop;
            dLeft  = TARGET_X;
            dWidth = Math.min(660, window.innerWidth - TARGET_X - 32);
            dMaxH  = `calc(100vh - ${belowTop + 32}px)`;
        }

        if (dialog) {
            Object.assign(dialog.style, {
                position:  "fixed",
                top:       `${dTop}px`,
                left:      `${dLeft}px`,
                width:     `${dWidth}px`,
                maxHeight: dMaxH,
                opacity:   "0",
                transform: "translateX(28px)",
                transition:"none",
            });
        }

        /* Kick animation — double rAF ensures a paint flush before transitions */
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                const tx = TARGET_X - rect.left;
                const ty = TARGET_Y - rect.top;

                clone.style.transition = [
                    `transform ${OPEN_MS}ms cubic-bezier(0.25,0.86,0.2,1)`,
                    `box-shadow ${OPEN_MS}ms ease`,
                ].join(",");
                clone.style.transform  = `translate(${tx}px,${ty}px) scale(${SCALE})`;
                clone.style.boxShadow  = "0 22px 64px rgba(17,32,63,0.24)";

                if (backdrop) {
                    backdrop.style.transition = "opacity 460ms ease";
                    backdrop.style.opacity    = "1";
                }
            });
        });

        // Dialog slides in while card is mid-flight
        window.setTimeout(() => {
            if (dialog) {
                dialog.style.transition = [
                    "opacity 360ms cubic-bezier(0.25,0.8,0.25,1)",
                    "transform 360ms cubic-bezier(0.25,0.8,0.25,1)",
                ].join(",");
                dialog.style.opacity   = "1";
                dialog.style.transform = "translateX(0)";
            }
        }, Math.round(OPEN_MS * 0.52));

        window.setTimeout(() => { cardAnimBusy = false; }, OPEN_MS + 60);
    }

    /* ─────────────────────────────────────────────────
       SUBJECT MODAL — close animation (reverse)

       Dialog fades out first, then the clone slides back
       to its original position with scale(1).  Only after
       the card lands does the original cell reappear.
    ───────────────────────────────────────────────── */

    function closeSubjectModal() {
        if (!subjectModal) return;
        if (cardAnimBusy)  return;
        cardAnimBusy = true;

        const dialog   = subjectModal.querySelector(".subject-detail-modal__dialog");
        const backdrop = subjectModal.querySelector(".subject-modal__backdrop");
        const CLOSE_MS = 540;

        // Fade out dialog quickly
        if (dialog) {
            dialog.style.transition = "opacity 200ms ease, transform 200ms cubic-bezier(0.4,0,1,1)";
            dialog.style.opacity    = "0";
            dialog.style.transform  = "translateX(18px)";
        }
        // Fade out backdrop over same duration as card return
        if (backdrop) {
            backdrop.style.transition = `opacity ${CLOSE_MS}ms ease`;
            backdrop.style.opacity    = "0";
        }

        if (activeCardAnim) {
            const { cell, clone } = activeCardAnim;

            // Short delay so dialog starts fading before card moves
            window.setTimeout(() => {
                clone.style.transition = [
                    `transform ${CLOSE_MS}ms cubic-bezier(0.3,0,0.15,1)`,
                    `box-shadow ${CLOSE_MS}ms ease`,
                ].join(",");
                // "none" = back to translate(0,0) scale(1) = exact original position
                clone.style.transform  = "none";
                clone.style.boxShadow  = "0 6px 20px rgba(17,32,63,0.13)";
            }, 60);

            // After card lands — clean up and reveal original cell
            window.setTimeout(() => {
                clone.remove();
                cell.classList.remove("is-animating");
                activeCardAnim = null;
                cardAnimBusy   = false;

                subjectModal.classList.add("hidden");
                subjectModal.setAttribute("aria-hidden", "true");
                if (dialog)   dialog.removeAttribute("style");
                if (backdrop) backdrop.removeAttribute("style");
                if (subjectModalPreview) subjectModalPreview.removeAttribute("style");
            }, 60 + CLOSE_MS + 40);

        } else {
            // No card animation active — just close
            window.setTimeout(() => {
                subjectModal.classList.add("hidden");
                subjectModal.setAttribute("aria-hidden", "true");
                cardAnimBusy = false;
                if (dialog)   dialog.removeAttribute("style");
                if (backdrop) backdrop.removeAttribute("style");
            }, 260);
        }
    }

    /* ─────────────────────────────────────────────────
       HOMEWORK MODAL
    ───────────────────────────────────────────────── */

    async function loadHomeworkForWeek(weekStart, scope) {
        if (!weekStart) return;
        try {
            const data = await requestJson(
                `/scholar-hub/homework?weekStart=${encodeURIComponent(weekStart)}`,
                { method: "GET" }
            );
            const list   = Array.isArray(data) ? data : [];
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
        activeLessonCell  = cell;
        const homework    = getCachedHomework(cell);
        const subjectName = cell.dataset.subjectShortName || cell.dataset.subjectName || "Lesson";
        const lessonOrder = cell.dataset.lessonOrder || "";
        const lessonDate  = cell.dataset.lessonDate ? new Date(cell.dataset.lessonDate) : null;
        const dateLabel   = lessonDate
            ? formatFullDateLabel(lessonDate)
            : (dayLabels[cell.dataset.dayKey] || "Current day");

        homeworkContext.textContent    = `${subjectName} • lesson ${lessonOrder} • ${dateLabel}`;
        homeworkTitleInput.value       = homework ? homework.title   || "" : "";
        homeworkDetailsInput.value     = homework ? homework.details || "" : "";
        homeworkPriorityInput.value    = homework?.priority || homeworkPriorityInput.value || "_00FF00";
        if (homeworkStatusInput)  homeworkStatusInput.value  = homework?.status || homeworkStatusInput.value || "Pending";
        if (homeworkDeleteButton) homeworkDeleteButton.disabled = !homework;

        homeworkModal.classList.remove("hidden");
        homeworkModal.setAttribute("aria-hidden", "false");
        window.setTimeout(() => homeworkTitleInput.focus(), 0);
    }

    async function saveHomework() {
        if (!activeLessonCell || !homeworkTitleInput || !homeworkDetailsInput || !homeworkPriorityInput) return;
        const ids = getLessonIdentifiers(activeLessonCell);
        if (!ids || !ids.weekStart)  { showToast("error", "Homework week is missing."); return; }
        if (!ids.daySubjectId)       { showToast("error", "Day subject id is missing."); return; }

        const title    = String(homeworkTitleInput.value || "").trim();
        const details  = String(homeworkDetailsInput.value || "").trim();
        const priority = homeworkPriorityInput.value || "_00FF00";
        const status   = homeworkStatusInput ? (homeworkStatusInput.value || "Pending") : "Pending";
        if (!title) { showToast("error", "Homework title is required."); return; }

        const payload = {
            title, details, priority,
            daySubjectId: Number(ids.daySubjectId),
            status,
            weekStart: ids.weekStart
        };
        const cached = getCachedHomework(activeLessonCell);
        const hwId   = cached?.id || null;
        try {
            if (hwId) {
                await requestJson(`/scholar-hub/homework/${encodeURIComponent(hwId)}`, { method: "PUT",  body: JSON.stringify(payload) });
            } else {
                await requestJson("/scholar-hub/homework",                              { method: "POST", body: JSON.stringify(payload) });
            }
            await loadHomeworkForWeek(ids.weekStart, activeLessonCell.closest(".hub-timetable"));
            showToast("success", "Homework saved.");
            closeHomeworkModal();
        } catch (error) {
            console.error(error);
            showToast("error", "Failed to save homework.");
        }
    }

    async function deleteHomework() {
        if (!activeLessonCell) return;
        const ids    = getLessonIdentifiers(activeLessonCell);
        if (!ids || !ids.weekStart) { showToast("error", "Homework week is missing."); return; }
        const cached = getCachedHomework(activeLessonCell);
        if (!cached?.id) { showToast("error", "Homework id is missing."); return; }
        try {
            await requestJson(`/scholar-hub/homework/${encodeURIComponent(cached.id)}`, { method: "DELETE" });
            await loadHomeworkForWeek(ids.weekStart, activeLessonCell.closest(".hub-timetable"));
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
        const timetable  = weekNav.parentElement?.querySelector(".hub-timetable");
        if (!rangeLabel || !timetable) return;

        const dayIndexes = Array.from(timetable.querySelectorAll("[data-day-key]"))
            .map((el) => dayOrder[el.dataset.dayKey])
            .filter(Number.isInteger)
            .sort((a, b) => a - b);

        const firstIdx = dayIndexes.length > 0 ? dayIndexes[0]                      : 0;
        const lastIdx  = dayIndexes.length > 0 ? dayIndexes[dayIndexes.length - 1]  : 5;
        const url = new URL(window.location.href);
        let weekOffset = Number.parseInt(url.searchParams.get("weekOffset") || "0", 10);
        if (Number.isNaN(weekOffset)) weekOffset = 0;

        function applyWeekContext() {
            const monday = getMonday(new Date());
            monday.setDate(monday.getDate() + weekOffset * 7);
            rangeLabel.textContent = `${formatDateLabel(addDays(monday, firstIdx))}-${formatDateLabel(addDays(monday, lastIdx))}`;
            timetable.querySelectorAll(".hub-timetable__cell--filled").forEach((cell) => {
                const idx = dayOrder[cell.dataset.dayKey];
                if (!Number.isInteger(idx)) return;
                cell.dataset.weekStart  = toIsoDate(monday);
                cell.dataset.lessonDate = addDays(monday, idx).toISOString();
            });
            void loadHomeworkForWeek(toIsoDate(monday), timetable);
            refreshHomeworkIndicators(timetable);
        }

        function syncUrl() {
            if (weekOffset === 0) url.searchParams.delete("weekOffset");
            else url.searchParams.set("weekOffset", String(weekOffset));
            window.history.replaceState({}, "", url);
        }

        weekNav.querySelectorAll("[data-week-shift]").forEach((btn) => {
            btn.addEventListener("click", () => {
                const shift = Number.parseInt(btn.dataset.weekShift || "0", 10);
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

    document.querySelectorAll("[data-homework-trigger]").forEach((btn) => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            e.stopPropagation();
            const cell = btn.closest(".hub-timetable__cell--filled");
            if (cell) openHomeworkModal(cell);
        });
    });

    document.querySelectorAll(".hub-timetable__cell--filled").forEach((cell) => {
        cell.addEventListener("click", (e) => {
            if (e.target.closest("a, button, input, textarea, select, label")) return;
            animateSubjectOpen(cell);
        });
    });

    closeHomeworkTriggers.forEach((t) => t.addEventListener("click", closeHomeworkModal));
    closeSubjectTriggers.forEach((t)  => t.addEventListener("click", closeSubjectModal));

    if (homeworkSaveButton)   homeworkSaveButton.addEventListener("click",   () => { void saveHomework(); });
    if (homeworkDeleteButton) homeworkDeleteButton.addEventListener("click", () => { void deleteHomework(); });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            if (homeworkModal && !homeworkModal.classList.contains("hidden")) closeHomeworkModal();
            else if (subjectModal && !subjectModal.classList.contains("hidden")) closeSubjectModal();
        }
    });
});