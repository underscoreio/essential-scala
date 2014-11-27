#global module:false

path    = require 'path'
process = require 'child_process'

"use strict"

module.exports = (grunt) ->
  minify = grunt.option('minify') ? false

  grunt.loadNpmTasks "grunt-browserify"
  grunt.loadNpmTasks "grunt-contrib-connect"
  grunt.loadNpmTasks "grunt-contrib-less"
  grunt.loadNpmTasks "grunt-contrib-watch"
  grunt.loadNpmTasks "grunt-exec"
  grunt.loadNpmTasks "grunt-css-url-embed"

  joinLines = (lines) ->
    lines.split(/[ \r\n]+/).join(" ")

  pandocSources = joinLines """
    src/pages/index.md
    src/pages/getting-started/index.md
    src/pages/intro/index.md
    src/pages/intro/objects.md
    src/pages/intro/literals.md
    src/pages/intro/object-literals.md
    src/pages/intro/writing-methods.md
    src/pages/intro/expressions.md
    src/pages/intro/conclusion.md
    src/pages/classes/index.md
    src/pages/classes/classes.md
    src/pages/classes/apply.md
    src/pages/classes/companion-objects.md
    src/pages/classes/case-classes.md
    src/pages/classes/pattern-matching.md
    src/pages/classes/conclusions.md
    src/pages/traits/index.md
    src/pages/traits/sealed-traits.md
    src/pages/traits/modelling-data-with-traits.md
    src/pages/traits/working-with-data.md
    src/pages/traits/recursive-data.md
    src/pages/traits/conclusions.md
    src/pages/sequencing/index.md
    src/pages/sequencing/generics.md
    src/pages/sequencing/functions.md
    src/pages/sequencing/working-with-data.md
    src/pages/sequencing/modelling-data.md
    src/pages/sequencing/sequencing-computation.md
    src/pages/sequencing/variance.md
    src/pages/sequencing/conclusions.md
    src/pages/collections/index.md
    src/pages/collections/seq.md
    src/pages/collections/working-with-seq.md
    src/pages/collections/for-comprehensions.md
    src/pages/collections/options.md
    src/pages/collections/meeting-monads.md
    src/pages/collections/for-comprehensions-redux.md
    src/pages/collections/map-and-set.md
    src/pages/collections/ranges.md
    src/pages/implicits/index.md
    src/pages/implicits/foundations.md
    src/pages/implicits/implicit-parameters.md
    src/pages/implicits/type-classes.md
    src/pages/implicits/enrichment.md
    src/pages/implicits/using-type-classes.md
    src/pages/implicits/conversions.md
    src/pages/pattern-matching/index.md
    src/pages/pattern-matching/extractors.md
    src/pages/dsl/index.md
    src/pages/dsl/operators.md
    src/pages/dsl/control.md
    src/pages/dsl/macros.md
    src/pages/collections-redux/index.md
    src/pages/collections-redux/seq-implementations.md
    src/pages/collections-redux/arrays-and-strings.md
    src/pages/collections-redux/iterators.md
    src/pages/collections-redux/traversable.md
    src/pages/collections-redux/java-interop.md
    src/pages/collections-redux/mutable-seq.md
  """

  grunt.initConfig
    less:
      main:
        options:
          paths: [
            "node_modules"
            "src/css"
          ]
          compress: minify
          yuicompress: minify
        files:
          "dist/temp/main.noembed.css" : "src/css/main.less"

    cssUrlEmbed:
      main:
        options:
          baseDir: "."
        files:
          "dist/temp/main.css" : "dist/temp/main.noembed.css"

    browserify:
      main:
        src:  "src/js/main.coffee"
        dest: "dist/temp/main.js"
        cwd:  "."
        options:
          watch: false
          transform: if minify
            [ 'coffeeify', [ 'uglifyify', { global: true } ] ]
          else
            [ 'coffeeify' ]
          browserifyOptions:
            debug: false
            extensions: [ '.coffee' ]

    watchImpl:
      options:
        livereload: true
      css:
        files: [
          "src/css/**/*"
        ]
        tasks: [
          "less"
          "cssUrlEmbed"
          "pandoc:html"
        ]
      js:
        files: [
          "src/js/**/*"
        ]
        tasks: [
          "browserify"
          "pandoc:html"
        ]
      templates:
        files: [
          "src/templates/**/*"
        ]
        tasks: [
          "pandoc:html"
          "pandoc:pdf"
          "pandoc:epub"
        ]
      pages:
        files: [
          "src/pages/**/*"
        ]
        tasks: [
          "pandoc:html"
          "pandoc:pdf"
          "pandoc:epub"
        ]
      metadata:
        files: [
          "src/meta/**/*"
        ]
        tasks: [
          "pandoc:html"
          "pandoc:pdf"
          "pandoc:epub"
        ]

    exec:
      exercises:
        cmd: joinLines """
               rm -rf essential-scala-code &&
               echo 'TODO: checkout sample code from git' &&
               mkdir essential-scala-code
               zip -r essential-scala-code.zip essential-scala-code
             """
        cwd: "dist"
      zip:
        cmd: "zip essential-scala.zip essential-scala.pdf essential-scala.html essential-scala.epub essential-scala-code.zip"
        cwd: "dist"


    connect:
      server:
        options:
          port: 4000
          base: 'dist'

  grunt.renameTask "watch", "watchImpl"

  grunt.registerTask "pandoc", "Run pandoc", (target) ->
    done = this.async()

    target ?= "html"

    switch target
      when "pdf"
        output   = "--output=dist/essential-scala.pdf"
        template = "--template=src/templates/template.tex"
        filters  = joinLines """
                     --filter=src/filters/pdf/callout.coffee
                     --filter=src/filters/pdf/columns.coffee
                   """
        metadata = "src/meta/pdf.yaml"

      when "html"
        output   = "--output=dist/essential-scala.html"
        template = "--template=src/templates/template.html"
        filters  = joinLines """
                     --filter=src/filters/html/tables.coffee
                   """
        metadata = "src/meta/html.yaml"

      when "epub"
        output   = "--output=dist/essential-scala.epub"
        template = "--epub-stylesheet=dist/temp/main.css"
        filters  = ""
        metadata = "src/meta/epub.yaml"

      when "json"
        output   = "--output=dist/essential-scala.json"
        template = ""
        filters  = ""
        metadata = ""

      else
        grunt.log.error("Bad pandoc format: #{target}")

    command = joinLines """
      pandoc
      --smart
      #{output}
      #{template}
      --from=markdown+grid_tables+multiline_tables+fenced_code_blocks+fenced_code_attributes+yaml_metadata_block+implicit_figures
      --latex-engine=xelatex
      #{filters}
      --chapters
      --number-sections
      --table-of-contents
      --highlight-style tango
      --standalone
      --self-contained
      src/meta/metadata.yaml
      --epub-cover-image=src/images/epub_cover.png
      #{metadata}
      #{pandocSources}
    """
    pandoc = process.exec(command)

    pandoc.stdout.on 'data', (d) ->
      grunt.log.write(d)
      return

    pandoc.stderr.on 'data', (d) ->
      grunt.log.error(d)
      return

    pandoc.on 'error', (err) ->
      grunt.log.error("Failed with: #{err}")
      done(false)

    pandoc.on 'exit', (code) ->
      if code == 0
        grunt.verbose.subhead("pandoc exited with code 0")
        done()
      else
        grunt.log.error("pandoc exited with code #{code}")
        done(false)

    return

  grunt.registerTask "json", [
    "pandoc:json"
  ]

  grunt.registerTask "html", [
    "less"
    "cssUrlEmbed"
    "browserify"
    "pandoc:html"
  ]

  grunt.registerTask "pdf", [
    "pandoc:pdf"
  ]

  grunt.registerTask "epub", [
    "less"
    "cssUrlEmbed"
    "pandoc:epub"
  ]

  grunt.registerTask "all", [
    "less"
    "cssUrlEmbed"
    "browserify"
    "pandoc:html"
    "pandoc:pdf"
    "pandoc:epub"
  ]

  grunt.registerTask "zip", [
    "all"
    "exec:exercises"
    "exec:zip"
  ]

  grunt.registerTask "serve", [
    "build"
    "connect:server"
    "watchImpl"
  ]

  grunt.registerTask "watch", [
    "all"
    "connect:server"
    "watchImpl"
    "serve"
  ]

  grunt.registerTask "default", [
    "zip"
  ]
