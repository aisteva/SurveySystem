/**
 * Created by Lenovo on 2017-05-28.
 */
$(document).ready(function() {
    function setTooltip(btn, message) {
        btn.tooltip('hide')
            .attr('data-original-title', message)
            .tooltip('show');
    }

    function hideTooltip(btn) {
        setTimeout(function () {
            btn.tooltip('hide');
        }, 1000);
    }


    var clipboard = new Clipboard('.link-copy');

    clipboard.on('success', function (e) {
        var btn = $(e.trigger);
        setTooltip(btn, 'Nukopijuota');
        hideTooltip(btn);
    });
});