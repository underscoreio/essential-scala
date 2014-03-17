var relative = null;
if (location.protocol==='file:') {
  relative = Array($('link[rel="canonical"]').attr('href').match(/\//g).length-2).join('../');
  if (relative == '') relative = './';
}

function toRelative(link, index) {
  if (!relative) return link;
  var hash = link ? link.match(/#.*$/) : null;
  if (hash) link = link.replace(/#.*$/, '');
  return link?(link.replace(/^\//, relative)+(index?(link.substr(-1)=='/'?'index.html':''):'')+(hash?hash[0]:'')):null;
}

$().ready(function() {
  if (relative) {
    $('a').attr('href', function(a,b) {return toRelative(b, true);});
    $('img').attr('src', function(a,b) {return toRelative(b, false);});
  }

  // Transform
  //
  // <div class="solution">blah</div>
  //
  // To
  //
  // <div class="solution-block">
  //   <h5>...</h5>
  //   <div class="solution">blah</div>
  // </div>
  //
  // with click handler on h5 to toggle visibility of solution

  $('.solution')
    .hide()
    .wrap('<div class="solution-block"></div>')
    .before('<h5><a href="#">Solution (click to reveal)</a></h5>')
    .siblings('h5')
    .show()
    .click(function(evt) {
      $(this).siblings(".solution").toggle();
      evt.preventDefault();
    });
});
