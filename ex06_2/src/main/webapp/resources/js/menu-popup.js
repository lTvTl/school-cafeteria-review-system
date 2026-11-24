(function () {
    'use strict';
    document.getElementById('close-popup').addEventListener('click', function () {
        if (window.opener) window.close();
        else window.location.href = document.querySelector('.popup-footer a').href;
    });
    Array.prototype.forEach.call(document.querySelectorAll('.remove-review'), function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm('이 리뷰를 삭제할까요?')) event.preventDefault();
        });
    });
    // Only the server's success flash (after commit + PRG) sets this attribute.
    if (document.body.getAttribute('data-review-saved') === 'true') {
        try {
            if (window.opener && !window.opener.closed) {
                window.opener.postMessage({ type: 'menu-review-saved', menuId: document.body.getAttribute('data-menu-id') }, window.location.origin);
            }
        } catch (ignored) { /* A directly opened detail page has no parent to refresh. */ }
    }
}());
