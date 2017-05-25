$(document).ready(function() {
    $(".nav a").on("click", function () {
        $(".nav").find(".active").removeClass("active");
        $(this).parent().addClass("active");

    });
    $('#nav-icon').click(function(){
        $(this).toggleClass('collapsed');
    });

    $('.link-copy').tooltip({
        trigger: 'click',
        placement: 'bottom'
    });

    function setTooltip(btn, message) {
        btn.tooltip('hide')
            .attr('data-original-title', message)
            .tooltip('show');
    }

    function hideTooltip(btn) {
        setTimeout(function() {
            btn.tooltip('hide');
        }, 1000);
    }


    var clipboard = new Clipboard('.link-copy');

    clipboard.on('success', function(e) {
        var btn = $(e.trigger);
        setTooltip(btn, 'Nukopijuota');
        hideTooltip(btn);
    });
});