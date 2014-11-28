$   = require 'jquery'
toc = require './toc'

$ ->
  toc.init('.toc-toggle', '.toc-contents')
  return
