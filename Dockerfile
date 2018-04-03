# This is a multi-stage build, see reference:
# https://docs.docker.com/develop/develop-images/multistage-build/
FROM openjdk:8 as builder

# Get SBT
RUN set -e \
  ; echo "deb http://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \
  ; apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
  ; apt-get update \
  ; apt-get install sbt \
  ;

WORKDIR /work

# Get dependencies first, rebuilds will reuse these layers
COPY project project
COPY build.sbt .
RUN set -e \
  ; sbt "docker:stage" \
  ;

# Stage artifacts for docker
COPY . .
RUN set -e \
  ; sbt "docker:stage" \
  ;


FROM openjdk:8-jre-alpine

WORKDIR /opt/docker
# Add artifacts from builder
COPY --from=builder /work/target/docker/stage/opt/docker .

# Add bash, add daemon user, setup /data, setup entrypoint
RUN set -e \
  ; apk add --no-cache bash \
  ; chown -R daemon:daemon . \
  ; mkdir -p /data \
  ; chown -R daemon:daemon /data \
  ; chmod +x bin/docker-entrypoint.sh \
  ;

VOLUME ["/data"]

ENTRYPOINT ["bin/docker-entrypoint.sh", "bin/renga-storage"]
CMD []

# In-container health check
HEALTHCHECK CMD ["bin/renga-storage", "-main", "controllers.HealthCheckController"]
