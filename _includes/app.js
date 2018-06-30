(function() {
  $("h2,h3").each(function() {
    var $this = $(this);
    var $link = $("<a />").attr("href", "#" + $this.attr('id')).text(String.fromCharCode("182")).hide();
    $this.append($link).hover(function() {
      $link.show();
    }, function () {
      $link.hide();
    });
  });

  $('span.n:contains("httpAction"),span.n:contains("wsAction"),span.s:contains("\\"/path\\"")').addClass('highlighted');

  var $tabs = $("#example-tabs");
  var $tabsContent = $("div.tabs-content[data-tabs-content=" + $tabs.attr("id") + "]");
  var $examples = $("div.example");
  $tabs.find("li > a").each(function(i) {
    var $this = $(this);
    var $text = $this.text();
    var $id = "example-" + $text.toLowerCase().replace(/\W/g, "-");
    $this.attr("href", "#" + $id);
    $("<div>").attr("id", $id).addClass("tabs-panel")
      .append($("<p>").html("A working example is available at TODO example link."))
      .append($examples.eq(i).clone())
      .append($("<p>").html("For more information, see <a href='#" + $id.substring(8) + "'>" + $text + "</a>."))
      .appendTo($tabsContent);
  });

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

  $(document).foundation();
})();
