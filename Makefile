version := $(shell cat VERSION | tr -d '\n')
uberfile := target/intelligence-feed-service-$(version)-standalone.jar
docker_tag := intelligence-feed-service-$(version)

uber:
	clj -T:build uber

run: $(uberfile)
	java -jar $(uberfile)

clean:
	clj -T:build clean

docker_build:
	docker build -t $(docker_tag) .

docker_run: docker_build
	docker run $(docker_tag)

.PHONY: version uberfile test lint cljfmt-fix cljfmt-check
version:
	@echo Found version $(version)
uberfile:
	@echo $(uberfile)

test:
	clj -M:test

lint:
	clj -M:clj-kondo --lint src

cljfmt-fix:
	clj -Mcljfmt-fix

cljfmt-check:
	clj -Mcljfmt-check
