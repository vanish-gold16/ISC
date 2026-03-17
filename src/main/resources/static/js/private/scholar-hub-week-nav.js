document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-week-nav]").forEach((weekNav) => {
        const rangeLabel = weekNav.querySelector("[data-week-range]");
        const timetable = weekNav.parentElement ? weekNav.parentElement.querySelector(".hub-timetable") : null;
        if (!rangeLabel || !timetable) {
            return;
        }

        const dayOrder = {
            MONDAY: 0,
            TUESDAY: 1,
            WEDNESDAY: 2,
            THURSDAY: 3,
            FRIDAY: 4,
            SATURDAY: 5,
            SUNDAY: 6
        };

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

        const formatter = new Intl.DateTimeFormat("ru-RU", {
            day: "2-digit",
            month: "2-digit"
        });

        const getMonday = (date) => {
            const monday = new Date(date);
            const day = monday.getDay();
            const diff = day === 0 ? -6 : 1 - day;
            monday.setHours(0, 0, 0, 0);
            monday.setDate(monday.getDate() + diff);
            return monday;
        };

        const formatRange = () => {
            const monday = getMonday(new Date());
            monday.setDate(monday.getDate() + weekOffset * 7);

            const start = new Date(monday);
            start.setDate(start.getDate() + firstDayIndex);

            const end = new Date(monday);
            end.setDate(end.getDate() + lastDayIndex);

            rangeLabel.textContent = `${formatter.format(start)}-${formatter.format(end)}`;
        };

        const syncUrl = () => {
            if (weekOffset === 0) {
                url.searchParams.delete("weekOffset");
            } else {
                url.searchParams.set("weekOffset", String(weekOffset));
            }
            window.history.replaceState({}, "", url);
        };

        weekNav.querySelectorAll("[data-week-shift]").forEach((button) => {
            button.addEventListener("click", () => {
                const shift = Number.parseInt(button.dataset.weekShift || "0", 10);
                if (Number.isNaN(shift) || shift === 0) {
                    return;
                }

                weekOffset += shift;
                formatRange();
                syncUrl();
            });
        });

        formatRange();
        syncUrl();
    });
});
