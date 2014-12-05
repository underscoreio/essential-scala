$   = require 'jquery'
toc = require './toc'

addToggle = (className, name, additionalClasses) ->

  console.log("addToggle")

  toggleMaina = () ->
    console.log("toggleMaina")
    toggleable = $(this)

    extraClasses =  additionalClasses ? additionalClasses : ''

    theToggling = (evt) ->
      console.log("theToggling")
      toggleable.toggle()
      evt.preventDefault()

    toggleable
      .addClass("panel-body")
      .wrap('<div class="panel panel-default #{extraClasses}"></div>')
      .hide()

    $("<a href=\"javascript:void 0\"><div class=\"panel-heading\"><h5> #{name} (click to reveal)</h5></div></a>")
      .insertBefore(toggleable)
      .click(theToggling)

  $(".#{className}").each(toggleMaina)


$ ->
  toc.init('.toc-toggle', '.toc-contents')
  addToggle('solution', 'Solution')
  return

