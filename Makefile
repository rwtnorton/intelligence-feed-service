version := $(shell cat VERSION | tr -d '\n')
uberfile := target/intelligence-feed-service-$(version)-standalone.jar
docker_tag := intelligence-feed-service-$(version)

uber:
	clojure -T:build uber

# Note:  scripts/run is more flexible!
run: $(uberfile)
	java -jar $(uberfile)

clean:
	clojure -T:build clean

docker-build:
	docker build -t $(docker_tag) .

docker-run: docker-build
	docker run $(docker_tag)

.PHONY: version uberfile \
        test lint \
        cljfmt-fix cljfmt-check \
        outdated vuln-scan
version:
	@echo Found version $(version)
uberfile:
	@echo $(uberfile)

test:
	clojure -M:test

lint:
	clojure -M:clj-kondo --lint src --lint test

cljfmt-fix:
	clojure -Mcljfmt-fix

cljfmt-check:
	clojure -Mcljfmt-check

outdated:
	clojure -M:outdated

# This can be slow on the first run (> 10 minutes).
vuln-scan:
	clojure -M:clj-watson -p deps.edn

preflight-quick: lint cljfmt-check test

preflight-full: lint cljfmt-check test outdated vuln-scan

