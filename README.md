# Essential Scala

Written by [Dave Gurnell](http://twitter.com/davegurnell) and
[Noel Welsh](http://twitter.com/noelwelsh).
Copyright [Underscore Consulting LLP](http://underscore.io), 2015-2017.

<a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/"><img alt="Creative Commons Licence" style="border-width:0" src="https://i.creativecommons.org/l/by-sa/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/">Creative Commons Attribution-ShareAlike 4.0 International License</a>.


## Overview

[Essential Scala][essential-scala] is an introduction to Scala for professional developers.
Essential Scala covers the core Scala patterns that new developers need to be productive in the language.
It's designed to get you productive as quickly as possible, and avoid the dark and confusing corners of the language.


## Building

Essential Scala uses [Underscore's ebook build system][ebook-template].

The simplest way to build the book is to use [Docker Compose](http://docker.com):

- install Docker Compose (`brew install docker-compose` on OS X; or download from [docker.com](http://docker.com/)); and
- run `go.sh` (or `docker-compose run book bash` if `go.sh` doesn't work).

This will open a `bash` shell running inside the Docker container which contains all the dependencies to build the book. From the shell run:

- `npm install`; and then
- `sbt`.

Within `sbt` you can issue the commands `pdf`, `html`, `epub`, or `all` to build the desired version(s) of the book. Targets are placed in the `dist` directory:

[ebook-template]: https://github.com/underscoreio/underscore-ebook-template
[essential-scala]: http://underscore.io/books/essential-scala/
