# intelligence-feed-service

REST microservice that provides search capabilities on AlienVault OTX intelligence feed.

## Usage

### Build and run

Build uberjar and run locally:
```
$ make uber run
```

Or just run:
```
$ ./scripts/run --help
$ ./scripts/run --env test
$ make clean uber && ./scripts/run --env dev

### use a different config file:
$ config=resources/dat-other-config.edn ./scripts/run --env prod

### use a different port:
$ server_port=8778 ./scripts/run --env dev
```

Build and run in Docker:
```
$ make uber docker-build docker-run
```

### Linting

```
$ make lint
```

### Formatting

```
$ make cljfmt-check
```

```
$ make cljfmt-fix
```

### Testing

```
$ make test
```

### Check for outdated dependencies

```
$ make outdated
```

### Scan dependencies for security vulnerabilities

Caveat:  This can be pretty slow on the first run.  It needs to
build a database of vulnerabilities.  Can take well over 10 minutes.

```
$ make vuln-scan
```

### CI preflight checks

```
$ make preflight-quick
```

```
$ make preflight-full
```

# Roadmap for future development

- incorporate `clojure.spec` (in web layer and repo layer)
- add suppor for Swagger / OpenAPI online documentation for routes
- consider pivoting to `polylith`

# Author

Richard W. Norton (`rwtnorton@gmail.com`)
