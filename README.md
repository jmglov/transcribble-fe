# Transcribble Frontend

A browser-based transcription tool written in ClojureScript. Heavily inspired by
[oTranscribe](https://otranscribe.com/).

Use it by aiming your browser at https://jmglov.net/transcribble-fe

## Development

Transcribble Frontent is written in ClojureScript using [Scittle](https://github.com/babashka/scittle/).

First, [install Babashka](https://book.babashka.org/#getting_started) if you
haven't already.

Then, from the project root, run:

``` text
bb dev
```

This will start a webserver on http://localhost:1341, and an nREPL server on
port 1339. If you want to connect to the REPL for some good old fashioned
interactive development, start a ClojureScript REPL client in your editor of
choice and connect to port 1339, using `nbb` as your REPL type, if you have that
option (which you should if you're using
[CIDER](https://docs.cider.mx/cider/index.html) in Emacs).
