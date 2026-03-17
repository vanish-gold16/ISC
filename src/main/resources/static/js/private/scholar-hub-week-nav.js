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

    const homeworkStorageKey = "scholarHubHomeworkStore";
    const homeworkModal = document.getElementById("homework-modal");
    const homeworkTitleInput = document.getElementById("homework-title");
    const homeworkDetailsInput = document.getElementById("homework-details");
    const homeworkPriorityInput = document.getElementById("homework-priority");
    const homeworkContext = document.getElementById("homework-modal-context");
    const homeworkSaveButton = document.getElementById("homework-save");
    const homeworkDeleteButton = document.getElementById("homework-delete");
    const closeHomeworkTriggers = Array.from(document.querySelectorAll("[data-close-homework-modal]"));
    let homeworkStore = readHomeworkStore();
    let activeLessonCell = null;

    function readHomeworkStore() {
        try {
            const rawValue = window.localStorage.getItem(homeworkStorageKey);
            if (!rawValue) {
                return {};
            }
            const parsedValue = JSON.parse(rawValue);
            return parsedValue && typeof parsedValue === "object" ? parsedValue : {};
        } catch (error) {
            return {};
        }
    }

    function persistHomeworkStore() {
        try {
            window.localStorage.setItem(homeworkStorageKey, JSON.stringify(homeworkStore));
        } catch (error) {
            console.error(error);
        }
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

    function buildHomeworkKey(cell) {
        const weekStart = cell.dataset.weekStart;
        const dayKey = cell.dataset.dayKey;
        const lessonOrder = cell.dataset.lessonOrder;
        if (!weekStart || !dayKey || !lessonOrder) {
            return null;
        }
        return `${weekStart}|${dayKey}|${lessonOrder}`;
    }

    function getHomeworkEntry(cell) {
        const key = buildHomeworkKey(cell);
        return key ? homeworkStore[key] || null : null;
    }

    function renderHomeworkIndicator(cell) {
        const indicator = cell.querySelector("[data-homework-indicator]");
        if (!indicator) {
            return;
        }

        indicator.classList.remove(
            "hidden",
            "hub-timetable__homework-dot--low",
            "hub-timetable__homework-dot--medium",
            "hub-timetable__homework-dot--high"
        );

        const homework = getHomeworkEntry(cell);
        if (!homework || (!homework.title && !homework.details)) {
            indicator.classList.add("hidden");
            return;
        }

        indicator.classList.add(`hub-timetable__homework-dot--${homework.priority || "low"}`);
    }

    function refreshHomeworkIndicators(scope) {
        const root = scope || document;
        root.querySelectorAll(".hub-timetable__cell--filled[data-week-start]").forEach((cell) => {
            renderHomeworkIndicator(cell);
        });
    }

    function closeHomeworkModal() {
        if (!homeworkModal) {
            return;
        }
        homeworkModal.classList.add("hidden");
        homeworkModal.setAttribute("aria-hidden", "true");
        activeLessonCell = null;
    }

    function openHomeworkModal(cell) {
        if (!homeworkModal || !homeworkTitleInput || !homeworkDetailsInput || !homeworkPriorityInput || !homeworkContext) {
            return;
        }

        activeLessonCell = cell;
        const homework = getHomeworkEntry(cell);
        const subjectName = cell.dataset.subjectShortName || cell.dataset.subjectName || "Lesson";
        const lessonOrder = cell.dataset.lessonOrder || "";
        const lessonDate = cell.dataset.lessonDate ? new Date(cell.dataset.lessonDate) : null;
        const dateLabel = lessonDate ? formatFullDateLabel(lessonDate) : (dayLabels[cell.dataset.dayKey] || "Current day");

        homeworkContext.textContent = `${subjectName} • lesson ${lessonOrder} • ${dateLabel}`;
        homeworkTitleInput.value = homework ? homework.title || "" : "";
        homeworkDetailsInput.value = homework ? homework.details || "" : "";
        homeworkPriorityInput.value = homework ? homework.priority || "low" : "low";
        if (homeworkDeleteButton) {
            homeworkDeleteButton.disabled = !homework;
        }

        homeworkModal.classList.remove("hidden");
        homeworkModal.setAttribute("aria-hidden", "false");
        window.setTimeout(() => homeworkTitleInput.focus(), 0);
    }

    function saveHomework() {
        if (!activeLessonCell || !homeworkTitleInput || !homeworkDetailsInput || !homeworkPriorityInput) {
            return;
        }

        const key = buildHomeworkKey(activeLessonCell);
        if (!key) {
            return;
        }

        const title = String(homeworkTitleInput.value || "").trim();
        const details = String(homeworkDetailsInput.value || "").trim();
        const priority = homeworkPriorityInput.value || "low";

        if (!title && !details) {
            delete homeworkStore[key];
            persistHomeworkStore();
            refreshHomeworkIndicators();
            closeHomeworkModal();
            return;
        }

        homeworkStore[key] = {
            title,
            details,
            priority,
            subjectName: activeLessonCell.dataset.subjectName || "",
            weekStart: activeLessonCell.dataset.weekStart || "",
            dayKey: activeLessonCell.dataset.dayKey || "",
            lessonOrder: activeLessonCell.dataset.lessonOrder || ""
        };
        persistHomeworkStore();
        refreshHomeworkIndicators();
        closeHomeworkModal();
    }

    function deleteHomework() {
        if (!activeLessonCell) {
            return;
        }

        const key = buildHomeworkKey(activeLessonCell);
        if (!key) {
            return;
        }

        delete homeworkStore[key];
        persistHomeworkStore();
        refreshHomeworkIndicators();
        closeHomeworkModal();
    }

    document.querySelectorAll("[data-week-nav]").forEach((weekNav) => {
        const rangeLabel = weekNav.querySelector("[data-week-range]");
        const timetable = weekNav.parentElement ? weekNav.parentElement.querySelector(".hub-timetable") : null;
        if (!rangeLabel || !timetable) {
            return;
        }

        const dayIndexes = Array.from(timetable.querySelectorAll("[data-day-key]"))
            .map((element) => dayOrder[element.dataset.dayKey])
            .filter((value) => Number.isInteger(value))
            .sort((left, right) => left - right);

        const firstDayIndex = dayIndexes.length > 0 ? dayIndexes[0] : 0;
        const lastDayIndex = dayIndexes.length > 0 ? dayIndexes[dayIndexes.length - 1] : 5;
        const url = new URL(window.location.href);
        let weekOffset = Number.parseInt(url.searchParams.get("weekOffset") || "0", 10);
        if (Number.isNaN(weekOffset)) {
            weekOffset = 0;
        }

        function applyWeekContext() {
            const monday = getMonday(new Date());
            monday.setDate(monday.getDate() + weekOffset * 7);

            const start = addDays(monday, firstDayIndex);
            const end = addDays(monday, lastDayIndex);
            rangeLabel.textContent = `${formatDateLabel(start)}-${formatDateLabel(end)}`;

            timetable.querySelectorAll(".hub-timetable__cell--filled").forEach((cell) => {
                const dayIndex = dayOrder[cell.dataset.dayKey];
                if (!Number.isInteger(dayIndex)) {
                    return;
                }

                const lessonDate = addDays(monday, dayIndex);
                cell.dataset.weekStart = toIsoDate(monday);
                cell.dataset.lessonDate = lessonDate.toISOString();
            });

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
                if (Number.isNaN(shift) || shift === 0) {
                    return;
                }

                weekOffset += shift;
                applyWeekContext();
                syncUrl();
            });
        });

        applyWeekContext();
        syncUrl();
    });

    document.querySelectorAll("[data-homework-trigger]").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.preventDefault();
            event.stopPropagation();

            const lessonCell = button.closest(".hub-timetable__cell--filled");
            if (!lessonCell) {
                return;
            }

            openHomeworkModal(lessonCell);
        });
    });

    closeHomeworkTriggers.forEach((trigger) => {
        trigger.addEventListener("click", () => {
            closeHomeworkModal();
        });
    });

    if (homeworkSaveButton) {
        homeworkSaveButton.addEventListener("click", saveHomework);
    }

    if (homeworkDeleteButton) {
        homeworkDeleteButton.addEventListener("click", deleteHomework);
    }

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && homeworkModal && !homeworkModal.classList.contains("hidden")) {
            closeHomeworkModal();
        }
    });

    refreshHomeworkIndicators();
});
