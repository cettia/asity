$(document).foundation();
$("h2,h3").each(function () {
  var $this = $(this);
  var $link = $("<a />").attr("href", "#" + $this.attr('id')).text(String.fromCharCode("182"));
  $this.append($link.hide()).hover(function () {
    $link.show();
  }, function () {
    $link.hide();
  });
});
$('span.n:contains("httpAction"),span.n:contains("wsAction"),span.s:contains("\\"/asity\\"")').addClass('highlighted');
