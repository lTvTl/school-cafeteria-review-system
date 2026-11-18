(function ($) {
    'use strict';
    var panel = document.getElementById('admin-monitor');
    if (!panel) return;
    var timer;
    var pending = false;
    var button = $('#admin-refresh');
    var samples = [];

    function drawChart(name, key, maximum, now) {
        var line = '';
        var area = '';
        var segment = '';
        var firstX;
        var lastX;
        var previousTime;
        function closeSegment() {
            if (segment) area += segment + ' L' + lastX + ' 160 L' + firstX + ' 160 Z ';
            segment = '';
        }
        samples.forEach(function (sample) {
            if (sample[key] === null) { closeSegment(); previousTime = null; return; }
            if (previousTime && sample.time - previousTime > 6000) closeSegment();
            var x = (600 * (1 - (now - sample.time) / 60000)).toFixed(2);
            var y = (160 * (1 - sample[key] / maximum)).toFixed(2);
            var point = (segment ? ' L' : ' M') + x + ' ' + y;
            if (!segment) firstX = x;
            line += point;
            segment += point;
            lastX = x;
            previousTime = sample.time;
        });
        closeSegment();
        $('#admin-' + name + '-line').attr('d', line);
        $('#admin-' + name + '-area').attr('d', area);
    }

    function record(memoryUsage, activeUserCnt) {
        var now = Date.now();
        samples.push({time: now, memoryUsage: memoryUsage, activeUserCnt: activeUserCnt});
        samples = samples.filter(function (sample) { return sample.time >= now - 60000; }).slice(-180);
        var maximum = 5;
        samples.forEach(function (sample) {
            if (sample.activeUserCnt !== null) maximum = Math.max(maximum, Math.ceil(sample.activeUserCnt / 5) * 5);
        });
        $('#admin-users-max').text(maximum + ' 세션');
        drawChart('memory', 'memoryUsage', 100, now);
        drawChart('users', 'activeUserCnt', maximum, now);
    }

    function unavailable() {
        record(null, null);
        $('#admin-server-status').text('확인 불가').attr('class', 'admin-status is-unknown');
        $('#admin-active-users').text('—');
        $('#admin-memory-usage').text('—');
        $('#admin-monitor-note').text('상태를 가져오지 못했습니다. 연결 상태와 관리자 로그인을 확인해 주세요.');
    }

    function refresh() {
        if (pending) return;
        clearTimeout(timer);
        pending = true;
        button.prop('disabled', true);
        $.ajax({url: panel.getAttribute('data-status-url'), dataType: 'json', timeout: 5000, cache: false})
            .done(function (data) {
                if (!data || ['NORMAL', 'BUSY'].indexOf(data.serverStatus) === -1 ||
                    !Number.isInteger(data.activeUserCnt) || data.activeUserCnt < 0 ||
                    !Number.isFinite(data.memoryUsage) || data.memoryUsage < 0 || data.memoryUsage > 100) {
                    unavailable();
                    return;
                }
                var normal = data.serverStatus === 'NORMAL';
                $('#admin-server-status').text(normal ? '정상' : '혼잡').attr('class', 'admin-status ' + (normal ? 'is-normal' : 'is-busy'));
                $('#admin-active-users').text(data.activeUserCnt);
                $('#admin-memory-usage').text(data.memoryUsage.toFixed(1) + '%');
                record(data.memoryUsage, data.activeUserCnt);
                $('#admin-monitor-note').text('최근 확인 ' + new Date().toLocaleTimeString('ko-KR') + ' · 2초마다 갱신 · 최근 60초 · 현재 서버 기준');
            })
            .fail(unavailable)
            .always(function () {
                pending = false;
                button.prop('disabled', false);
                timer = setTimeout(refresh, 2000);
            });
    }
    button.on('click', refresh);
    var initialMemory = Number(panel.getAttribute('data-memory-usage'));
    var initialUsers = Number(panel.getAttribute('data-active-users'));
    if (Number.isFinite(initialMemory) && initialMemory >= 0 && initialMemory <= 100 &&
        Number.isInteger(initialUsers) && initialUsers >= 0) record(initialMemory, initialUsers);
    timer = setTimeout(refresh, 2000);
})(jQuery);
