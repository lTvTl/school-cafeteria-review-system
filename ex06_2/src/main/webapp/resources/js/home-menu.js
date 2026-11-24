(function ($) {
    'use strict';

    document.querySelectorAll('.cafeteria-main .meal-card').forEach(function (card, index) {
        var storageKey = 'cafeteria-meal-open:' + window.location.pathname + ':' + index;
        try {
            var saved = window.sessionStorage.getItem(storageKey);
            if (saved !== null) card.open = saved === 'true';
        } catch (ignored) { /* Storage can be unavailable in private browsing. */ }
        var summary = card.querySelector('summary');
        var body = $(card.querySelector('.card-body'));
        var expanded = card.open;

        summary.addEventListener('click', function (event) {
            event.preventDefault();
            expanded = !expanded;
            try { window.sessionStorage.setItem(storageKey, String(expanded)); } catch (ignored) { /* Optional persistence. */ }
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
