document.addEventListener("DOMContentLoaded", () => {
    const modal = document.querySelector("[data-criterion-modal]");
    const title = modal?.querySelector("[data-criterion-modal-title]");
    const description = modal?.querySelector("[data-criterion-modal-description]");
    const closeButtons = modal ? Array.from(modal.querySelectorAll("[data-criterion-modal-close]")) : [];
    const closeButton = modal?.querySelector(".criterion-modal__close");
    const interactiveSelector = "a, button, input, textarea, select, label";
    const fallbackDescription = "Description is not available yet.";
    let lastFocusedElement = null;

    if (!modal || !title || !description) {
        return;
    }

    function getCardPayload(card) {
        if (!card) {
            return null;
        }

        const name = String(
            card.dataset.criterionName
            || card.querySelector(".criteria-name")?.textContent
            || "Criterion"
        ).trim() || "Criterion";

        const details = String(
            card.dataset.criterionDescription
            || card.querySelector(".criteria-desc")?.textContent
            || fallbackDescription
        ).trim() || fallbackDescription;

        return { name, details };
    }

    function openModal(card) {
        const payload = getCardPayload(card);
        if (!payload) {
            return;
        }

        const activeElement = document.activeElement instanceof HTMLElement ? document.activeElement : null;
        lastFocusedElement = card instanceof HTMLElement ? card : activeElement;
        title.textContent = payload.name;
        description.textContent = payload.details;
        modal.hidden = false;
        modal.setAttribute("aria-hidden", "false");
        document.documentElement.classList.add("criterion-modal-open");
        document.body.classList.add("criterion-modal-open");

        requestAnimationFrame(() => {
            modal.classList.add("is-open");
            closeButton?.focus();
        });
    }

    function closeModal() {
        if (modal.hidden) {
            return;
        }

        modal.classList.remove("is-open");
        modal.setAttribute("aria-hidden", "true");
        modal.hidden = true;
        document.documentElement.classList.remove("criterion-modal-open");
        document.body.classList.remove("criterion-modal-open");

        if (lastFocusedElement && typeof lastFocusedElement.focus === "function") {
            lastFocusedElement.focus();
        }
    }

    document.addEventListener("click", (event) => {
        const target = event.target;
        if (!(target instanceof Element)) {
            return;
        }

        const card = target.closest("[data-criterion-card]");
        if (!card) {
            return;
        }

        if (target.closest(interactiveSelector)) {
            return;
        }

        openModal(card);
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && !modal.hidden) {
            closeModal();
            return;
        }

        if (event.key !== "Enter" && event.key !== " ") {
            return;
        }

        const target = event.target;
        if (!(target instanceof Element)) {
            return;
        }

        if (target.matches(interactiveSelector)) {
            return;
        }

        const card = target.closest("[data-criterion-card]");
        if (!card) {
            return;
        }

        event.preventDefault();
        openModal(card);
    });

    closeButtons.forEach((button) => {
        button.addEventListener("click", closeModal);
    });
});
