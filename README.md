# intelligence-feed-service

REST microservice that provides search capabilities on AlienVault OTX intelligence feed.

## Usage

### Build and run

Build uberjar and run locally:
```
$ make clean uber run
```

Or just run:
```
$ make clean uber && ./scripts/run --env dev
$ ./scripts/run --help
$ ./scripts/run --env test

### use a different config file:
$ config=resources/dat-other-config.edn ./scripts/run --env prod

### use a different port:
$ server_port=8778 ./scripts/run --env dev
```

Build and run in Docker:
```
$ make uber docker-build docker-run
```

#### Run from nREPL

Perform a `cider-jack-in-clj` (or equivalent for non-Emacs setups) and then
you will be in `dev/user.clj`, where you can start/stop/restart the system:
```
user> (go)
user> ;; ... doing amazing things ...
user> (stop)
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

## Endpoints

### GET /health

### GET /indicators/:doc_id

Returns a document by its ID.

### GET /indicators

Return all documents.

### GET /indicators?type=$doc_type

Find documents by their type.

### POST /search

Take search criteria as a JSON document in the body of the HTTP request.
Return all documents matching search criteria.

# Roadmap for future development

- incorporate `clojure.spec` (in web layer and repo layer)
- add support for Swagger / OpenAPI online documentation for routes
- ~~consider pivoting to `polylith`~~

# Author

Richard W. Norton (`rwtnorton@gmail.com`)
