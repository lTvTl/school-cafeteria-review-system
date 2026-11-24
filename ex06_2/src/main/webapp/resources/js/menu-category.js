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
    // Hearts have their own buttons and handlers; only food-name links open review popups.
    Array.prototype.forEach.call(document.querySelectorAll('.bookmark-button'), function (button) {
        var root = button.closest('.food-category');
        var cell = button.closest('.menu-bookmarks');
        var busy = false;
        function message(text, login) {
            var feedback = root.querySelector('.bookmark-feedback');
            feedback.hidden = false;
            feedback.querySelector('.bookmark-feedback-text').textContent = text;
            feedback.querySelector('.bookmark-login').hidden = !login;
        }
        button.addEventListener('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            if (busy) return;
            if (root.getAttribute('data-bookmark-authenticated') !== 'true') {
                message('북마크하려면 로그인해 주세요.', true);
                return;
            }
            var id = button.getAttribute('data-menu-id');
            var add = button.getAttribute('data-bookmarked') !== 'true';
            var header = root.getAttribute('data-csrf-header');
            var token = root.getAttribute('data-csrf-token');
            if (!header || !token) { message('페이지를 새로고침한 후 다시 시도해 주세요.', false); return; }
            busy = true;
            button.disabled = true;
            button.setAttribute('aria-busy', 'true');
            var xhr = new XMLHttpRequest();
            function finish() { busy = false; button.disabled = false; button.removeAttribute('aria-busy'); }
            function failed() { finish(); message('북마크를 변경하지 못했습니다. 상태를 확인하려면 새로고침해 주세요.', false); }
            xhr.open('POST', button.getAttribute(add ? 'data-add-url' : 'data-remove-url'), true);
            xhr.timeout = 15000;
            xhr.setRequestHeader('Accept', 'application/json');
            xhr.setRequestHeader(header, token);
            xhr.onload = function () {
                finish();
                if (xhr.status === 401 || xhr.status === 403 || /\/customLogin(?:[?#]|$)/.test(xhr.responseURL || '')) {
                    message('로그인 상태가 만료되었거나 요청을 확인할 수 없습니다. 다시 로그인하거나 새로고침해 주세요.', true);
                    return;
                }
                if (xhr.status !== 200) { message(xhr.status === 404 ? '음식을 찾을 수 없습니다. 새로고침해 주세요.' : '북마크를 변경하지 못했습니다. 잠시 후 다시 시도해 주세요.', false); return; }
                try {
                    var data = JSON.parse(xhr.responseText);
                    if (String(data.menuId) !== id || typeof data.bookmarked !== 'boolean' || data.bookmarked !== add ||
                            typeof data.bookmarkCnt !== 'number' || !isFinite(data.bookmarkCnt) ||
                            data.bookmarkCnt < 0 || Math.floor(data.bookmarkCnt) !== data.bookmarkCnt) throw new Error('Unexpected bookmark response');
                    button.setAttribute('data-bookmarked', String(data.bookmarked));
                    button.setAttribute('aria-pressed', String(data.bookmarked));
                    button.setAttribute('aria-label', button.getAttribute('data-menu-name') + ' 북마크 ' + (data.bookmarked ? '해제' : '등록'));
                    var icon = button.querySelector('.fa');
                    icon.classList.toggle('fa-heart', data.bookmarked);
                    icon.classList.toggle('fa-heart-o', !data.bookmarked);
                    cell.querySelector('.bookmark-count').textContent = data.bookmarkCnt.toLocaleString('ko-KR');
                    message(data.bookmarked ? '북마크에 등록했습니다.' : '북마크를 해제했습니다.', false);
                } catch (ignored) { failed(); }
            };
            xhr.onerror = failed;
            xhr.ontimeout = failed;
            xhr.onabort = failed;
            try { xhr.send(); } catch (ignored) { failed(); }
        });
    });
}());
