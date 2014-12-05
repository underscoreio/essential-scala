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

# String manipulation ---------------------------

# arrayOf(node) -> string
textOf = (body) =>
  ans = ""
  for item in body
    switch item.t
      when "Str"   then ans += item.c
      when "Space" then ans += " "
  ans

# string integer -> string
numberedTitle = (title, number = 1) ->
  if number == 1 then title else "#{title} Part #{number}"

# string -> string
labelCounter = 0
label = (prefix, title) ->
  # prefix + title.replace(/[^a-zA-z0-9 ]+/g, "").replace(/[ ]+/g, "-").toLowerCase()
  labelCounter = labelCounter + 1
  prefix + crypto.createHash('md5').update(title + "-" + labelCounter).digest("hex")

# Accumulators ----------------------------------

class Heading
  constructor: (@label, @title) ->
    # Do nothing

class Solution
  constructor: (@exerciseLabel, @solutionLabel, @exerciseTitle, @solutionTitle, @body) ->
    # Do nothing

# arrayOf(or(Heading, Solution))
#
# A list of chapter (level 1) headings and solutions:
solutionAccum = []

# or(Heading, null)
#
# The last heading (any level) we passed.
# We record this because exercise titles are rendered using headings:
chapterAccum = null
headingAccum = null

# integer
#
# The number of solutions we've passed since the last heading.
# We record this because some exercises have multiple solutions:
chapterCounter = 0 # index of solution since last chapter heading
headingCounter = 0 # index of solution since last heading

# Tree walkin' ----------------------------------

action = (type, value, format, meta) ->
  switch type
    when 'Header'
      [ level, [ident, classes, kvs], body ] = value

      # console.error("HEADING #{level} #{JSON.stringify([ident, classes, kvs])}")

      # Record the last title we passed so we can name and number exercises.
      # Some exercises have multiple solutions, so reset that counter too.
      headingAccum = new Heading(ident, textOf(body))
      headingCounter = 0

      # We keep a record of the last chapter heading.
      # As soon as we see a solution in this chapter,
      # we add the chapter heading as a subheading in the solutions chapter:
      if level == 1
        chapterAccum = headingAccum
        chapterCounter = 0

      return # don't rewrite the document here
    when 'Div'
      [ [ident, classes, kvs], body ] = value
      if classes?[0] == "solution"
        chapterCounter = chapterCounter + 1
        headingCounter = headingCounter + 1

        # If this is the first solution this chapter,
        # push the chapter heading on the list of items to
        # render in the solutions chapter:
        if chapterCounter == 1 then solutionAccum.push(chapterAccum)

        # Titles of the exercise and the solution:
        exerciseTitle = headingAccum.title
        solutionTitle = "Solution to: " + numberedTitle(headingAccum.title, headingCounter)

        # Anchor labels for the exercise and the solution:
        exerciseLabel = headingAccum.label
        solutionLabel = label("solution:", solutionTitle)

        solutionAccum.push(new Solution(exerciseLabel, solutionLabel, exerciseTitle, solutionTitle, body))

        pandoc.RawBlock("latex", "\\hyperref[#{solutionLabel}]{See the solution}")
      else if classes?[0] == "solutions"
        nodes = []
        for item in solutionAccum
          if item instanceof Heading
            { title } = item

            # console.error("CHAPTER #{title}")

            nodes = nodes.concat [
              pandoc.RawBlock("latex", "\\section{#{title}}")
            ]
          else if item instanceof Solution
            {
              exerciseTitle
              solutionTitle
              exerciseLabel
              solutionLabel
              body
            } = item

            # console.error("SOLUTION #{exerciseTitle} #{solutionTitle} #{exerciseLabel} #{solutionLabel}")

            nodes = nodes.concat [
              pandoc.RawBlock("latex", "\\subsection{#{solutionTitle}} \\label{#{solutionLabel}}")
              body...
              pandoc.RawBlock("latex", "\\hyperref[#{exerciseLabel}]{Return to the exercise}")
            ]
        pandoc.Div([ "", [], [] ], nodes)


pandoc.stdio(action)