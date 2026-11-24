(function () {
    'use strict';
    Array.prototype.forEach.call(document.querySelectorAll('.daily-remove-review'), function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm('오늘의 학식 리뷰를 삭제할까요?')) event.preventDefault();
        });
    });
}());
