(function () {
    'use strict';
    Array.prototype.forEach.call(document.querySelectorAll('.star-picker'), function (picker) {
        var inputs = Array.prototype.slice.call(picker.querySelectorAll('input[type="radio"]'));
        var hoverScore = null;
        var selectedScore = 0;

        function readSelection() {
            selectedScore = 0;
            inputs.forEach(function (input) { if (input.checked) selectedScore = Number(input.value); });
        }
        function paint() {
            var displayScore = hoverScore === null ? selectedScore : hoverScore;
            inputs.forEach(function (input) {
                var filled = Number(input.value) <= displayScore;
                var label = input.nextElementSibling;
                label.classList.toggle('selected', filled);
                label.querySelector('[data-star-icon]').textContent = filled ? '★' : '☆';
            });
        }
        inputs.forEach(function (input) {
            input.nextElementSibling.addEventListener('pointerenter', function (event) {
                if (event.pointerType === 'touch') return;
                hoverScore = Number(input.value);
                paint(); // Deliberately never writes input.checked.
            });
        });
        function restore() { hoverScore = null; paint(); }
        picker.addEventListener('pointerleave', restore);
        picker.addEventListener('pointercancel', restore);
        picker.addEventListener('keydown', restore);
        picker.addEventListener('change', function () {
            readSelection(); // Native label clicks, touch and radio arrow keys commit the score.
            restore();
        });
        if (inputs.length && inputs[0].form) inputs[0].form.addEventListener('reset', function () {
            window.setTimeout(function () { readSelection(); restore(); }, 0);
        });
        readSelection();
        paint();
    });

    Array.prototype.forEach.call(document.querySelectorAll('.review-form'), function (form) {
        var text = form.querySelector('textarea[name="reviewText"]');
        if (!text) return;
        text.addEventListener('input', function () { text.setCustomValidity(''); });
        form.addEventListener('submit', function (event) {
            var missing = form.querySelector('.star-picker') && !form.querySelector('input[name="starScore"]:checked');
            if (missing || !text.value.trim() || Array.from(text.value).length > 1000) {
                event.preventDefault();
                if (missing) { form.querySelector('input[name="starScore"]').focus(); form.reportValidity(); }
                else {
                    text.setCustomValidity(!text.value.trim() ? '리뷰 내용을 입력해 주세요.' : '리뷰는 1,000자 이내로 입력해 주세요.');
                    text.reportValidity();
                }
            }
        });
    });
}());
