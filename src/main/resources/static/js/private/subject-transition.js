document.addEventListener("DOMContentLoaded", () => {
    const target = document.getElementById("subject-transition-target");
    if (!target) {
        return;
    }

    const raw = sessionStorage.getItem("subjectTransition");
    if (!raw) {
        target.style.opacity = "1";
        return;
    }

    sessionStorage.removeItem("subjectTransition");

    let payload = null;
    try {
        payload = JSON.parse(raw);
    } catch (error) {
        console.warn(error);
        target.style.opacity = "1";
        return;
    }

    if (!payload || !payload.html || !payload.width || !payload.height) {
        target.style.opacity = "1";
        return;
    }

    const startX = 16;
    const startY = 16;
    const startScale = payload.scale || 1;
    const baseWidth = payload.width;
    const baseHeight = payload.height;

    const clone = document.createElement("div");
    clone.className = "hub-timetable__cell hub-timetable__cell--filled";
    clone.innerHTML = payload.html;
    clone.style.position = "fixed";
    clone.style.left = `${startX}px`;
    clone.style.top = `${startY}px`;
    clone.style.width = `${baseWidth}px`;
    clone.style.height = `${baseHeight}px`;
    clone.style.margin = "0";
    clone.style.zIndex = "999";
    clone.style.pointerEvents = "none";
    clone.style.transformOrigin = "top left";
    clone.style.boxShadow = "0 20px 60px rgba(17, 32, 63, 0.22)";

    if (payload.accent) {
        clone.style.setProperty("--preview-accent", payload.accent);
        target.style.setProperty("--preview-accent", payload.accent);
    }

    document.body.appendChild(clone);

    target.style.opacity = "0";
    target.innerHTML = payload.html;

    const targetRect = target.getBoundingClientRect();
    const translateX = targetRect.left - startX;
    const translateY = targetRect.top - startY;
    const scaleX = targetRect.width / baseWidth;
    const scaleY = targetRect.height / baseHeight;
    const endScale = Math.min(scaleX, scaleY);

    clone.style.transform = `scale(${startScale})`;

    const duration = 800;
    requestAnimationFrame(() => {
        clone.style.transition = `transform ${duration}ms cubic-bezier(0.2, 0.9, 0.2, 1), box-shadow ${duration}ms ease`;
        clone.style.transform = `translate(${translateX}px, ${translateY}px) scale(${endScale})`;
        clone.style.boxShadow = "0 14px 34px rgba(17, 32, 63, 0.16)";
    });

    window.setTimeout(() => {
        clone.remove();
        target.style.opacity = "1";
    }, duration + 40);
});
