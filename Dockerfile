# Stage that builds the application, a prerequisite for the running stage
FROM maven:3-amazoncorretto-23-debian as build

MAINTAINER salsi.it

USER root

RUN DEBIAN_FRONTEND=noninteractive apt update && apt-get upgrade -y && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /var/www
RUN useradd -m pocket
RUN chown pocket:pocket /var/www
USER pocket


#RUN mkdir commons
RUN mkdir /var/www/scripts
COPY --chown=pocket commons ./commons
COPY --chown=pocket:pocket src src
COPY --chown=pocket pom.xml ./
COPY --chown=pocket scripts/pocket4-config.yaml /var/www/scripts/pocket4-config.yaml




RUN  mvn install:install-file \
    -Dfile=commons/commons-base-7.0.0.jar \
    -DgroupId=it.salsi \
    -DartifactId=commons-base \
    -Dversion=7.0.0 \
    -Dpackaging=jar \
    -DgeneratePom=true

RUN  mvn install:install-file \
    -Dfile=commons/commons-utils-7.0.0.jar \
    -DgroupId=it.salsi \
    -DartifactId=commons-utils \
    -Dversion=7.0.0 \
    -Dpackaging=jar \
    -DgeneratePom=true


#RUN mv /var/www/target/pocket-*.jar /var/www/pocket.jar
RUN mvn package -DskipTests

#RUN /usr/bin/java -Dspring.config.location=/var/www/scripts/pocket4-config.yaml -jar /var/www/pocket.jar
#CMD ["/usr/bin/java" "-Dspring.config.location=/var/www/scripts/pocket4-config.yaml" "-jar" "/var/www/pocket.jar"]

#Not delete
#CMD ["tail", "-f", "/dev/null"]

FROM maven:3-amazoncorretto-23-debian 
COPY --from=build /var/www/target/pocket-*.jar /var/www/pocket.jar
COPY --from=build /var/www/scripts /var/www/scripts
RUN useradd -m pocket
USER pocket
EXPOSE 8081
RUN ls -la /var/www/
RUN ls -la /var/www/scripts
CMD /usr/bin/java -Dspring.config.location=/var/www/scripts/pocket4-config.yaml -jar /var/www/pocket.jar



