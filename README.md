Essential Scala
---------------

Getting Started
---------------

You'll need to install the grunt project dependencies the first time you check the project out:

~~~
brew install pandoc
npm install -g grunt-cli
npm install
mkdir dist
~~~

Building
--------

Use the following commands to build a single format:

~~~
grunt pdf
grunt html
grunt epub
~~~

All targets are placed in the `dist` directory.

Run the following to build all formats, start a web server to serve them all,
and rebuild if you change any files:

~~~
grunt watch
~~~

Use the following to build all a ZIP of all formats:

~~~
grunt zip
~~~

The default grunt behaviour is to run `zip`:

~~~
grunt
~~~
