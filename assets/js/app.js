$(document).foundation();
$("#content :header").each(function() {
    var $this = $(this);
    var $link = $("<a />").attr("href", "#" + $this.attr('id')).text(String.fromCharCode("182"));
    $this.append($link.hide()).hover(function() {
        $link.show();
    }, function() {
        $link.hide();
    });
});
