# Temporary image to download the package
FROM maven:3.8 as builder

ADD . /build
WORKDIR /build

RUN apt-get update \
    && apt install -y python3 \
    && ln -s /usr/bin/python3 /usr/bin/python
RUN python3 tools/createCompletionLists.py && mvn verify \
    && VERSION=$(grep "<version>.*</version>" pom.xml | head -n1 | cut -d '>' -f 2 | cut -d '<' -f 1) \
    && cd target \
    && tar xvf ltex-ls-${VERSION}.tar.gz \
    && mv ltex-ls-${VERSION} /app \
    && rm /app/bin/*.bat \
    && find /app -maxdepth 1 -type f -not -name LICENSE.md -delete

# Actual image
FROM openjdk:slim

WORKDIR /app/bin
COPY --from=builder /app /app
ENTRYPOINT [ "./ltex-cli" ]
