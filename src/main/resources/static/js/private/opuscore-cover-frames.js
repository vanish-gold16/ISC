(function () {
    const MIN_RATIO = 3 / 4.4;

    function normalizeRatio(width, height) {
        if (!width || !height) {
            return null;
        }

        const rawRatio = width / height;
        const clampedRatio = Math.max(MIN_RATIO, rawRatio);
        return Math.round(clampedRatio * 1000) / 1000;
    }

    function applyFrameRatio(frame, image) {
        if (!frame || !image || !image.naturalWidth || !image.naturalHeight) {
            return;
        }

        const ratio = normalizeRatio(image.naturalWidth, image.naturalHeight);
        if (!ratio) {
            return;
        }

        frame.style.setProperty('--cover-frame-aspect', String(ratio));
        frame.dataset.coverShape = image.naturalWidth >= image.naturalHeight ? 'square' : 'portrait';
    }

    function bindFrame(frame) {
        if (!frame) {
            return;
        }

        const image = frame.matches('img') ? frame : frame.querySelector('[data-cover-image], img');
        const targetFrame = frame.matches('[data-cover-frame]') ? frame : frame.closest('[data-cover-frame]');
        if (!image || !targetFrame) {
            return;
        }

        const sync = function () {
            applyFrameRatio(targetFrame, image);
        };

        if (image.complete && image.naturalWidth) {
            sync();
            return;
        }

        image.addEventListener('load', sync, { once: true });
    }

    function applyCoverFrames(root) {
        const scope = root || document;
        if (scope instanceof Element && scope.matches('[data-cover-frame]')) {
            bindFrame(scope);
        }

        const frames = scope.querySelectorAll ? scope.querySelectorAll('[data-cover-frame]') : [];
        frames.forEach(bindFrame);
    }

    window.__opuscoreApplyCoverFrames = applyCoverFrames;
    window.__opuscoreApplyCoverFrame = bindFrame;

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () {
            applyCoverFrames(document);
        }, { once: true });
    } else {
        applyCoverFrames(document);
    }
})();
