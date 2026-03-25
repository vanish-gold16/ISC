(function () {
    'use strict';

    const STORAGE_KEY = 'opuscore:transition';
    const ENTRY_WINDOW_MS = 2600;
    const BG = "linear-gradient(180deg, rgba(5, 7, 11, 0.24) 0%, rgba(5, 7, 11, 0.68) 100%), radial-gradient(circle at top, rgba(108, 142, 255, 0.14), transparent 44%), radial-gradient(circle at 78% 18%, rgba(192, 132, 252, 0.09), transparent 30%), url('/images/private/opuscore-bg.png') center/cover no-repeat";
    const SQUISH_DELAY = 230;
    const NAV_DELAY = 620;
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const root = document.documentElement;

    function isOpusCorePath(pathname) {
        return pathname === '/opuscore' || pathname.startsWith('/opuscore/');
    }

    function readTransitionState() {
        try {
            const raw = sessionStorage.getItem(STORAGE_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch (error) {
            return null;
        }
    }

    function writeTransitionState(state) {
        try {
            sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
        } catch (error) {
            return;
        }
    }

    function clearTransitionState() {
        try {
            sessionStorage.removeItem(STORAGE_KEY);
        } catch (error) {
            return;
        }
    }

    function hasFreshTransitionState(state) {
        return Boolean(state && typeof state.ts === 'number' && (Date.now() - state.ts) < ENTRY_WINDOW_MS);
    }

    function toPercent(value) {
        return `${(value * 100).toFixed(3)}%`;
    }

    const style = document.createElement('style');
    style.textContent = `
    html.oc-is-transitioning,
    html.oc-is-transitioning body,
    html.oc-entry-pending,
    html.oc-entry-pending body {
      overflow: hidden;
    }

    .topbar-opuscore.oc-squish {
      animation: ocSquish 620ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
      transform-origin: center;
    }

    @keyframes ocSquish {
      0% { transform: scale(1) rotate(0deg); filter: none; opacity: 1; }
      18% { transform: scaleX(0.9) scaleY(0.96) rotate(-1deg); }
      38% { transform: scaleX(0.74) scaleY(0.9) rotate(-3deg); }
      56% { transform: scaleX(0.5) scaleY(1.12) rotate(2deg); }
      74% { transform: scaleX(0.46) scaleY(0.88) rotate(-1deg); }
      100% { transform: scaleX(0.42) scaleY(0.98); filter: blur(5px); opacity: 0.14; }
    }

    #oc-reveal,
    #oc-entry-screen {
      position: fixed;
      inset: 0;
      pointer-events: none;
      background: ${BG};
      z-index: 9998;
    }

    #oc-reveal {
      opacity: 0;
      clip-path: ellipse(var(--orx-start, 0px) var(--ory-start, 0px) at var(--ox, 50%) var(--oy, 50%));
      will-change: clip-path, opacity, transform;
      transform-origin: var(--ox, 50%) var(--oy, 50%);
      transform: scaleX(0.84) scaleY(1.14) rotate(-5deg);
      filter: saturate(1.08) contrast(1.02);
    }

    #oc-reveal::before,
    #oc-entry-screen::before {
      content: '';
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at var(--ox, 50%) var(--oy, 50%), rgba(146, 176, 255, 0.18) 0%, rgba(146, 176, 255, 0.08) 16%, rgba(146, 176, 255, 0) 40%),
        radial-gradient(circle at calc(var(--ox, 50%) - 5%) calc(var(--oy, 50%) - 7%), rgba(198, 166, 255, 0.08), transparent 24%);
      mix-blend-mode: screen;
      opacity: 0.7;
    }

    #oc-reveal::after,
    #oc-entry-screen::after {
      content: '';
      position: absolute;
      inset: 0;
      background: radial-gradient(circle at var(--ox, 50%) var(--oy, 50%), rgba(5, 7, 11, 0) 24%, rgba(5, 7, 11, 0.28) 100%);
    }

    #oc-reveal.oc-open {
      opacity: 1;
      clip-path: ellipse(var(--orx-end, 0px) var(--ory-end, 0px) at var(--ox, 50%) var(--oy, 50%));
      transform: scaleX(1.05) scaleY(0.98) rotate(0deg);
      transition:
        clip-path 980ms cubic-bezier(0.16, 1, 0.3, 1),
        opacity 180ms ease-out,
        transform 980ms cubic-bezier(0.16, 1, 0.3, 1);
    }

    #oc-entry-screen {
      opacity: 1;
      filter: blur(0);
      z-index: 9997;
    }

    #oc-entry-screen.oc-clear {
      opacity: 0;
      filter: blur(16px);
      transition:
        opacity 620ms cubic-bezier(0.22, 1, 0.36, 1),
        filter 760ms cubic-bezier(0.16, 1, 0.3, 1);
    }

    #oc-flash {
      position: fixed;
      inset: 0;
      z-index: 9999;
      pointer-events: none;
      opacity: 0;
      background:
        radial-gradient(circle at var(--ox, 50%) var(--oy, 50%), rgba(132, 164, 255, 0.2), rgba(132, 164, 255, 0.08) 14%, rgba(132, 164, 255, 0) 38%),
        radial-gradient(circle at calc(var(--ox, 50%) - 3%) calc(var(--oy, 50%) - 6%), rgba(196, 152, 255, 0.12), rgba(196, 152, 255, 0) 34%);
      transform: scaleX(0.16) scaleY(0.22);
    }

    #oc-flash.oc-burst {
      animation: ocFlash 760ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }

    @keyframes ocFlash {
      0% { opacity: 0; transform: scaleX(0.16) scaleY(0.22); }
      18% { opacity: 0.72; }
      52% { opacity: 0.34; transform: scaleX(1.4) scaleY(2.2); }
      100% { opacity: 0; transform: scaleX(2.8) scaleY(3.6); }
    }

    #oc-ring {
      position: fixed;
      left: 0;
      top: 0;
      width: 0;
      height: 0;
      z-index: 10000;
      border-radius: 50%;
      transform: translate(-50%, -50%);
      pointer-events: none;
      opacity: 0;
      border: 1px solid rgba(255, 255, 255, 0.48);
      box-shadow:
        0 0 0 1px rgba(173, 193, 255, 0.24),
        0 0 44px rgba(108, 142, 255, 0.28);
    }

    #oc-ring.oc-burst {
      animation: ocRing 920ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }

    @keyframes ocRing {
      0% { width: 18px; height: 22px; opacity: 0; transform: translate(-50%, -50%) scaleX(0.82) scaleY(1.12); }
      18% { opacity: 0.72; }
      56% { width: 240px; height: 340px; opacity: 0.26; transform: translate(-50%, -50%) scaleX(0.94) scaleY(1.08); }
      100% { width: 460px; height: 390px; opacity: 0; transform: translate(-50%, -50%) scaleX(1.08) scaleY(0.94); }
    }

    @media (prefers-reduced-motion: reduce) {
      .topbar-opuscore.oc-squish {
        animation: none;
      }

      #oc-reveal,
      #oc-ring,
      #oc-flash,
      #oc-entry-screen {
        display: none !important;
      }
    }
  `;
    document.head.appendChild(style);

    const reveal = document.createElement('div');
    reveal.id = 'oc-reveal';
    const flash = document.createElement('div');
    flash.id = 'oc-flash';
    const ring = document.createElement('div');
    ring.id = 'oc-ring';
    document.body.appendChild(reveal);
    document.body.appendChild(flash);
    document.body.appendChild(ring);

    function playEntryAnimation() {
        if (!isOpusCorePath(window.location.pathname)) {
            return;
        }

        const state = readTransitionState();
        if (!hasFreshTransitionState(state) || prefersReducedMotion) {
            clearTransitionState();
            root.classList.remove('oc-entry-pending');
            if ('scrollRestoration' in history) {
                history.scrollRestoration = 'auto';
            }
            return;
        }

        if ('scrollRestoration' in history) {
            history.scrollRestoration = 'manual';
        }
        window.scrollTo(0, 0);

        const entryScreen = document.createElement('div');
        entryScreen.id = 'oc-entry-screen';
        const ox = typeof state.ox === 'number' ? toPercent(state.ox) : '50%';
        const oy = typeof state.oy === 'number' ? toPercent(state.oy) : '50%';
        entryScreen.style.setProperty('--ox', ox);
        entryScreen.style.setProperty('--oy', oy);
        document.body.appendChild(entryScreen);

        requestAnimationFrame(() => {
            root.classList.remove('oc-entry-pending');
            requestAnimationFrame(() => {
                entryScreen.classList.add('oc-clear');
            });
        });

        window.setTimeout(() => {
            entryScreen.remove();
            clearTransitionState();
            if ('scrollRestoration' in history) {
                history.scrollRestoration = 'auto';
            }
        }, 760);
    }

    function hookBadgeTransition() {
        const badge = document.querySelector('.topbar-opuscore');
        if (!badge) return;

        badge.addEventListener('click', function (e) {
            if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
                return;
            }

            const href = badge.getAttribute('href') || '/opuscore';
            if (!href || !href.startsWith('/opuscore')) {
                return;
            }

            if (prefersReducedMotion) {
                clearTransitionState();
                return;
            }

            e.preventDefault();
            if (badge.classList.contains('oc-squish')) return;

            const rect = badge.getBoundingClientRect();
            const cx = rect.left + (rect.width / 2);
            const cy = rect.top + (rect.height / 2);
            const maxX = Math.max(cx, window.innerWidth - cx);
            const maxY = Math.max(cy, window.innerHeight - cy);
            const endRadiusX = maxX + 220;
            const endRadiusY = maxY + 220;
            const startRadiusX = Math.max(rect.width * 0.42, 14);
            const startRadiusY = Math.max(rect.height * 0.28, 10);
            const ox = toPercent(cx / window.innerWidth);
            const oy = toPercent(cy / window.innerHeight);

            reveal.style.setProperty('--ox', ox);
            reveal.style.setProperty('--oy', oy);
            reveal.style.setProperty('--orx-start', `${startRadiusX}px`);
            reveal.style.setProperty('--ory-start', `${startRadiusY}px`);
            reveal.style.setProperty('--orx-end', `${endRadiusX}px`);
            reveal.style.setProperty('--ory-end', `${endRadiusY}px`);
            flash.style.setProperty('--ox', ox);
            flash.style.setProperty('--oy', oy);

            writeTransitionState({
                href,
                ts: Date.now(),
                ox: cx / window.innerWidth,
                oy: cy / window.innerHeight
            });

            root.classList.add('oc-is-transitioning');
            document.body.classList.add('oc-is-transitioning');
            badge.classList.add('oc-squish');

            window.setTimeout(() => {
                ring.style.left = `${cx}px`;
                ring.style.top = `${cy}px`;
                flash.classList.remove('oc-burst');
                ring.classList.remove('oc-burst');
                reveal.classList.remove('oc-open');
                void flash.offsetWidth;
                void ring.offsetWidth;
                void reveal.offsetWidth;
                flash.classList.add('oc-burst');
                ring.classList.add('oc-burst');
                reveal.classList.add('oc-open');
            }, SQUISH_DELAY);

            window.setTimeout(() => {
                window.location.assign(href);
            }, SQUISH_DELAY + NAV_DELAY);
        });
    }

    playEntryAnimation();

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', hookBadgeTransition);
    } else {
        hookBadgeTransition();
    }
})();
