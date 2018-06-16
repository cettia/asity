$(document).foundation();
$("h2,h3").each(function() {
  var $this = $(this);
  var $link = $("<a />").attr("href", "#" + $this.attr('id')).text(String.fromCharCode("182"));
  $this.append($link.hide()).hover(function() {
    $link.show();
  }, function () {
    $link.hide();
  });
});
$('span.n:contains("httpAction"),span.n:contains("wsAction"),span.s:contains("\\"/asity\\"")').addClass('highlighted');
$.get("http://cettia.io/feed.xml", function(doc) {
  var $announcement = $("#announcement");
  var $entry = $(doc).find("entry:contains(Asity):first");

  var title = $entry.find("title").text();
  $announcement.find(".link").attr({title: title, href: $entry.find("link").attr("href")}).text(title);

  var date = new Date(Date.parse($entry.find("published").text()));
  var options = {year: "numeric", month: "long", day: "numeric"};
  $announcement.find(".date").text(new Intl.DateTimeFormat("en-US", options).format(date));
  $announcement.find("p").css({visibility: "visible"});
});
