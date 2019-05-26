(function() {
  $("h2[id],h3[id]").each(function() {
    var $this = $(this);
    var $link = $("<a />").attr({href: "#" + $this.attr('id'), "data-smooth-scroll": true, "data-offset": 55.375}).text(String.fromCharCode("182")).hide();
    $this.append($link).hover(function() {
      $link.show();
    }, function () {
      $link.hide();
    });
  });

  $('span.n:contains("httpAction"),span.n:contains("wsAction"),span.s:contains("\\"/echo\\"")').addClass('highlighted');
/*
  $.get("https://cettia.io/feed.xml", function(doc) {
    var $announcement = $("#announcement");
    var $entry = $(doc).find("entry:contains(Asity):first");

    var title = $entry.find("title").text();
    $announcement.find(".link").attr({title: title, href: $entry.find("link").attr("href")}).text(title);

    var date = new Date(Date.parse($entry.find("published").text()));
    var options = {year: "numeric", month: "long", day: "numeric"};
    $announcement.find(".date").text(new Intl.DateTimeFormat("en-US", options).format(date));
    $announcement.find("p").css({visibility: "visible"});
  });
*/

  Foundation.SmoothScroll._scrollToLoc = Foundation.SmoothScroll.scrollToLoc;
  Foundation.SmoothScroll.scrollToLoc = function(loc, options, callback) {
    if (loc === "#") {
      loc = "html";
    }

    return Foundation.SmoothScroll._scrollToLoc.call(this, loc, options, function() {
      if (loc === "html") {
        loc = "#";
      }
      history.pushState({}, "", loc);
      if (callback && typeof callback == "function"){
        callback();
      }
    });
  };
/*
  var onscroll = function () {
    if (window.scrollY) {
      document.getElementById("top-bar-wrapper").classList.add("visible");
    } else {
      document.getElementById("top-bar-wrapper").classList.remove("visible");
    }
  };
  onscroll();
  window.onscroll = onscroll;*/
  $(document).foundation();
})();
