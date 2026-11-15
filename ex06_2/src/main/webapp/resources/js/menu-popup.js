(function () {
    'use strict';
    document.getElementById('close-popup').addEventListener('click', function () {
        if (window.opener) window.close();
        else window.location.href = document.querySelector('.popup-footer a').href;
    });
    Array.prototype.forEach.call(document.querySelectorAll('.star-picker'), function (picker) {
        function paint() {
            var selected = picker.querySelector('input:checked');
            var score = selected ? Number(selected.value) : 0;
            Array.prototype.forEach.call(picker.querySelectorAll('input'), function (input) {
                input.nextElementSibling.classList.toggle('selected', Number(input.value) <= score);
            });
        }
        picker.addEventListener('change', paint);
        paint();
    });
    Array.prototype.forEach.call(document.querySelectorAll('.remove-review'), function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm('이 리뷰를 삭제할까요?')) event.preventDefault();
        });
    });
    Array.prototype.forEach.call(document.querySelectorAll('.review-form'), function (form) {
        form.addEventListener('submit', function (event) {
            var text = form.querySelector('textarea');
            if (!text.value.trim()) {
                event.preventDefault();
                text.setCustomValidity('리뷰 내용을 입력해 주세요.');
                text.reportValidity();
            }
        });
        form.querySelector('textarea').addEventListener('input', function () { this.setCustomValidity(''); });
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
