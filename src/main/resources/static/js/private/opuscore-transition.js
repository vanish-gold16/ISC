(function () {
    'use strict';

    const STORAGE_KEY = 'opuscore:transition';
    const ENTRY_WINDOW_MS = 2600;
    const BG = "linear-gradient(180deg, rgba(6, 9, 14, 0.18) 0%, rgba(6, 9, 14, 0.56) 100%), url('/images/private/opuscore-bg.png') center/cover no-repeat";
    const SQUISH_DELAY = 180;
    const REVEAL_MS = 920;
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

    const style = document.createElement('style');
    style.textContent = `
    html.oc-is-transitioning,
    html.oc-entry-pending {
      overflow-x: hidden;
    }

    body.oc-is-transitioning > :not(script):not(#oc-reveal):not(#oc-ring):not(#oc-flash) {
      animation: ocFadeOut 680ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
    }

    @keyframes ocFadeOut {
      from { opacity: 1; transform: none; filter: none; }
      to { opacity: 0.08; transform: translateY(18px) scale(0.985); filter: blur(12px); }
    }

    .topbar-opuscore.oc-squish {
      animation: ocSquish 440ms cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
      transform-origin: center;
    }

    @keyframes ocSquish {
      0% { transform: scale(1) rotate(0deg); opacity: 1; }
      24% { transform: scale(0.78) rotate(-3deg); }
      48% { transform: scale(0.56) rotate(2deg); }
      100% { transform: scale(0.42); opacity: 0.16; filter: blur(5px); }
    }

    #oc-reveal {
      position: fixed;
      inset: 0;
      z-index: 9998;
      pointer-events: none;
      background: ${BG};
      opacity: 0;
      clip-path: circle(var(--or-start, 0px) at var(--ox, 50%) var(--oy, 50%));
      transform: scale(0.9);
      transform-origin: var(--ox, 50%) var(--oy, 50%);
      filter: saturate(1.08) contrast(1.02);
      will-change: clip-path, transform, opacity;
    }

    #oc-reveal::before {
      content: '';
      position: absolute;
      inset: -10%;
      background:
        radial-gradient(circle at var(--ox, 50%) var(--oy, 50%), rgba(255, 255, 255, 0.24) 0%, rgba(255, 255, 255, 0.08) 16%, transparent 42%),
        radial-gradient(circle at center, transparent 40%, rgba(2, 4, 8, 0.28) 100%);
      opacity: 0.92;
    }

    #oc-reveal.oc-open {
      opacity: 1;
      clip-path: circle(var(--or-end, 0px) at var(--ox, 50%) var(--oy, 50%));
      transform: scale(1.03);
      transition:
        clip-path ${REVEAL_MS}ms cubic-bezier(0.16, 1, 0.3, 1),
        transform ${REVEAL_MS + 180}ms cubic-bezier(0.18, 0.9, 0.2, 1),
        opacity 220ms ease-out;
    }

    #oc-flash {
      position: fixed;
      inset: 0;
      z-index: 9999;
      pointer-events: none;
      opacity: 0;
      background: radial-gradient(circle at var(--ox, 50%) var(--oy, 50%), rgba(255, 255, 255, 0.24), rgba(255, 255, 255, 0.12) 10%, rgba(255, 255, 255, 0) 34%);
      transform: scale(0.24);
    }

    #oc-flash.oc-burst {
      animation: ocFlash 540ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }

    @keyframes ocFlash {
      0% { opacity: 0; transform: scale(0.24); }
      18% { opacity: 1; }
      100% { opacity: 0; transform: scale(2.4); }
    }

    #oc-ring {
      position: fixed;
      z-index: 10000;
      pointer-events: none;
      opacity: 0;
      width: 0;
      height: 0;
      left: 0;
      top: 0;
      transform: translate(-50%, -50%);
      border-radius: 50%;
      border: 1px solid rgba(255, 255, 255, 0.5);
      box-shadow:
        0 0 0 1px rgba(173, 193, 255, 0.22),
        0 0 36px rgba(108, 142, 255, 0.34);
    }

    #oc-ring.oc-burst {
      animation: ocRing 760ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }

    @keyframes ocRing {
      0% { width: 24px; height: 24px; opacity: 0; }
      16% { opacity: 1; }
      100% { width: 380px; height: 380px; opacity: 0; }
    }

    @media (prefers-reduced-motion: reduce) {
      body.oc-is-transitioning > :not(script):not(#oc-reveal):not(#oc-ring):not(#oc-flash) {
        animation: none;
      }

      .topbar-opuscore.oc-squish {
        animation: none;
      }

      #oc-reveal,
      #oc-ring,
      #oc-flash {
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
        if (!hasFreshTransitionState(state)) {
            clearTransitionState();
            root.classList.remove('oc-entry-pending');
            return;
        }

        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                root.classList.remove('oc-entry-pending');
                clearTransitionState();
            });
        });
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
            const cx = rect.left + rect.width / 2;
            const cy = rect.top + rect.height / 2;
            const maxX = Math.max(cx, window.innerWidth - cx);
            const maxY = Math.max(cy, window.innerHeight - cy);
            const endRadius = Math.hypot(maxX, maxY) + 120;
            const startRadius = Math.max(rect.width, rect.height) * 0.35;

            reveal.style.setProperty('--ox', `${cx}px`);
            reveal.style.setProperty('--oy', `${cy}px`);
            reveal.style.setProperty('--or-start', `${startRadius}px`);
            reveal.style.setProperty('--or-end', `${endRadius}px`);
            flash.style.setProperty('--ox', `${cx}px`);
            flash.style.setProperty('--oy', `${cy}px`);

            writeTransitionState({
                href,
                ts: Date.now()
            });

            root.classList.add('oc-is-transitioning');
            document.body.classList.add('oc-is-transitioning');
            badge.classList.add('oc-squish');

            setTimeout(() => {
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

            setTimeout(() => {
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
