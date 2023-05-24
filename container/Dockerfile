# Temporary image to download the package
FROM ubuntu:latest as downloader

ARG VERSION=16.0.0
RUN apt update \
    && apt install -y curl tar \
    && curl https://github.com/valentjn/ltex-ls/releases/download/${VERSION}/ltex-ls-${VERSION}.tar.gz \
        --location \
        --output ltex-ls-${VERSION}.tar.gz \
    && tar xvf ltex-ls-${VERSION}.tar.gz \
    && mv ltex-ls-${VERSION} /app \
    && rm /app/bin/*.bat \
    && find /app -maxdepth 1 -type f -not -name LICENSE.md -delete

# Actual image
FROM openjdk:slim

WORKDIR /app/bin
COPY --from=downloader /app /app
ENTRYPOINT [ "./ltex-cli" ]
