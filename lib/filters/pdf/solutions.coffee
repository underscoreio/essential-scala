#!/usr/bin/env coffee

_      = require 'underscore'
pandoc = require 'pandoc-filter'
crypto = require 'crypto'

#        write out the orignal DIV
#        pandoc.Div [ ident, [], kvs ], [ body...];
#        debugging
#        console.error("type #{type}\n value #{value}\n format #{format}\n meta #{meta}")

solutions = []

action = (type, value, format, meta) ->
  switch type
    when 'Div'
      [ [ident, classes, kvs], body ] = value
      if classes && classes[0] == "solution"
        hash = crypto.createHash('md5').update(JSON.stringify(body)).digest("hex")
        uniqueHash = "#{hash}_#{solutions.length}"
        solutions.push({key:uniqueHash,value:body})
        #
        # This needs some text "Click to see solution"
        # Also needs to be an anchor so we and build a return point from the
        # solution
        #
        pandoc.RawBlock("latex", "\\ref{#{uniqueHash}}")
      else if classes && classes[0] == "solutions"
        nodes = []
        for solution, i in solutions
          #
          # This needs "return to exercises"
          #
          nodes = nodes.concat [
            pandoc.RawBlock("latex", "\\subsection{Solution #{i}} \\label{#{solution.key}}")
            solution.value...
          ]
        pandoc.Div([ "", [], [] ], nodes)


pandoc.stdio(action)
