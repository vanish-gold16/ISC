/**
 * opuscore-transition.js
 * src/main/resources/static/js/private/opuscore-transition.js
 *
 * Add ONE line at the end of your header fragment (after header-settings.js):
 *   <script src="/js/private/opuscore-transition.js"></script>
 *
 * ── BACKGROUND IMAGE ──────────────────────────────────────────────────────
 * Place image at:  src/main/resources/static/images/private/opuscore-bg.jpg
 * Then change BG:  const BG = "url('/images/private/opuscore-bg.jpg') center/cover no-repeat";
 * ──────────────────────────────────────────────────────────────────────────
 */
(function () {
    'use strict';

    /* ── CONFIG ── */
    const BG = "url('/images/private/opuscore-bg.png') center/cover no-repeat";
    const REVEAL_MS = 780;   /* duration of clip-path expansion          */
    const NAV_DELAY = 520;   /* navigate this many ms after reveal starts */

    /* ── CSS ── */
    const style = document.createElement('style');
    style.textContent = `
    /* Badge spring-squish */
    .topbar-opuscore.oc-squish {
      animation: ocSquish 400ms cubic-bezier(0.34,1.56,0.64,1) forwards;
      transform-origin: center;
    }
    @keyframes ocSquish {
      0%   { transform: scale(1)    rotate(0deg);  }
      20%  { transform: scale(0.70) rotate(-4deg); }
      42%  { transform: scale(0.50) rotate(2deg);  }
      58%  { transform: scale(0.40);               }
      100% { transform: scale(0.40);               }
    }

    /* Full-screen circle reveal */
    #oc-reveal {
      position: fixed;
      inset: 0;
      z-index: 9999;
      pointer-events: none;
      background: ${BG};
      clip-path: circle(0% at var(--ox,50%) var(--oy,50%));
      will-change: clip-path;
    }
    #oc-reveal.oc-open {
      clip-path: circle(180% at var(--ox,50%) var(--oy,50%));
      transition: clip-path ${REVEAL_MS}ms cubic-bezier(0.16,1,0.3,1);
    }
    /* Subtle inner depth */
    #oc-reveal::after {
      content: '';
      position: absolute;
      inset: 0;
      background: radial-gradient(
        ellipse 70% 70% at var(--ox,50%) var(--oy,50%),
        transparent 30%, rgba(0,0,0,0.4) 100%
      );
    }

    /* Ripple ring that fires from the badge center */
    #oc-ring {
      position: fixed;
      border-radius: 50%;
      border: 1.5px solid rgba(255,255,255,0.6);
      box-shadow: 0 0 12px 2px rgba(140,170,255,0.3);
      width: 0; height: 0;
      transform: translate(-50%,-50%);
      pointer-events: none;
      z-index: 10000;
      opacity: 0;
    }
    #oc-ring.oc-burst {
      animation: ocRing 580ms cubic-bezier(0.16,1,0.3,1) forwards;
    }
    @keyframes ocRing {
      0%   { width: 0;     height: 0;     opacity: 1;   }
      55%  {                              opacity: 0.45; }
      100% { width: 300px; height: 300px; opacity: 0;   }
    }
  `;
    document.head.appendChild(style);

    /* ── DOM ── */
    const reveal = document.createElement('div'); reveal.id = 'oc-reveal';
    const ring   = document.createElement('div'); ring.id   = 'oc-ring';
    document.body.appendChild(reveal);
    document.body.appendChild(ring);

    /* ── HOOK ── */
    function hook() {
        const badge = document.querySelector('.topbar-opuscore');
        if (!badge) return;

        badge.addEventListener('click', function (e) {
            e.preventDefault();
            if (badge.classList.contains('oc-squish')) return;

            const href = badge.getAttribute('href') || '/opuscore';
            const rect = badge.getBoundingClientRect();
            const cx   = rect.left + rect.width  / 2;
            const cy   = rect.top  + rect.height / 2;

            /* Percentage origin so clip-path scales correctly */
            reveal.style.setProperty('--ox', (cx / window.innerWidth  * 100).toFixed(2) + '%');
            reveal.style.setProperty('--oy', (cy / window.innerHeight * 100).toFixed(2) + '%');

            /* 1 — badge squishes inward */
            badge.classList.add('oc-squish');

            /* 2 — at the tightest squish frame, the reveal erupts from that point */
            setTimeout(() => {
                /* ripple ring */
                ring.style.left = cx + 'px';
                ring.style.top  = cy + 'px';
                void ring.offsetWidth;
                ring.classList.add('oc-burst');

                /* circle reveal */
                void reveal.offsetWidth;
                reveal.classList.add('oc-open');
            }, 210); /* timed to the 42% keyframe where scale is 0.50 */

            /* 3 — navigate once the wave covers most of the screen */
            setTimeout(() => {
                window.location.href = href;
            }, 210 + NAV_DELAY);
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', hook);
    } else {
        hook();
    }
})();