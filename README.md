# intelligence-feed-service

REST microservice that provides search capabilities on AlienVault OTX intelligence feed.

## Usage

### Build and run

Build uberjar and run locally:
```
$ make uber run
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
$ make ci-quick
```

```
$ make ci-full
```
