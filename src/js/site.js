$(function() {
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

  if (relative) {
    $('a').attr('href', function(a,b) {return toRelative(b, true);});
    $('img').attr('src', function(a,b) {return toRelative(b, false);});
  }

  // Transform
  //
  // <div class="className">blah</div>
  //
  // To
  //
  // <div class="panel panel-default">
  //   <h5>...</h5>
  //   <div class="className">blah</div>
  // </div>
  //
  // with click handler on h5 to toggle visibility of solution
  function addToggle(className, name) {
    $('.'+className).each(function() {
      var toggleable = $(this);

      toggleable
        .addClass("panel-body")
        .wrap('<div class="panel panel-default"></div>')
        .hide();

      $('<a href="javascript:void 0"><div class="panel-heading"><h5>'+name+' (click to reveal)</h5></div></a>')
        .insertBefore(toggleable)
        .click(function(evt) {
          toggleable.toggle();
          evt.preventDefault();
        });
    });
  }

  addToggle('solution', 'Solution')

  addToggle('java-tip', 'Java Tip');
});
