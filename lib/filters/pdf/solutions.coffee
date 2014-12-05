#!/usr/bin/env coffee

# Moves the solutions to exercises out of the main body and into a separate chapter.
# This filter modifies the native output from pandoc. See: http://johnmacfarlane.net/pandoc/scripting.html

_      = require 'underscore'
pandoc = require 'pandoc-filter'
crypto = require 'crypto'

#        write out the orignal DIV
#        pandoc.Div [ ident, [], kvs ], [ body...];
#        debugging
#        console.error("type #{type}\n value #{value}\n format #{format}\n meta #{meta}")

solutions = []

# Mutable state for the section we are currently in. Used to give the solution a title.
currentSectionTitle = "_no_section_seen_"

# Convert Pandoc AST text into a string.
# Eg [ { t: 'Str', c: 'Hi' } { t: 'Str', c: 'Mum' } ] -> "Hi Mum"
textOf = (body) -> _.chain(body).where({ t: 'Str'}).pluck('c').value().join(' ')

action = (type, value, format, meta) ->
  switch type
    when 'Header'
      [ level, [ident, classes, kvs], body ] = value
      currentSectionTitle = textOf(body)
      return
    when 'Div'
      [ [ident, classes, kvs], body ] = value
      if classes && classes[0] == "solution"
        hash = crypto.createHash('md5').update(JSON.stringify(body)).digest("hex")
        uniqueHash = "#{hash}_#{solutions.length}"
        solutions.push({key:uniqueHash,value:body,title:currentSectionTitle})
        if currentSectionTitle == "_no_section_seen_"
          console.error("Didn't find a section heading before solution encountered. Bug in solutions.coffee")
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
            pandoc.RawBlock("latex", "\\section{#{solution.title}} \\label{#{solution.key}}")
            solution.value...
          ]
        pandoc.Div([ "", [], [] ], nodes)


pandoc.stdio(action)
