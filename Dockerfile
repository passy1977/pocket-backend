# Stage that builds the application, a prerequisite for the running stage
FROM maven:3-amazoncorretto-21-debian-bookworm as build

USER root

RUN DEBIAN_FRONTEND=noninteractive apt update && apt-get upgrade -y
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y libssl-dev curl git build-essential manpages-dev autoconf automake cmake git libtool pkg-config vim && apt-get clean && rm -rf /var/lib/apt/lists/*


WORKDIR /var/www
RUN useradd -m pocket
RUN chown pocket:pocket /var/www
USER pocket


#RUN mkdir commons
RUN mkdir /var/www/scripts
COPY --chown=pocket commons ./commons
COPY --chown=pocket:pocket ./src src
COPY --chown=pocket pom.xml ./
COPY --chown=pocket ./docker_data/pocket5/pocket5-config.yaml /var/www/scripts/pocket5-config.yaml

RUN  mvn install:install-file \
    -Dfile=commons/commons-base-6.0.0.jar \
    -DgroupId=it.salsi \
    -DartifactId=commons-base \
    -Dversion=6.0.0 \
    -Dpackaging=jar \
    -DgeneratePom=true

RUN  mvn install:install-file \
    -Dfile=commons/commons-utils-6.0.0.jar \
    -DgroupId=it.salsi \
    -DartifactId=commons-utils \
    -Dversion=6.0.0 \
    -Dpackaging=jar \
    -DgeneratePom=true

RUN mvn package -DskipTests

WORKDIR /home/pocket
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
ENV PATH="/home/pocket/.cargo/bin:$PATH"
RUN git clone https://github.com/passy1977/pocket-cli.git
WORKDIR /home/pocket/pocket-cli
RUN cargo build --release
RUN rustup self uninstall -y
RUN cp /home/pocket/pocket-cli/target/release/pocket-device /var/www/pocket-device
RUN cp /home/pocket/pocket-cli/target/release/pocket-user /var/www/pocket-user
RUN rm -fr /home/pocket/pocket-cli
#RUN /usr/bin/java -Dspring.config.location=/var/www/scripts/pocket5-config.yaml -jar /var/www/pocket.jar
#CMD ["/usr/bin/java" "-Dspring.config.location=/var/www/scripts/pocket5-config.yaml" "-jar" "/var/www/pocket.jar"]

#Not delete
#CMD ["tail", "-f", "/dev/null"]

FROM maven:3-amazoncorretto-21-debian-bookworm
RUN useradd -m pocket
COPY --from=build --chown=pocket /var/www/target/pocket-*.jar /var/www/pocket.jar
COPY --from=build --chown=pocket /var/www/scripts /var/www/scripts
COPY --from=build --chown=pocket /var/www/pocket-device /var/www/pocket-device
COPY --from=build --chown=pocket /var/www/pocket-user /var/www/pocket-user
USER pocket
EXPOSE 8081
CMD /usr/bin/java -Dspring.config.location=/var/www/scripts/pocket5-config.yaml -jar /var/www/pocket.jar
#CMD ["tail", "-f", "/dev/null"]



