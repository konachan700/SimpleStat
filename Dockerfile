FROM adoptopenjdk/openjdk11

COPY target/SimpleStat-1.0-SNAPSHOT.jar /app.jar

CMD [ "sh", "-c", "cd / && java -jar /app.jar"]