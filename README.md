# chord-server #

## Build & Run ##

```sh
$ cd chord-server
$ cp -R ../../js/svelte-chords/dist/ src/main/webapp
$ sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.
