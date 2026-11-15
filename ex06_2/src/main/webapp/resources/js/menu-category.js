(function () {
    'use strict';
    var windows = Object.create(null);
    var requests = Object.create(null);
    var links = document.querySelectorAll('.menu-popup-link');
    Array.prototype.forEach.call(links, function (link) {
        link.addEventListener('click', function (event) {
            var id = link.getAttribute('data-menu-id');
            var left = Math.max(0, (window.screenX || 0) + (window.outerWidth - 520) / 2);
            var top = Math.max(0, (window.screenY || 0) + (window.outerHeight - 760) / 2);
            try {
                var popup = windows[id];
                if (!popup || popup.closed) {
                    // Synchronous user gesture: the anchor is the current-tab fallback if blocked.
                    popup = window.open(link.href, 'menu_review_' + id,
                        'popup=yes,width=520,height=760,left=' + Math.round(left) + ',top=' + Math.round(top) +
                        ',scrollbars=yes,resizable=yes');
                }
                if (popup) {
                    windows[id] = popup;
                    event.preventDefault();
                    popup.focus();
                }
            } catch (ignored) { /* The normal link remains usable when window.open is unavailable. */ }
        });
    });

    window.addEventListener('message', function (event) {
        var data = event.data;
        if (event.origin !== window.location.origin || !data || typeof data !== 'object' ||
                Array.isArray(data) || data.type !== 'menu-review-saved' ||
                typeof data.menuId !== 'string' || !/^[1-9][0-9]{0,18}$/.test(data.menuId) ||
                !windows[data.menuId] || event.source !== windows[data.menuId]) return;
        Array.prototype.forEach.call(links, function (link) {
            if (link.getAttribute('data-menu-id') !== data.menuId) return;
            var version = (requests[data.menuId] || 0) + 1;
            requests[data.menuId] = version;
            // Fetch authoritative values, rather than trusting a rating supplied in a message.
            var xhr = new XMLHttpRequest();
            xhr.open('GET', link.href + '/summary', true);
            xhr.setRequestHeader('Accept', 'application/json');
            xhr.onload = function () {
                if (xhr.status !== 200 || requests[data.menuId] !== version) return;
                try {
                    var summary = JSON.parse(xhr.responseText);
                    if (typeof summary.avgRating !== 'number' || !isFinite(summary.avgRating) ||
                            summary.avgRating < 0 || summary.avgRating > 5) return;
                    link.closest('tr').querySelector('.menu-average').textContent = summary.avgRating.toFixed(2);
                } catch (ignored) { /* A later successful change or refresh retries the summary. */ }
            };
            xhr.send();
        });
    });
}());
