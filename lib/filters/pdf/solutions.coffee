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
        console.error("hash #{uniqueHash}")
        pandoc.RawBlock("latex", "\\hyperlink{#{uniqueHash}}{Solution}")
      else if classes && classes[0] == "solutions"
        console.error("soutions")
        big = ""
        for solution, i in solutions
          tmp = "\\hypertarget{#{solution.key}}
                 {Solution #{i}} #{solution.value}
                 \\pagebreak\n"
          big = big + tmp
          console.error("#{i} - #{solution.key}  - #{tmp}")
        console.error("after loop")
        pandoc.RawBlock("latex", big)


pandoc.stdio(action)
