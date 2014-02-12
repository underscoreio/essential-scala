$().ready(function() {
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
