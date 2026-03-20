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
        ["_D97706", { label: "Medium priority", color: "#D97706" }],
        ["_FFFF00", { label: "Medium priority", color: "#D97706" }],
        ["_FF0000", { label: "High priority",   color: "#FF0000" }],
        ["_6B21A8", { label: "Test",            color: "#6B21A8" }]
    ]);
    const defaultHomeworkPriority = "_00FF00";
    const testHomeworkStoredPriority = "_6B21A8";
    const homeworkStatuses = ["Pending", "Completed", "Non_completed", "Graded"];
    const gradedHomeworkIndicatorColor = "#215cc9";

    const homeworkModal        = document.getElementById("homework-modal");
    const homeworkTitleInput   = document.getElementById("homework-title");
    const homeworkDetailsInput = document.getElementById("homework-details");
    const homeworkPriorityInput= document.getElementById("homework-priority");
    const homeworkStatusInput  = document.getElementById("homework-status");
    const homeworkContext      = document.getElementById("homework-modal-context");
    const homeworkSaveButton   = document.getElementById("homework-save");
    const homeworkDeleteButton = document.getElementById("homework-delete");
    const closeHomeworkTriggers= Array.from(document.querySelectorAll("[data-close-homework-modal]"));
    const weekHomeworkModal = document.getElementById("week-homework-modal");
    const weekHomeworkContext = document.getElementById("week-homework-modal-context");
    const weekHomeworkList = document.getElementById("week-homework-list");
    const weekHomeworkEmpty = document.getElementById("week-homework-empty");
    const openWeekHomeworkTriggers = Array.from(document.querySelectorAll("[data-open-week-homework-modal]"));
    const closeWeekHomeworkTriggers = Array.from(document.querySelectorAll("[data-close-week-homework-modal]"));
    const toastStack           = document.querySelector("[data-homework-toasts]");
    const subjectModal         = document.getElementById("subject-modal");
    const subjectModalPreview  = document.getElementById("subject-modal-preview");
    const subjectModalShortName= document.getElementById("subject-modal-short-name");
    const subjectModalRoom     = document.getElementById("subject-modal-room");
    const subjectModalTeacher  = document.getElementById("subject-modal-teacher");
    const subjectModalLesson   = document.getElementById("subject-modal-lesson");
    const subjectModalAverage  = document.getElementById("subject-modal-average-value");
    const subjectUpcomingWidget = document.getElementById("subject-modal-upcoming-widget");
    const subjectUpcomingDate = document.getElementById("subject-modal-upcoming-date");
    const subjectUpcomingTitle = document.getElementById("subject-modal-upcoming-title");
    const subjectGradesModal = document.getElementById("subject-modal-grades-panel");
    const subjectGradesTrigger = document.querySelector("[data-open-subject-grades-modal]");
    const subjectGradesContext = document.getElementById("subject-grades-panel-context");
    const subjectSideModal     = document.getElementById("subject-modal-side");
    const subjectSideTrigger   = document.querySelector("[data-open-subject-side-modal]");
    const subjectUpcomingLayer = document.getElementById("subject-modal-upcoming-layer");
    const subjectHomeworkDetailModal = document.getElementById("subject-modal-homework-detail");
    const subjectCurrentGroup = document.getElementById("subject-current-group");
    const subjectPastGroup = document.getElementById("subject-past-group");
    const subjectCurrentList = document.getElementById("subject-current-list");
    const subjectPastList = document.getElementById("subject-past-list");
    const subjectCurrentEmpty = document.getElementById("subject-current-empty");
    const subjectPastEmpty = document.getElementById("subject-past-empty");
    const subjectHomeworkTabButtons = Array.from(document.querySelectorAll("[data-subject-homework-tab]"));
    const subjectHomeworkDetailHeading = document.getElementById("subject-homework-detail-title-heading");
    const subjectHomeworkDetailContext = document.getElementById("subject-homework-detail-context");
    const subjectHomeworkDetailTitleInput = document.getElementById("subject-homework-detail-title");
    const subjectHomeworkDetailDetailsInput = document.getElementById("subject-homework-detail-details");
    const subjectHomeworkDetailPriorityInput = document.getElementById("subject-homework-detail-priority");
    const subjectHomeworkDetailStatusInput = document.getElementById("subject-homework-detail-status");
    const subjectHomeworkDetailSaveButton = document.getElementById("subject-homework-detail-save");
    const subjectHomeworkDetailDeleteButton = document.getElementById("subject-homework-detail-delete");
    const subjectSideHomeworkPanelTitle = document.getElementById("subject-side-homework-title");
    const subjectSideHomeworkContext = document.getElementById("subject-side-homework-context");
    const subjectSideHomeworkTitleInput = document.getElementById("subject-side-homework-title-input");
    const subjectSideHomeworkDetailsInput = document.getElementById("subject-side-homework-details");
    const subjectSideHomeworkPriorityInput = document.getElementById("subject-side-homework-priority");
    const subjectSideHomeworkStatusInput = document.getElementById("subject-side-homework-status");
    const subjectSideHomeworkSaveButton = document.getElementById("subject-side-homework-save");
    const closeSubjectTriggers = Array.from(document.querySelectorAll("[data-close-subject-modal]"));
    const closeSubjectGradesTriggers = Array.from(document.querySelectorAll("[data-close-subject-grades-modal]"));
    const closeSubjectSideTriggers = Array.from(document.querySelectorAll("[data-close-subject-side-modal]"));
    const closeSubjectUpcomingTriggers = Array.from(document.querySelectorAll("[data-close-subject-upcoming-modal]"));
    const closeSubjectHomeworkDetailTriggers = Array.from(document.querySelectorAll("[data-close-subject-homework-detail-modal]"));
    const homeworkFormRoots = Array.from(document.querySelectorAll("[data-homework-form-root]"));

    const homeworkCache = new Map();
    let activeLessonCell = null;
    let subjectUpcomingTab = "current";
    let activeHomeworkDetailEntry = null;
    let activeHomeworkDetailTrigger = null;
    let activeWeekTimetable = null;
    let activeWeekHomeworkTrigger = null;

    // Stores { cell, clone, rect } while the subject modal is open
    let activeCardAnim = null;
    let cardAnimBusy   = false;
    let subjectDialogLayout = null;

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

    function getPriorityRank(priority) {
        switch (priority) {
            case "_6B21A8":
                return 4;
            case "_FF0000":
                return 3;
            case "_D97706":
            case "_FFFF00":
                return 2;
            case "_00FF00":
                return 1;
            default:
                return 0;
        }
    }

    function formatHomeworkStatus(status) {
        switch (status) {
            case "Non_completed":
                return "Not completed";
            default:
                return status || "Pending";
        }
    }

    function normalizePriorityValue(priority) {
        return priority === "_FFFF00" ? "_D97706" : priority;
    }

    function resetStatusDropdownAnimation(dropdown) {
        if (!dropdown) return;
        dropdown.style.removeProperty("transition");
        dropdown.style.removeProperty("opacity");
        dropdown.style.removeProperty("visibility");
        dropdown.style.removeProperty("pointer-events");
        dropdown.style.removeProperty("transform");
    }

    function setStatusMenuOpen(statusMenu, isOpen) {
        if (!statusMenu) return;

        const trigger = statusMenu.querySelector(".subject-homework-item__status-summary");
        const dropdown = statusMenu.querySelector(".subject-homework-item__status-dropdown");

        if (!isOpen) {
            statusMenu.classList.remove("is-open");
            trigger?.setAttribute("aria-expanded", "false");
            resetStatusDropdownAnimation(dropdown);
            return;
        }

        document.querySelectorAll(".subject-homework-item__status-menu.is-open").forEach((menu) => {
            if (menu !== statusMenu) setStatusMenuOpen(menu, false);
        });

        statusMenu.classList.add("is-open");
        trigger?.setAttribute("aria-expanded", "true");
        animateStatusMenuOpen(statusMenu);
    }

    function closeAllStatusMenus() {
        document.querySelectorAll(".subject-homework-item__status-menu.is-open").forEach((menu) => {
            setStatusMenuOpen(menu, false);
        });
    }

    function animateStatusMenuOpen(statusMenu) {
        if (!statusMenu?.classList.contains("is-open")) return;
        const badge = statusMenu.querySelector(".subject-homework-item__status-badge");
        const dropdown = statusMenu.querySelector(".subject-homework-item__status-dropdown");
        if (!badge || !dropdown) return;

        const badgeRect = badge.getBoundingClientRect();
        const dropdownRect = dropdown.getBoundingClientRect();
        if (!badgeRect.width || !badgeRect.height || !dropdownRect.width || !dropdownRect.height) return;

        const translateX = badgeRect.left - dropdownRect.left;
        const translateY = badgeRect.top + (badgeRect.height / 2) - (dropdownRect.top + dropdownRect.height / 2);
        const scaleX = Math.max(badgeRect.width / dropdownRect.width, 0.14);
        const scaleY = Math.max(badgeRect.height / dropdownRect.height, 0.26);

        dropdown.style.transition = "none";
        dropdown.style.opacity = "0";
        dropdown.style.visibility = "visible";
        dropdown.style.pointerEvents = "none";
        dropdown.style.transform = `translateY(-50%) translateX(${translateX}px) scale(${scaleX}, ${scaleY})`;

        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                dropdown.style.transition = [
                    "opacity 320ms cubic-bezier(0.22, 1, 0.36, 1)",
                    "transform 560ms cubic-bezier(0.18, 0.9, 0.2, 1)"
                ].join(", ");
                dropdown.style.opacity = "1";
                dropdown.style.pointerEvents = "auto";
                dropdown.style.transform = "translateY(-50%) translateX(0) scale(1)";
            });
        });
    }

    function syncPriorityPicker(inputId) {
        const input = document.getElementById(inputId);
        const picker = document.querySelector(`[data-priority-picker="${inputId}"]`);
        if (!input || !picker) return;

        picker.querySelectorAll("[data-priority-value]").forEach((button) => {
            const active = button.dataset.priorityValue === input.value;
            button.classList.toggle("is-selected", active);
            button.setAttribute("aria-pressed", active ? "true" : "false");
        });

        document.querySelectorAll(`[data-homework-form-root][data-priority-input-id="${inputId}"]`).forEach((root) => {
            syncHomeworkFormMode(root);
        });
    }

    function setPriorityValue(input, value) {
        if (!input) return;
        const nextValue = normalizePriorityValue(value || defaultHomeworkPriority);
        input.dataset.standardPriority = nextValue;
        input.value = nextValue;
        if (input.id) syncPriorityPicker(input.id);
    }

    function syncHomeworkFormMode(root) {
        if (!root) return;
        const inputId = root.getAttribute("data-priority-input-id");
        const input = inputId ? document.getElementById(inputId) : null;
        if (!input) return;

        const mode = root.dataset.homeworkMode || "homework";
        root.dataset.homeworkMode = mode;

        root.querySelectorAll("[data-homework-mode-trigger]").forEach((button) => {
            const active = button.getAttribute("data-homework-mode-trigger") === mode;
            button.classList.toggle("is-active", active);
            button.setAttribute("aria-selected", active ? "true" : "false");
        });

        const titleLabel = root.querySelector("[data-homework-title-label]");
        if (titleLabel) titleLabel.textContent = mode === "test" ? "Test title" : "Homework title";

        const titleInput = root.querySelector("[data-homework-title-input]");
        if (titleInput) {
            titleInput.placeholder = mode === "test"
                ? "For example: algebra test"
                : "For example: solve exercises 1-8";
        }

        const priorityField = root.querySelector("[data-homework-priority-field]");
        if (priorityField) priorityField.classList.toggle("is-hidden", mode === "test");
    }

    function setHomeworkFormMode(root, mode) {
        if (!root) return;
        const inputId = root.getAttribute("data-priority-input-id");
        const input = inputId ? document.getElementById(inputId) : null;
        if (!input) return;

        const previousMode = root.dataset.homeworkMode || "homework";
        root.dataset.homeworkMode = mode;

        if (mode === "test") {
            if (previousMode !== "test") {
                input.dataset.standardPriority = input.value || defaultHomeworkPriority;
            }
            input.value = testHomeworkStoredPriority;
            if (input.id) syncPriorityPicker(input.id);
            return;
        }

        setPriorityValue(input, input.dataset.standardPriority || defaultHomeworkPriority);
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

    function makeCacheKey(weekStart, dueDaySubjectId) {
        if (!weekStart || !dueDaySubjectId) return null;
        return `${weekStart}|${dueDaySubjectId}`;
    }

    function getLessonIdentifiers(cell) {
        if (!cell) return null;
        return {
            weekStart: cell.dataset.weekStart || "",
            dueDaySubjectId: cell.dataset.daySubjectId || "",
            subjectId: cell.dataset.subjectId || ""
        };
    }

    function getScheduleCells() {
        const timetable = activeLessonCell?.closest(".hub-timetable");
        return timetable
            ? Array.from(timetable.querySelectorAll(".hub-timetable__cell--filled[data-week-start]"))
            : [];
    }

    function getCachedHomeworks(cell) {
        const ids = getLessonIdentifiers(cell);
        if (!ids) return [];
        const key = makeCacheKey(ids.weekStart, ids.dueDaySubjectId);
        return key ? (homeworkCache.get(key) || []) : [];
    }

    function getHighestPriorityHomework(homeworks) {
        if (!Array.isArray(homeworks) || homeworks.length === 0) return null;
        return homeworks.reduce((best, item) => {
            if (!best) return item;
            return getPriorityRank(item.priority) > getPriorityRank(best.priority) ? item : best;
        }, null);
    }

    function getCachedHomework(cell) {
        return getHighestPriorityHomework(getCachedHomeworks(cell));
    }

    function getWeekHomeworkEntries(timetable) {
        if (!timetable) return [];

        return Array.from(timetable.querySelectorAll(".hub-timetable__cell--filled[data-week-start]"))
            .flatMap((cell) => {
                const lessonDate = cell.dataset.lessonDate ? new Date(cell.dataset.lessonDate) : null;
                if (!lessonDate || Number.isNaN(lessonDate.getTime())) return [];

                return getCachedHomeworks(cell).map((homework) => ({
                    cell,
                    homework,
                    lessonDate,
                    lessonOrder: Number(cell.dataset.lessonOrder || "0"),
                    subjectName: cell.dataset.subjectShortName || cell.dataset.subjectName || "Subject"
                }));
            })
            .sort((left, right) => {
                const dateDiff = left.lessonDate - right.lessonDate;
                if (dateDiff !== 0) return dateDiff;
                const lessonDiff = left.lessonOrder - right.lessonOrder;
                if (lessonDiff !== 0) return lessonDiff;
                return String(left.homework.title || "").localeCompare(String(right.homework.title || ""));
            });
    }

    function getIndicatorHomework(cell) {
        const visibleHomeworks = getCachedHomeworks(cell).filter((homework) => {
            if (!homework || (!homework.title && !homework.details)) return false;
            return homework.status !== "Completed";
        });
        return getHighestPriorityHomework(visibleHomeworks);
    }

    function setHomeworkIndicator(cell, homework) {
        const indicator = cell.querySelector("[data-homework-indicator]");
        if (!indicator) return;
        indicator.classList.remove("hidden");
        indicator.style.removeProperty("--homework-color");
        if (!homework || (!homework.title && !homework.details) || homework.status === "Completed") {
            indicator.classList.add("hidden");
            return;
        }
        const color = homework.status === "Graded"
            ? gradedHomeworkIndicatorColor
            : getPriorityColor(homework.priority);
        if (color) indicator.style.setProperty("--homework-color", color);
    }

    function refreshHomeworkIndicators(scope) {
        (scope || document).querySelectorAll(".hub-timetable__cell--filled[data-week-start]").forEach((cell) => {
            setHomeworkIndicator(cell, getIndicatorHomework(cell));
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

    function buildWeekHomeworkItem(entry) {
        const item = document.createElement("article");
        item.className = "week-homework-modal__item";

        const color = entry.homework.status === "Graded"
            ? gradedHomeworkIndicatorColor
            : getPriorityColor(entry.homework.priority);
        if (color) item.style.setProperty("--week-homework-color", color);

        const top = document.createElement("div");
        top.className = "week-homework-modal__top";

        const date = document.createElement("p");
        date.className = "week-homework-modal__date";
        date.textContent = formatDateLabel(entry.lessonDate);

        const subject = document.createElement("p");
        subject.className = "week-homework-modal__subject";
        subject.textContent = entry.subjectName;

        const title = document.createElement("p");
        title.className = "week-homework-modal__title";
        title.textContent = entry.homework.title || "Homework";

        top.append(date, subject, title);
        item.appendChild(top);

        if (entry.homework.details) {
            const details = document.createElement("p");
            details.className = "week-homework-modal__details";
            details.textContent = entry.homework.details;
            item.appendChild(details);
        }

        const meta = document.createElement("div");
        meta.className = "week-homework-modal__meta";

        const lessonBadge = document.createElement("span");
        lessonBadge.className = "subject-homework-item__badge";
        lessonBadge.textContent = `Lesson ${entry.lessonOrder || "-"}`;

        const statusBadge = document.createElement("span");
        statusBadge.className = "subject-homework-item__status-badge";
        statusBadge.dataset.status = entry.homework.status || "Pending";
        statusBadge.textContent = formatHomeworkStatus(entry.homework.status);

        meta.append(lessonBadge, statusBadge);
        item.appendChild(meta);

        return item;
    }

    function renderWeekHomeworkModal() {
        if (!weekHomeworkList || !weekHomeworkEmpty || !activeWeekTimetable) return;

        const entries = getWeekHomeworkEntries(activeWeekTimetable);
        weekHomeworkList.innerHTML = "";
        entries.forEach((entry) => weekHomeworkList.appendChild(buildWeekHomeworkItem(entry)));
        weekHomeworkEmpty.classList.toggle("hidden", entries.length > 0);

        const firstCell = activeWeekTimetable.querySelector(".hub-timetable__cell--filled[data-week-start]");
        if (weekHomeworkContext && firstCell?.dataset.weekStart) {
            const monday = new Date(firstCell.dataset.weekStart);
            const sunday = addDays(monday, 6);
            weekHomeworkContext.textContent = `${formatDateLabel(monday)}-${formatDateLabel(sunday)}`;
        } else if (weekHomeworkContext) {
            weekHomeworkContext.textContent = "Selected week";
        }
    }

    function resetWeekHomeworkModal() {
        if (!weekHomeworkModal) return;
        weekHomeworkModal.classList.add("hidden");
        weekHomeworkModal.setAttribute("aria-hidden", "true");
        weekHomeworkModal.removeAttribute("style");
        const dialog = weekHomeworkModal.querySelector(".week-homework-modal__dialog");
        const backdrop = weekHomeworkModal.querySelector(".subject-modal__backdrop");
        dialog?.removeAttribute("style");
        backdrop?.removeAttribute("style");
    }

    function closeWeekHomeworkModal() {
        if (!weekHomeworkModal || weekHomeworkModal.classList.contains("hidden")) return;

        const dialog = weekHomeworkModal.querySelector(".week-homework-modal__dialog");
        const backdrop = weekHomeworkModal.querySelector(".subject-modal__backdrop");
        const triggerRect = activeWeekHomeworkTrigger?.getBoundingClientRect();
        const panelRect = dialog?.getBoundingClientRect();
        const closeMs = 420;

        if (backdrop) {
            backdrop.style.transition = `opacity ${closeMs}ms ease`;
            backdrop.style.opacity = "0";
        }

        if (dialog && panelRect?.width && panelRect?.height && triggerRect?.width && triggerRect?.height) {
            const translateX = (triggerRect.left + triggerRect.width / 2) - (panelRect.left + panelRect.width / 2);
            const translateY = (triggerRect.top + triggerRect.height / 2) - (panelRect.top + panelRect.height / 2);
            const scaleX = Math.max(triggerRect.width / panelRect.width, 0.08);
            const scaleY = Math.max(triggerRect.height / panelRect.height, 0.08);

            dialog.style.transition = [
                `opacity ${Math.round(closeMs * 0.72)}ms ease`,
                `transform ${closeMs}ms cubic-bezier(0.2,0.8,0.2,1)`
            ].join(",");
            dialog.style.opacity = "0";
            dialog.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scaleX}, ${scaleY})`;
        } else if (dialog) {
            dialog.style.transition = `opacity ${closeMs}ms ease, transform ${closeMs}ms cubic-bezier(0.2,0.8,0.2,1)`;
            dialog.style.opacity = "0";
            dialog.style.transform = "translateY(18px) scale(0.94)";
        }

        window.setTimeout(() => {
            resetWeekHomeworkModal();
        }, closeMs + 30);
    }

    function openWeekHomeworkModal(timetable, triggerElement = null) {
        if (!weekHomeworkModal || !timetable) return;
        activeWeekTimetable = timetable;
        activeWeekHomeworkTrigger = triggerElement;
        renderWeekHomeworkModal();
        weekHomeworkModal.classList.remove("hidden");
        weekHomeworkModal.setAttribute("aria-hidden", "false");

        const dialog = weekHomeworkModal.querySelector(".week-homework-modal__dialog");
        const backdrop = weekHomeworkModal.querySelector(".subject-modal__backdrop");
        const triggerRect = triggerElement?.getBoundingClientRect();

        if (backdrop) {
            backdrop.style.transition = "none";
            backdrop.style.opacity = "0";
        }

        if (!dialog) return;

        dialog.style.transition = "none";
        dialog.style.opacity = "0";

        if (triggerRect?.width && triggerRect?.height) {
            const panelRect = dialog.getBoundingClientRect();
            const translateX = (triggerRect.left + triggerRect.width / 2) - (panelRect.left + panelRect.width / 2);
            const translateY = (triggerRect.top + triggerRect.height / 2) - (panelRect.top + panelRect.height / 2);
            const scaleX = Math.max(triggerRect.width / panelRect.width, 0.08);
            const scaleY = Math.max(triggerRect.height / panelRect.height, 0.08);
            dialog.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scaleX}, ${scaleY})`;
        } else {
            dialog.style.transform = "translateY(18px) scale(0.94)";
        }

        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                if (backdrop) {
                    backdrop.style.transition = "opacity 320ms ease";
                    backdrop.style.opacity = "1";
                }
                dialog.style.transition = [
                    "opacity 280ms ease",
                    "transform 520ms cubic-bezier(0.18,0.9,0.2,1)"
                ].join(",");
                dialog.style.opacity = "1";
                dialog.style.transform = "translate(0, 0) scale(1)";
            });
        });
    }

    function resetSubjectSideModal() {
        if (!subjectSideModal) return;
        subjectSideModal.classList.add("hidden");
        subjectSideModal.setAttribute("aria-hidden", "true");
        subjectSideModal.removeAttribute("style");
        if (subjectSideHomeworkPanelTitle) subjectSideHomeworkPanelTitle.textContent = "Add homework";
        if (subjectSideHomeworkContext) subjectSideHomeworkContext.textContent = "Current lesson";
        if (subjectSideHomeworkTitleInput) subjectSideHomeworkTitleInput.value = "";
        if (subjectSideHomeworkDetailsInput) subjectSideHomeworkDetailsInput.value = "";
        setPriorityValue(subjectSideHomeworkPriorityInput, defaultHomeworkPriority);
        if (subjectSideHomeworkStatusInput) subjectSideHomeworkStatusInput.value = "Pending";
    }

    function resetSubjectGradesModal() {
        if (!subjectGradesModal) return;
        subjectGradesModal.classList.add("hidden");
        subjectGradesModal.setAttribute("aria-hidden", "true");
        subjectGradesModal.removeAttribute("style");
        if (subjectGradesContext) subjectGradesContext.textContent = "Current lesson";
    }

    function resetSubjectUpcomingLayer() {
        if (!subjectUpcomingLayer) return;
        closeSubjectHomeworkDetailModal(true);
        subjectUpcomingLayer.classList.add("hidden");
        subjectUpcomingLayer.setAttribute("aria-hidden", "true");
        subjectUpcomingLayer.removeAttribute("style");
        setSubjectUpcomingTab("current");
        renderSubjectUpcomingLayer();
    }

    function isSubjectSideModalOpen() {
        return !!subjectSideModal && !subjectSideModal.classList.contains("hidden");
    }

    function isSubjectGradesModalOpen() {
        return !!subjectGradesModal && !subjectGradesModal.classList.contains("hidden");
    }

    function isSubjectUpcomingLayerOpen() {
        return !!subjectUpcomingLayer && !subjectUpcomingLayer.classList.contains("hidden");
    }

    function isSubjectHomeworkDetailModalOpen() {
        return !!subjectHomeworkDetailModal && !subjectHomeworkDetailModal.classList.contains("hidden");
    }

    function getNextUpcomingHomework(cell) {
        if (!cell) return null;
        const subjectId = cell.dataset.subjectId;
        if (!subjectId) return null;

        const currentTime = cell.dataset.lessonDate ? new Date(cell.dataset.lessonDate).getTime() : Number.NEGATIVE_INFINITY;
        const candidates = getScheduleCells()
            .filter((candidate) => candidate.dataset.subjectId === subjectId)
            .map((candidate) => {
                const homework = getHighestPriorityHomework(
                    getCachedHomeworks(candidate).filter((item) => item?.status === "Pending")
                );
                const lessonDate = candidate.dataset.lessonDate ? new Date(candidate.dataset.lessonDate) : null;
                return { cell: candidate, homework, lessonDate };
            })
            .filter((entry) => entry.homework && entry.lessonDate && entry.lessonDate.getTime() >= currentTime)
            .sort((left, right) => {
                const dateDiff = left.lessonDate - right.lessonDate;
                if (dateDiff !== 0) return dateDiff;
                return Number(left.cell.dataset.lessonOrder || "0") - Number(right.cell.dataset.lessonOrder || "0");
            });

        return candidates[0] || null;
    }

    function updateSubjectUpcomingWidget() {
        if (!subjectUpcomingDate) return;
        const nextHomework = getNextUpcomingHomework(activeLessonCell);
        if (!nextHomework) {
            subjectUpcomingDate.textContent = "No upcoming homework yet";
            if (subjectUpcomingTitle) {
                subjectUpcomingTitle.textContent = "";
                subjectUpcomingTitle.classList.add("hidden");
                subjectUpcomingTitle.style.removeProperty("--upcoming-chip-color");
            }
            return;
        }

        subjectUpcomingDate.textContent = formatDateLabel(nextHomework.lessonDate);
        if (subjectUpcomingTitle) {
            subjectUpcomingTitle.textContent = nextHomework.homework.title || "Homework";
            subjectUpcomingTitle.classList.remove("hidden");
            const color = getPriorityColor(nextHomework.homework.priority);
            if (color) {
                subjectUpcomingTitle.style.setProperty("--upcoming-chip-color", color);
            } else {
                subjectUpcomingTitle.style.removeProperty("--upcoming-chip-color");
            }
        }
    }

    function getSubjectHomeworkEntries() {
        if (!activeLessonCell) return [];
        const subjectId = activeLessonCell.dataset.subjectId;
        if (!subjectId) return [];

        return getScheduleCells()
            .filter((candidate) => candidate.dataset.subjectId === subjectId)
            .flatMap((candidate) => {
                const lessonDate = candidate.dataset.lessonDate ? new Date(candidate.dataset.lessonDate) : null;
                return getCachedHomeworks(candidate).map((homework) => ({
                    cell: candidate,
                    homework,
                    lessonDate,
                    lessonOrder: Number(candidate.dataset.lessonOrder || "0"),
                }));
            })
            .filter((entry) => entry.homework && entry.lessonDate)
            .sort((left, right) => {
                const dateDiff = left.lessonDate - right.lessonDate;
                if (dateDiff !== 0) return dateDiff;
                return left.lessonOrder - right.lessonOrder;
            });
    }

    function isEntryBeforeActiveLesson(entry) {
        if (!activeLessonCell || !entry?.lessonDate) return false;
        const activeLessonDate = activeLessonCell.dataset.lessonDate ? new Date(activeLessonCell.dataset.lessonDate) : null;
        if (!activeLessonDate) return false;

        const entryTime = entry.lessonDate.getTime();
        const activeTime = activeLessonDate.getTime();
        if (entryTime !== activeTime) {
            return entryTime < activeTime;
        }

        const activeOrder = Number(activeLessonCell.dataset.lessonOrder || "0");
        return entry.lessonOrder < activeOrder;
    }

    function isCurrentSubjectHomework(entry) {
        if (!entry?.homework) return false;
        return entry.homework.status === "Pending" && !isEntryBeforeActiveLesson(entry);
    }

    function isPreviousSubjectHomework(entry) {
        if (!entry?.homework) return false;
        return !isCurrentSubjectHomework(entry);
    }

    function buildSubjectHomeworkItem(entry, isPast = false) {
        const item = document.createElement("article");
        item.className = `subject-homework-item${isPast ? " subject-homework-item--past" : ""}`;
        const color = getPriorityColor(entry.homework.priority);
        if (color) item.style.setProperty("--homework-item-color", color);

        const top = document.createElement("div");
        top.className = "subject-homework-item__top";

        const date = document.createElement("p");
        date.className = "subject-homework-item__date";
        date.textContent = formatDateLabel(entry.lessonDate);

        const title = document.createElement("p");
        title.className = "subject-homework-item__title";
        title.textContent = entry.homework.title || "Homework";

        top.append(date, title);
        item.appendChild(top);

        if (entry.homework.details) {
            const details = document.createElement("p");
            details.className = "subject-homework-item__details";
            details.textContent = entry.homework.details;
            item.appendChild(details);
        }

        const meta = document.createElement("div");
        meta.className = "subject-homework-item__meta";

        const lessonBadge = document.createElement("span");
        lessonBadge.className = "subject-homework-item__badge";
        lessonBadge.textContent = `Lesson ${entry.lessonOrder || "-"}`;
        meta.appendChild(lessonBadge);

        const statusMenu = document.createElement("div");
        statusMenu.className = "subject-homework-item__status-menu";

        const statusSummary = document.createElement("button");
        statusSummary.type = "button";
        statusSummary.className = "subject-homework-item__status-summary";
        statusSummary.setAttribute("aria-haspopup", "menu");
        statusSummary.setAttribute("aria-expanded", "false");

        const statusBadge = document.createElement("span");
        statusBadge.className = "subject-homework-item__status-badge";
        statusBadge.dataset.status = entry.homework.status || "Pending";
        statusBadge.textContent = formatHomeworkStatus(entry.homework.status);
        statusSummary.appendChild(statusBadge);
        statusMenu.appendChild(statusSummary);

        const statusDropdown = document.createElement("div");
        statusDropdown.className = "subject-homework-item__status-dropdown";

        homeworkStatuses.forEach((status) => {
            const option = document.createElement("button");
            option.type = "button";
            option.className = "subject-homework-item__status-option";
            option.dataset.status = status;
            option.textContent = formatHomeworkStatus(status);
            option.disabled = status === (entry.homework.status || "Pending");
            option.addEventListener("click", (event) => {
                event.preventDefault();
                event.stopPropagation();
                void updateHomeworkStatus(entry, status, statusMenu, statusBadge, statusDropdown);
            });
            statusDropdown.appendChild(option);
        });

        statusSummary.addEventListener("click", (event) => {
            event.preventDefault();
            event.stopPropagation();
            setStatusMenuOpen(statusMenu, !statusMenu.classList.contains("is-open"));
        });

        statusMenu.addEventListener("keydown", (event) => {
            if (event.key !== "Escape") return;
            event.preventDefault();
            event.stopPropagation();
            setStatusMenuOpen(statusMenu, false);
            statusSummary.focus();
        });

        statusMenu.appendChild(statusDropdown);
        meta.appendChild(statusMenu);

        item.appendChild(meta);
        item.addEventListener("click", (event) => {
            if (event.target.closest(".subject-homework-item__status-menu")) return;
            openSubjectHomeworkDetailModal(entry, item);
        });
        return item;
    }

    async function updateHomeworkStatus(entry, status, statusMenu, statusBadge, statusDropdown) {
        if (!entry?.homework?.id || !activeLessonCell) return;

        const payload = {
            title: entry.homework.title || "",
            details: entry.homework.details || "",
            priority: entry.homework.priority || defaultHomeworkPriority,
            subjectId: Number(entry.homework.subjectId ?? entry.cell?.dataset?.subjectId),
            dueDaySubjectId: Number(entry.homework.dueDaySubjectId ?? entry.homework.daySubjectId ?? entry.cell?.dataset?.daySubjectId),
            status,
            weekStart: entry.homework.weekStart || entry.cell?.dataset?.weekStart || activeLessonCell.dataset.weekStart
        };

        if (!payload.subjectId || !payload.dueDaySubjectId || !payload.weekStart) {
            showToast("error", "Homework update context is missing.");
            return;
        }

        const previousStatus = entry.homework.status || "Pending";

        try {
            statusDropdown?.querySelectorAll("button").forEach((button) => {
                button.disabled = true;
            });
            await requestJson(`/scholar-hub/homework/${encodeURIComponent(entry.homework.id)}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });
            entry.homework.status = status;
            if (statusBadge) {
                statusBadge.dataset.status = status;
                statusBadge.textContent = formatHomeworkStatus(status);
            }
            await loadHomeworkForWeek(payload.weekStart, activeLessonCell.closest(".hub-timetable"));
            setStatusMenuOpen(statusMenu, false);
            showToast("success", "Homework status updated.");
        } catch (error) {
            console.error(error);
            entry.homework.status = previousStatus;
            if (statusBadge) {
                statusBadge.dataset.status = previousStatus;
                statusBadge.textContent = formatHomeworkStatus(previousStatus);
            }
            showToast("error", "Failed to update homework status.");
        } finally {
            statusDropdown?.querySelectorAll("button").forEach((button) => {
                button.disabled = button.dataset.status === (entry.homework.status || "Pending");
            });
        }
    }

    function renderSubjectHomeworkList(container, entries, isPast = false) {
        if (!container) return;
        container.innerHTML = "";
        entries.forEach((entry) => container.appendChild(buildSubjectHomeworkItem(entry, isPast)));
    }

    function renderSubjectUpcomingLayer() {
        const entries = getSubjectHomeworkEntries();
        const currentEntries = entries
            .filter(isCurrentSubjectHomework)
            .sort((left, right) => {
                const dateDiff = left.lessonDate - right.lessonDate;
                if (dateDiff !== 0) return dateDiff;
                return left.lessonOrder - right.lessonOrder;
            });
        const pastEntries = entries
            .filter(isPreviousSubjectHomework)
            .sort((left, right) => {
                const dateDiff = right.lessonDate - left.lessonDate;
                if (dateDiff !== 0) return dateDiff;
                return right.lessonOrder - left.lessonOrder;
            });

        renderSubjectHomeworkList(subjectCurrentList, currentEntries, false);
        renderSubjectHomeworkList(subjectPastList, pastEntries, true);

        if (subjectCurrentEmpty) {
            subjectCurrentEmpty.classList.toggle("hidden", currentEntries.length > 0);
        }
        if (subjectPastEmpty) {
            subjectPastEmpty.classList.toggle("hidden", pastEntries.length > 0);
        }
        syncSubjectUpcomingTab();
    }

    function setSubjectUpcomingTab(tab) {
        subjectUpcomingTab = tab === "past" ? "past" : "current";
        syncSubjectUpcomingTab();
    }

    function syncSubjectUpcomingTab() {
        subjectHomeworkTabButtons.forEach((button) => {
            const isActive = button.dataset.subjectHomeworkTab === subjectUpcomingTab;
            button.classList.toggle("is-active", isActive);
            button.setAttribute("aria-selected", isActive ? "true" : "false");
        });

        if (subjectCurrentGroup) {
            subjectCurrentGroup.classList.toggle("hidden", subjectUpcomingTab !== "current");
        }
        if (subjectPastGroup) {
            subjectPastGroup.classList.toggle("hidden", subjectUpcomingTab !== "past");
        }
    }

    function getSubjectSideTriggerRect() {
        if (!subjectSideTrigger) return null;
        const rect = subjectSideTrigger.getBoundingClientRect();
        if (!rect.width || !rect.height) return null;
        return rect;
    }

    function getSubjectGradesTriggerRect() {
        if (!subjectGradesTrigger) return null;
        const rect = subjectGradesTrigger.getBoundingClientRect();
        if (!rect.width || !rect.height) return null;
        return rect;
    }

    function closeSubjectGradesModal(force = false) {
        if (!subjectGradesModal || subjectGradesModal.classList.contains("hidden")) return;
        if (force) {
            resetSubjectGradesModal();
            return;
        }

        const closeMs = 520;
        const panelRect = subjectGradesModal.getBoundingClientRect();
        const triggerRect = getSubjectGradesTriggerRect();

        if (triggerRect && panelRect.width && panelRect.height) {
            const translateX = triggerRect.left - panelRect.left;
            const translateY = triggerRect.top - panelRect.top;
            const scaleX = Math.max(triggerRect.width / panelRect.width, 0.08);
            const scaleY = Math.max(triggerRect.height / panelRect.height, 0.08);

            subjectGradesModal.style.transition = [
                `opacity ${closeMs}ms cubic-bezier(0.4,0,0.2,1)`,
                `transform ${closeMs}ms cubic-bezier(0.2,0.8,0.2,1)`,
            ].join(",");
            subjectGradesModal.style.opacity = "0";
            subjectGradesModal.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scaleX}, ${scaleY})`;
        } else {
            subjectGradesModal.style.transition = `opacity ${closeMs}ms ease, transform ${closeMs}ms ease`;
            subjectGradesModal.style.opacity = "0";
            subjectGradesModal.style.transform = "translateX(18px) scale(0.92)";
        }

        window.setTimeout(() => {
            resetSubjectGradesModal();
        }, closeMs + 30);
    }

    function closeSubjectSideModal() {
        if (!subjectSideModal || subjectSideModal.classList.contains("hidden")) return;
        const closeMs = 520;
        const panelRect = subjectSideModal.getBoundingClientRect();
        const triggerRect = getSubjectSideTriggerRect();

        if (triggerRect && panelRect.width && panelRect.height) {
            const translateX = triggerRect.left - panelRect.left;
            const translateY = triggerRect.top - panelRect.top;
            const scaleX = Math.max(triggerRect.width / panelRect.width, 0.08);
            const scaleY = Math.max(triggerRect.height / panelRect.height, 0.08);

            subjectSideModal.style.transition = [
                `opacity ${closeMs}ms cubic-bezier(0.4,0,0.2,1)`,
                `transform ${closeMs}ms cubic-bezier(0.2,0.8,0.2,1)`,
            ].join(",");
            subjectSideModal.style.opacity = "0";
            subjectSideModal.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scaleX}, ${scaleY})`;
        } else {
            subjectSideModal.style.transition = `opacity ${closeMs}ms ease, transform ${closeMs}ms ease`;
            subjectSideModal.style.opacity = "0";
            subjectSideModal.style.transform = "translateX(18px) scale(0.92)";
        }

        window.setTimeout(() => {
            resetSubjectSideModal();
        }, closeMs + 30);
    }

    function closeSubjectUpcomingLayer() {
        if (!subjectUpcomingLayer || subjectUpcomingLayer.classList.contains("hidden")) return;
        closeSubjectHomeworkDetailModal(true);

        const closeMs = 480;
        const layerRect = subjectUpcomingLayer.getBoundingClientRect();
        const triggerRect = subjectUpcomingWidget ? subjectUpcomingWidget.getBoundingClientRect() : null;

        if (triggerRect && layerRect.width && layerRect.height) {
            const translateX = triggerRect.left - layerRect.left;
            const translateY = triggerRect.top - layerRect.top;
            const scaleX = Math.max(triggerRect.width / layerRect.width, 0.18);
            const scaleY = Math.max(triggerRect.height / layerRect.height, 0.18);

            subjectUpcomingLayer.style.transition = [
                `opacity ${closeMs}ms cubic-bezier(0.4,0,0.2,1)`,
                `transform ${closeMs}ms cubic-bezier(0.2,0.8,0.2,1)`,
            ].join(",");
            subjectUpcomingLayer.style.opacity = "0";
            subjectUpcomingLayer.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scaleX}, ${scaleY})`;
        } else {
            subjectUpcomingLayer.style.transition = `opacity ${closeMs}ms ease, transform ${closeMs}ms ease`;
            subjectUpcomingLayer.style.opacity = "0";
            subjectUpcomingLayer.style.transform = "translateX(16px) scale(0.94)";
        }

        window.setTimeout(() => {
            resetSubjectUpcomingLayer();
        }, closeMs + 30);
    }

    function resetSubjectHomeworkDetailModal() {
        if (!subjectHomeworkDetailModal) return;
        subjectHomeworkDetailModal.classList.add("hidden");
        subjectHomeworkDetailModal.setAttribute("aria-hidden", "true");
        subjectHomeworkDetailModal.removeAttribute("style");
        if (subjectHomeworkDetailHeading) subjectHomeworkDetailHeading.textContent = "Homework";
        if (subjectHomeworkDetailContext) subjectHomeworkDetailContext.textContent = "Current lesson";
        if (subjectHomeworkDetailTitleInput) subjectHomeworkDetailTitleInput.value = "";
        if (subjectHomeworkDetailDetailsInput) subjectHomeworkDetailDetailsInput.value = "";
        setPriorityValue(subjectHomeworkDetailPriorityInput, defaultHomeworkPriority);
        if (subjectHomeworkDetailStatusInput) subjectHomeworkDetailStatusInput.value = "Pending";
        activeHomeworkDetailEntry = null;
        activeHomeworkDetailTrigger = null;
    }

    function closeSubjectHomeworkDetailModal(force = false) {
        if (!subjectHomeworkDetailModal || subjectHomeworkDetailModal.classList.contains("hidden")) return;
        if (force) {
            resetSubjectHomeworkDetailModal();
            return;
        }

        const closeMs = 520;
        const panelRect = subjectHomeworkDetailModal.getBoundingClientRect();
        const triggerRect = activeHomeworkDetailTrigger?.getBoundingClientRect();

        if (triggerRect && panelRect.width && panelRect.height) {
            const translateX = triggerRect.left - panelRect.left;
            const translateY = triggerRect.top - panelRect.top;
            const scaleX = Math.max(triggerRect.width / panelRect.width, 0.12);
            const scaleY = Math.max(triggerRect.height / panelRect.height, 0.12);
            subjectHomeworkDetailModal.style.transition = [
                `opacity ${closeMs}ms cubic-bezier(0.4,0,0.2,1)`,
                `transform ${closeMs}ms cubic-bezier(0.2,0.8,0.2,1)`
            ].join(",");
            subjectHomeworkDetailModal.style.opacity = "0";
            subjectHomeworkDetailModal.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scaleX}, ${scaleY})`;
        } else {
            subjectHomeworkDetailModal.style.transition = `opacity ${closeMs}ms ease, transform ${closeMs}ms ease`;
            subjectHomeworkDetailModal.style.opacity = "0";
            subjectHomeworkDetailModal.style.transform = "translateX(18px) scale(0.94)";
        }

        window.setTimeout(() => {
            resetSubjectHomeworkDetailModal();
        }, closeMs + 30);
    }

    function populateSubjectHomeworkDetailModal(entry) {
        if (!entry) return;
        const context = getLessonContext(entry.cell);
        if (subjectHomeworkDetailHeading) {
            subjectHomeworkDetailHeading.textContent = entry.homework.title || "Homework";
        }
        if (subjectHomeworkDetailContext) {
            subjectHomeworkDetailContext.textContent = context.context;
        }
        if (subjectHomeworkDetailTitleInput) {
            subjectHomeworkDetailTitleInput.value = entry.homework.title || "";
        }
        if (subjectHomeworkDetailDetailsInput) {
            subjectHomeworkDetailDetailsInput.value = entry.homework.details || "";
        }
        setPriorityValue(subjectHomeworkDetailPriorityInput, entry.homework.priority || defaultHomeworkPriority);
        if (subjectHomeworkDetailStatusInput) {
            subjectHomeworkDetailStatusInput.value = entry.homework.status || "Pending";
        }
    }

    function openSubjectHomeworkDetailModal(entry, triggerElement) {
        if (!subjectHomeworkDetailModal || !subjectDialogLayout || !entry?.homework) return;
        const isSameHomework =
            isSubjectHomeworkDetailModalOpen() &&
            activeHomeworkDetailEntry?.homework?.id &&
            activeHomeworkDetailEntry.homework.id === entry.homework.id;

        if (isSameHomework) {
            closeSubjectHomeworkDetailModal();
            return;
        }

        activeHomeworkDetailEntry = entry;
        activeHomeworkDetailTrigger = triggerElement || null;

        const baseLeft = isSubjectUpcomingLayerOpen()
            ? Number.parseFloat(subjectUpcomingLayer.style.left || "0")
            : subjectDialogLayout.left;
        const baseWidth = isSubjectUpcomingLayerOpen()
            ? Number.parseFloat(subjectUpcomingLayer.style.width || "0")
            : subjectDialogLayout.width;
        const baseTop = isSubjectUpcomingLayerOpen()
            ? Number.parseFloat(subjectUpcomingLayer.style.top || "0")
            : subjectDialogLayout.top;
        const baseMaxHeight = isSubjectUpcomingLayerOpen()
            ? subjectUpcomingLayer.style.maxHeight
            : subjectDialogLayout.maxHeight;
        const desiredWidth = Math.min(390, window.innerWidth - 48);
        const preferredOverlap = 34;
        const preferredLeft = baseLeft + baseWidth - preferredOverlap;
        const preferredRoom = window.innerWidth - preferredLeft - 24;
        let width = Math.min(desiredWidth, Math.max(preferredRoom, 280));
        let left = preferredLeft;

        if (preferredRoom < 280) {
            width = desiredWidth;
            left = Math.max(baseLeft + baseWidth - width - 88, 24);
        }

        const maxLeft = Math.max(window.innerWidth - width - 24, 24);
        left = Math.min(left, maxLeft);
        const top = Math.max(baseTop + 10, 20);
        const openMs = 680;

        populateSubjectHomeworkDetailModal(entry);
        subjectHomeworkDetailModal.classList.remove("hidden");
        subjectHomeworkDetailModal.setAttribute("aria-hidden", "false");
        Object.assign(subjectHomeworkDetailModal.style, {
            position: "fixed",
            top: `${top}px`,
            left: `${left}px`,
            width: `${width}px`,
            maxHeight: baseMaxHeight || `calc(100vh - ${top + 24}px)`,
            opacity: "0",
            transform: "translateX(0) scale(1)",
            transformOrigin: "top left",
            transition: "none"
        });

        const panelRect = subjectHomeworkDetailModal.getBoundingClientRect();
        const triggerRect = triggerElement?.getBoundingClientRect();
        if (triggerRect && panelRect.width && panelRect.height) {
            const translateX = triggerRect.left - panelRect.left;
            const translateY = triggerRect.top - panelRect.top;
            const scaleX = Math.max(triggerRect.width / panelRect.width, 0.12);
            const scaleY = Math.max(triggerRect.height / panelRect.height, 0.12);
            subjectHomeworkDetailModal.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scaleX}, ${scaleY})`;
        } else {
            subjectHomeworkDetailModal.style.transform = "translateX(22px) scale(0.92)";
        }

        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                subjectHomeworkDetailModal.style.transition = [
                    `opacity ${Math.round(openMs * 0.82)}ms cubic-bezier(0.2,0.8,0.2,1)`,
                    `transform ${openMs}ms cubic-bezier(0.18,0.9,0.2,1)`
                ].join(",");
                subjectHomeworkDetailModal.style.opacity = "1";
                subjectHomeworkDetailModal.style.transform = "translate(0, 0) scale(1)";
            });
        });

        window.setTimeout(() => {
            subjectHomeworkDetailTitleInput?.focus();
        }, Math.round(openMs * 0.72));
    }

    function toggleSubjectSideModal() {
        if (isSubjectSideModalOpen()) {
            closeSubjectSideModal();
            return;
        }
        openSubjectSideModal();
    }

    function toggleSubjectGradesModal() {
        if (isSubjectGradesModalOpen()) {
            closeSubjectGradesModal();
            return;
        }
        openSubjectGradesModal();
    }

    function openSubjectGradesModal() {
        if (!subjectGradesModal || !subjectDialogLayout || !activeLessonCell) return;
        if (!subjectGradesModal.classList.contains("hidden")) return;

        closeSubjectHomeworkDetailModal(true);
        resetSubjectUpcomingLayer();
        resetSubjectSideModal();

        const overlap = 26;
        const sideMinWidth = 240;
        const sideMaxWidth = 360;
        const sideLeft = subjectDialogLayout.left + subjectDialogLayout.width - overlap;
        const sideRoom = window.innerWidth - sideLeft - 24;
        if (sideRoom < sideMinWidth) return;

        const sideWidth = Math.min(sideMaxWidth, sideRoom);
        const triggerRect = getSubjectGradesTriggerRect();
        const lessonContext = getLessonContext(activeLessonCell);
        const openMs = 680;

        if (subjectGradesContext) {
            subjectGradesContext.textContent = lessonContext.context;
        }

        subjectGradesModal.classList.remove("hidden");
        subjectGradesModal.setAttribute("aria-hidden", "false");
        Object.assign(subjectGradesModal.style, {
            position: "fixed",
            top: `${subjectDialogLayout.top}px`,
            left: `${sideLeft}px`,
            width: `${sideWidth}px`,
            maxHeight: subjectDialogLayout.maxHeight,
            opacity: "0",
            transform: "translateX(0) scale(1)",
            transformOrigin: "top left",
            transition: "none",
        });

        const panelRect = subjectGradesModal.getBoundingClientRect();
        if (triggerRect && panelRect.width && panelRect.height) {
            const translateX = triggerRect.left - panelRect.left;
            const translateY = triggerRect.top - panelRect.top;
            const scaleX = Math.max(triggerRect.width / panelRect.width, 0.08);
            const scaleY = Math.max(triggerRect.height / panelRect.height, 0.08);
            subjectGradesModal.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scaleX}, ${scaleY})`;
        } else {
            subjectGradesModal.style.transform = "translateX(24px) scale(0.92)";
        }

        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                subjectGradesModal.style.transition = [
                    `opacity ${Math.round(openMs * 0.82)}ms cubic-bezier(0.2,0.8,0.2,1)`,
                    `transform ${openMs}ms cubic-bezier(0.18,0.9,0.2,1)`,
                ].join(",");
                subjectGradesModal.style.opacity = "1";
                subjectGradesModal.style.transform = "translate(0, 0) scale(1)";
            });
        });
    }

    function openSubjectSideModal() {
        if (!subjectSideModal || !subjectDialogLayout || !activeLessonCell) return;
        if (!subjectSideModal.classList.contains("hidden")) return;
        resetSubjectGradesModal();

        const overlap = 26;
        const sideMinWidth = 240;
        const sideMaxWidth = 360;
        const sideLeft = subjectDialogLayout.left + subjectDialogLayout.width - overlap;
        const sideRoom = window.innerWidth - sideLeft - 24;
        if (sideRoom < sideMinWidth) return;

        const sideWidth = Math.min(sideMaxWidth, sideRoom);
        const triggerRect = getSubjectSideTriggerRect();
        const openMs = 680;

        populateSubjectSideHomeworkForm();

        subjectSideModal.classList.remove("hidden");
        subjectSideModal.setAttribute("aria-hidden", "false");
        Object.assign(subjectSideModal.style, {
            position: "fixed",
            top: `${subjectDialogLayout.top}px`,
            left: `${sideLeft}px`,
            width: `${sideWidth}px`,
            maxHeight: subjectDialogLayout.maxHeight,
            opacity: "0",
            transform: "translateX(0) scale(1)",
            transformOrigin: "top left",
            transition: "none",
        });

        const panelRect = subjectSideModal.getBoundingClientRect();
        if (triggerRect && panelRect.width && panelRect.height) {
            const translateX = triggerRect.left - panelRect.left;
            const translateY = triggerRect.top - panelRect.top;
            const scaleX = Math.max(triggerRect.width / panelRect.width, 0.08);
            const scaleY = Math.max(triggerRect.height / panelRect.height, 0.08);
            subjectSideModal.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scaleX}, ${scaleY})`;
        } else {
            subjectSideModal.style.transform = "translateX(24px) scale(0.92)";
        }

        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                subjectSideModal.style.transition = [
                    `opacity ${Math.round(openMs * 0.82)}ms cubic-bezier(0.2,0.8,0.2,1)`,
                    `transform ${openMs}ms cubic-bezier(0.18,0.9,0.2,1)`,
                ].join(",");
                subjectSideModal.style.opacity = "1";
                subjectSideModal.style.transform = "translate(0, 0) scale(1)";
            });
        });

        window.setTimeout(() => {
            subjectSideHomeworkTitleInput?.focus();
        }, Math.round(openMs * 0.72));
    }

    function openSubjectUpcomingLayer() {
        if (!subjectUpcomingLayer || !subjectDialogLayout || !subjectUpcomingWidget) return;
        if (!subjectUpcomingLayer.classList.contains("hidden")) return;
        resetSubjectGradesModal();
        setSubjectUpcomingTab("current");
        renderSubjectUpcomingLayer();

        const overlap = 220;
        const top = Math.max(subjectDialogLayout.top + 8, 20);
        const left = Math.max(subjectDialogLayout.left + subjectDialogLayout.width - overlap, 24);
        const room = window.innerWidth - left - 24;
        if (room < 360) return;

        const width = Math.min(760, room);
        const maxHeight = `calc(100vh - ${top + 18}px)`;
        const triggerRect = subjectUpcomingWidget.getBoundingClientRect();
        const openMs = 620;

        subjectUpcomingLayer.classList.remove("hidden");
        subjectUpcomingLayer.setAttribute("aria-hidden", "false");
        Object.assign(subjectUpcomingLayer.style, {
            position: "fixed",
            top: `${top}px`,
            left: `${left}px`,
            width: `${width}px`,
            maxHeight,
            opacity: "0",
            transform: "translateX(0) scale(1)",
            transformOrigin: "top left",
            transition: "none",
        });

        const layerRect = subjectUpcomingLayer.getBoundingClientRect();
        if (triggerRect && layerRect.width && layerRect.height) {
            const translateX = triggerRect.left - layerRect.left;
            const translateY = triggerRect.top - layerRect.top;
            const scaleX = Math.max(triggerRect.width / layerRect.width, 0.18);
            const scaleY = Math.max(triggerRect.height / layerRect.height, 0.18);
            subjectUpcomingLayer.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scaleX}, ${scaleY})`;
        } else {
            subjectUpcomingLayer.style.transform = "translateX(18px) scale(0.94)";
        }

        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                subjectUpcomingLayer.style.transition = [
                    `opacity ${Math.round(openMs * 0.8)}ms cubic-bezier(0.2,0.8,0.2,1)`,
                    `transform ${openMs}ms cubic-bezier(0.18,0.9,0.2,1)`,
                ].join(",");
                subjectUpcomingLayer.style.opacity = "1";
                subjectUpcomingLayer.style.transform = "translate(0, 0) scale(1)";
            });
        });

        window.setTimeout(() => {
            subjectUpcomingLayer.scrollTop = 0;
        }, Math.round(openMs * 0.78));
    }

    function getLessonContext(cell) {
        const subjectName = cell.dataset.subjectShortName || cell.dataset.subjectName || "Lesson";
        const lessonOrder = cell.dataset.lessonOrder || "";
        const lessonDate = cell.dataset.lessonDate ? new Date(cell.dataset.lessonDate) : null;
        const dateLabel = lessonDate
            ? formatFullDateLabel(lessonDate)
            : (dayLabels[cell.dataset.dayKey] || "Current day");

        return {
            subjectName,
            lessonOrder,
            dateLabel,
            context: `${subjectName} • lesson ${lessonOrder} • ${dateLabel}`,
        };
    }

    function populateSubjectSideHomeworkForm() {
        if (!activeLessonCell) return;
        const lessonContext = getLessonContext(activeLessonCell);
        const subjectSideHomeworkFormRoot = document.querySelector('[data-priority-input-id="subject-side-homework-priority"]');

        if (subjectSideHomeworkPanelTitle) {
            subjectSideHomeworkPanelTitle.textContent = "Add homework";
        }
        if (subjectSideHomeworkContext) {
            subjectSideHomeworkContext.textContent = lessonContext.context;
        }
        if (subjectSideHomeworkTitleInput) {
            subjectSideHomeworkTitleInput.value = "";
        }
        if (subjectSideHomeworkDetailsInput) {
            subjectSideHomeworkDetailsInput.value = "";
        }
        setPriorityValue(subjectSideHomeworkPriorityInput, defaultHomeworkPriority);
        if (subjectSideHomeworkFormRoot) {
            subjectSideHomeworkFormRoot.dataset.homeworkMode = "homework";
            syncHomeworkFormMode(subjectSideHomeworkFormRoot);
        }
        if (subjectSideHomeworkStatusInput) {
            subjectSideHomeworkStatusInput.value = "Pending";
        }
    }

    function buildSubjectSideHomeworkPayload() {
        if (!activeLessonCell || !subjectSideHomeworkTitleInput || !subjectSideHomeworkDetailsInput || !subjectSideHomeworkPriorityInput) {
            return null;
        }

        const ids = getLessonIdentifiers(activeLessonCell);
        if (!ids || !ids.weekStart || !ids.dueDaySubjectId || !ids.subjectId) return null;

        return {
            title: String(subjectSideHomeworkTitleInput.value || "").trim(),
            details: String(subjectSideHomeworkDetailsInput.value || "").trim(),
            priority: subjectSideHomeworkPriorityInput.value || defaultHomeworkPriority,
            subjectId: Number(ids.subjectId),
            dueDaySubjectId: Number(ids.dueDaySubjectId),
            status: subjectSideHomeworkStatusInput ? (subjectSideHomeworkStatusInput.value || "Pending") : "Pending",
            weekStart: ids.weekStart,
        };
    }

    async function saveSubjectSideHomework() {
        if (!activeLessonCell || !subjectSideHomeworkSaveButton) return;
        const payload = buildSubjectSideHomeworkPayload();
        if (!payload) {
            showToast("error", "Homework context is missing.");
            return;
        }
        if (!payload.title) {
            showToast("error", "Title is required.");
            return;
        }

        try {
            subjectSideHomeworkSaveButton.disabled = true;
            subjectSideHomeworkSaveButton.textContent = "Saving...";
            await requestJson("/scholar-hub/homework", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            await loadHomeworkForWeek(payload.weekStart, activeLessonCell.closest(".hub-timetable"));
            showToast("success", "Homework saved.");
            closeSubjectSideModal();
        } catch (error) {
            console.error(error);
            showToast("error", "Failed to save homework.");
        } finally {
            subjectSideHomeworkSaveButton.disabled = false;
            subjectSideHomeworkSaveButton.textContent = "Save homework";
        }
    }

    function buildSubjectHomeworkDetailPayload() {
        if (!activeHomeworkDetailEntry || !subjectHomeworkDetailTitleInput || !subjectHomeworkDetailDetailsInput || !subjectHomeworkDetailPriorityInput) {
            return null;
        }

        return {
            title: String(subjectHomeworkDetailTitleInput.value || "").trim(),
            details: String(subjectHomeworkDetailDetailsInput.value || "").trim(),
            priority: subjectHomeworkDetailPriorityInput.value || defaultHomeworkPriority,
            subjectId: Number(activeHomeworkDetailEntry.homework.subjectId ?? activeHomeworkDetailEntry.cell?.dataset?.subjectId),
            dueDaySubjectId: Number(activeHomeworkDetailEntry.homework.dueDaySubjectId ?? activeHomeworkDetailEntry.homework.daySubjectId ?? activeHomeworkDetailEntry.cell?.dataset?.daySubjectId),
            status: subjectHomeworkDetailStatusInput ? (subjectHomeworkDetailStatusInput.value || "Pending") : "Pending",
            weekStart: activeHomeworkDetailEntry.homework.weekStart || activeHomeworkDetailEntry.cell?.dataset?.weekStart || activeLessonCell?.dataset?.weekStart || ""
        };
    }

    async function saveSubjectHomeworkDetail() {
        if (!activeHomeworkDetailEntry?.homework?.id || !subjectHomeworkDetailSaveButton) return;
        const payload = buildSubjectHomeworkDetailPayload();
        if (!payload) {
            showToast("error", "Homework context is missing.");
            return;
        }
        if (!payload.title) {
            showToast("error", "Title is required.");
            return;
        }

        try {
            subjectHomeworkDetailSaveButton.disabled = true;
            subjectHomeworkDetailSaveButton.textContent = "Saving...";
            await requestJson(`/scholar-hub/homework/${encodeURIComponent(activeHomeworkDetailEntry.homework.id)}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });
            await loadHomeworkForWeek(payload.weekStart, activeLessonCell?.closest(".hub-timetable"));
            showToast("success", "Homework updated.");
            closeSubjectHomeworkDetailModal();
        } catch (error) {
            console.error(error);
            showToast("error", "Failed to update homework.");
        } finally {
            subjectHomeworkDetailSaveButton.disabled = false;
            subjectHomeworkDetailSaveButton.textContent = "Save changes";
        }
    }

    async function deleteSubjectHomeworkDetail() {
        if (!activeHomeworkDetailEntry?.homework?.id || !subjectHomeworkDetailDeleteButton) return;
        const weekStart = activeHomeworkDetailEntry.homework.weekStart || activeHomeworkDetailEntry.cell?.dataset?.weekStart || activeLessonCell?.dataset?.weekStart;
        if (!weekStart) {
            showToast("error", "Homework week is missing.");
            return;
        }

        try {
            subjectHomeworkDetailDeleteButton.disabled = true;
            subjectHomeworkDetailDeleteButton.textContent = "Deleting...";
            await requestJson(`/scholar-hub/homework/${encodeURIComponent(activeHomeworkDetailEntry.homework.id)}`, {
                method: "DELETE"
            });
            await loadHomeworkForWeek(weekStart, activeLessonCell?.closest(".hub-timetable"));
            showToast("success", "Homework deleted.");
            closeSubjectHomeworkDetailModal();
        } catch (error) {
            console.error(error);
            showToast("error", "Failed to delete homework.");
        } finally {
            subjectHomeworkDetailDeleteButton.disabled = false;
            subjectHomeworkDetailDeleteButton.textContent = "Delete";
        }
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

        if (subjectModalShortName)  subjectModalShortName.textContent = shortName || "No short name";
        if (subjectModalRoom)       subjectModalRoom.textContent      = room;
        if (subjectModalTeacher)    subjectModalTeacher.textContent   = teacher;
        if (subjectModalAverage)    subjectModalAverage.textContent   = "N";
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
        activeLessonCell = cell;
        updateSubjectUpcomingWidget();

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

        const dialog   = subjectModal.querySelector(".subject-detail-modal__dialog:not(.subject-detail-modal__dialog--side)");
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
        subjectDialogLayout = { top: dTop, left: dLeft, width: dWidth, maxHeight: dMaxH };
        resetSubjectGradesModal();
        resetSubjectSideModal();
        resetSubjectUpcomingLayer();

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

        const dialog   = subjectModal.querySelector(".subject-detail-modal__dialog:not(.subject-detail-modal__dialog--side)");
        const backdrop = subjectModal.querySelector(".subject-modal__backdrop");
        const CLOSE_MS = 540;

        // Fade out dialog quickly
        if (dialog) {
            dialog.style.transition = "opacity 200ms ease, transform 200ms cubic-bezier(0.4,0,1,1)";
            dialog.style.opacity    = "0";
            dialog.style.transform  = "translateX(18px)";
        }
        closeSubjectUpcomingLayer();
        closeSubjectGradesModal();
        closeSubjectSideModal();
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
                resetSubjectGradesModal();
                resetSubjectSideModal();
                resetSubjectUpcomingLayer();
                if (backdrop) backdrop.removeAttribute("style");
                if (subjectModalPreview) subjectModalPreview.removeAttribute("style");
                subjectDialogLayout = null;
            }, 60 + CLOSE_MS + 40);

        } else {
            // No card animation active — just close
            window.setTimeout(() => {
                subjectModal.classList.add("hidden");
                subjectModal.setAttribute("aria-hidden", "true");
                cardAnimBusy = false;
                if (dialog)   dialog.removeAttribute("style");
                resetSubjectGradesModal();
                resetSubjectSideModal();
                resetSubjectUpcomingLayer();
                if (backdrop) backdrop.removeAttribute("style");
                subjectDialogLayout = null;
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
                const dueDaySubjectId = item?.dueDaySubjectId ?? item?.daySubjectId;
                if (!item || !dueDaySubjectId) return;
                const key = makeCacheKey(weekStart, dueDaySubjectId);
                if (!key) return;
                const cached = homeworkCache.get(key) || [];
                cached.push(item);
                homeworkCache.set(key, cached);
            });
            refreshHomeworkIndicators(scope);
            updateSubjectUpcomingWidget();
            renderSubjectUpcomingLayer();
            if (activeWeekTimetable && scope === activeWeekTimetable && !weekHomeworkModal?.classList.contains("hidden")) {
                renderWeekHomeworkModal();
            }
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
        const lessonContext = getLessonContext(cell);
        const homeworkFormRoot = document.querySelector('[data-priority-input-id="homework-priority"]');

        homeworkContext.textContent    = lessonContext.context;
        homeworkTitleInput.value       = homework ? homework.title   || "" : "";
        homeworkDetailsInput.value     = homework ? homework.details || "" : "";
        setPriorityValue(homeworkPriorityInput, homework?.priority || defaultHomeworkPriority);
        if (homeworkFormRoot) {
            homeworkFormRoot.dataset.homeworkMode = "homework";
            syncHomeworkFormMode(homeworkFormRoot);
        }
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
        if (!ids.dueDaySubjectId)    { showToast("error", "Due day subject id is missing."); return; }
        if (!ids.subjectId)          { showToast("error", "Subject id is missing."); return; }

        const title    = String(homeworkTitleInput.value || "").trim();
        const details  = String(homeworkDetailsInput.value || "").trim();
        const priority = homeworkPriorityInput.value || defaultHomeworkPriority;
        const status   = homeworkStatusInput ? (homeworkStatusInput.value || "Pending") : "Pending";
        if (!title) { showToast("error", "Title is required."); return; }

        const payload = {
            title, details, priority,
            subjectId: Number(ids.subjectId),
            dueDaySubjectId: Number(ids.dueDaySubjectId),
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
        const weekStateBadge = weekNav.querySelector("[data-week-current-badge]");
        const currentWeekButton = weekNav.querySelector("[data-week-current-action]");
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
            if (weekStateBadge) {
                const showWeekStateBadge = weekOffset === 0 || Math.abs(weekOffset) === 1;
                weekStateBadge.classList.toggle("hidden", !showWeekStateBadge);
                if (showWeekStateBadge) {
                    const weekState = weekOffset === 0 ? "current" : (weekOffset < 0 ? "previous" : "next");
                    weekStateBadge.dataset.weekState = weekState;
                    weekStateBadge.textContent = weekState === "current"
                        ? "Current week"
                        : (weekState === "previous" ? "Previous week" : "Next week");
                }
            }
            if (currentWeekButton) {
                currentWeekButton.classList.toggle("hidden", weekOffset === 0);
            }
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

        if (currentWeekButton) {
            currentWeekButton.addEventListener("click", () => {
                if (weekOffset === 0) return;
                weekOffset = 0;
                applyWeekContext();
                syncUrl();
            });
        }

        applyWeekContext();
        syncUrl();
    });

    /* ─────────────────────────────────────────────────
       EVENT LISTENERS
    ───────────────────────────────────────────────── */

    openWeekHomeworkTriggers.forEach((trigger) => {
        trigger.addEventListener("click", () => {
            const timetable = document.querySelector("#schedule-preview .hub-timetable");
            if (!timetable) return;
            openWeekHomeworkModal(timetable, trigger);
        });
    });

    closeWeekHomeworkTriggers.forEach((trigger) => {
        trigger.addEventListener("click", closeWeekHomeworkModal);
    });

    document.querySelectorAll("[data-priority-picker]").forEach((picker) => {
        const inputId = picker.getAttribute("data-priority-picker");
        const input = inputId ? document.getElementById(inputId) : null;
        if (!input) return;

        syncPriorityPicker(inputId);

        picker.querySelectorAll("[data-priority-value]").forEach((button) => {
            button.addEventListener("click", () => {
                setPriorityValue(input, button.dataset.priorityValue || defaultHomeworkPriority);
            });
        });
    });

    homeworkFormRoots.forEach((root) => {
        syncHomeworkFormMode(root);

        root.querySelectorAll("[data-homework-mode-trigger]").forEach((button) => {
            button.addEventListener("click", () => {
                setHomeworkFormMode(root, button.getAttribute("data-homework-mode-trigger") || "homework");
            });
        });
    });

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
    closeSubjectTriggers.forEach((t)  => t.addEventListener("click", (event) => {
        if (event.currentTarget.classList.contains("subject-modal__backdrop")) {
            if (isSubjectHomeworkDetailModalOpen()) {
                closeSubjectHomeworkDetailModal();
                return;
            }
            if (isSubjectUpcomingLayerOpen()) {
                closeSubjectUpcomingLayer();
                return;
            }
            if (isSubjectGradesModalOpen()) {
                closeSubjectGradesModal();
                return;
            }
            if (isSubjectSideModalOpen()) {
                closeSubjectSideModal();
                return;
            }
        }
        closeSubjectModal();
    }));
    closeSubjectGradesTriggers.forEach((t) => t.addEventListener("click", closeSubjectGradesModal));
    closeSubjectSideTriggers.forEach((t) => t.addEventListener("click", closeSubjectSideModal));
    closeSubjectUpcomingTriggers.forEach((t) => t.addEventListener("click", closeSubjectUpcomingLayer));
    closeSubjectHomeworkDetailTriggers.forEach((t) => t.addEventListener("click", () => closeSubjectHomeworkDetailModal()));

    if (subjectGradesTrigger) {
        subjectGradesTrigger.addEventListener("click", (e) => {
            e.preventDefault();
            e.stopPropagation();
            toggleSubjectGradesModal();
        });
    }

    if (subjectSideTrigger) {
        subjectSideTrigger.addEventListener("click", (e) => {
            e.preventDefault();
            e.stopPropagation();
            toggleSubjectSideModal();
        });
    }

    if (subjectUpcomingWidget) {
        subjectUpcomingWidget.addEventListener("click", (event) => {
            if (event.target.closest("[data-open-subject-side-modal]")) return;
            if (isSubjectUpcomingLayerOpen()) {
                closeSubjectUpcomingLayer();
            } else {
                openSubjectUpcomingLayer();
            }
        });

        subjectUpcomingWidget.addEventListener("keydown", (event) => {
            if (event.key !== "Enter" && event.key !== " ") return;
            event.preventDefault();
            if (isSubjectUpcomingLayerOpen()) {
                closeSubjectUpcomingLayer();
            } else {
                openSubjectUpcomingLayer();
            }
        });
    }

    subjectHomeworkTabButtons.forEach((button) => {
        button.addEventListener("click", () => {
            setSubjectUpcomingTab(button.dataset.subjectHomeworkTab || "current");
        });
    });

    document.addEventListener("click", (event) => {
        if (event.target.closest(".subject-homework-item__status-menu")) return;
        closeAllStatusMenus();
    });

    if (homeworkSaveButton)   homeworkSaveButton.addEventListener("click",   () => { void saveHomework(); });
    if (homeworkDeleteButton) homeworkDeleteButton.addEventListener("click", () => { void deleteHomework(); });
    if (subjectSideHomeworkSaveButton) {
        subjectSideHomeworkSaveButton.addEventListener("click", () => { void saveSubjectSideHomework(); });
    }
    if (subjectHomeworkDetailSaveButton) {
        subjectHomeworkDetailSaveButton.addEventListener("click", () => { void saveSubjectHomeworkDetail(); });
    }
    if (subjectHomeworkDetailDeleteButton) {
        subjectHomeworkDetailDeleteButton.addEventListener("click", () => { void deleteSubjectHomeworkDetail(); });
    }

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            if (homeworkModal && !homeworkModal.classList.contains("hidden")) closeHomeworkModal();
            else if (isSubjectHomeworkDetailModalOpen()) closeSubjectHomeworkDetailModal();
            else if (isSubjectUpcomingLayerOpen()) closeSubjectUpcomingLayer();
            else if (isSubjectGradesModalOpen()) closeSubjectGradesModal();
            else if (subjectSideModal && !subjectSideModal.classList.contains("hidden")) closeSubjectSideModal();
            else if (subjectModal && !subjectModal.classList.contains("hidden")) closeSubjectModal();
        }
    });
});
