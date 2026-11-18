(function ($) {
    'use strict';

    document.querySelectorAll('.cafeteria-main .meal-card').forEach(function (card) {
        var summary = card.querySelector('summary');
        var body = $(card.querySelector('.card-body'));
        var expanded = card.open;

        summary.addEventListener('click', function (event) {
            event.preventDefault();
            expanded = !expanded;
            body.stop(true, false);
            if (!card.open) {
                body.hide();
                card.open = true;
            }
            var duration = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 250;
            body[expanded ? 'slideDown' : 'slideUp'](duration, function () {
                card.open = expanded;
                body.css('display', '');
            });
        });
    });
})(jQuery);
