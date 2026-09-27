## Define the build argument for the JAR version
##ARG JAR_VERSION=0.0.0
#FROM aspire-java-base:latest
#ARG JAR_VERSION
#COPY ./build/libs/course-${JAR_VERSION}-SNAPSHOT.jar course-service.jar
#
## Set the entry point to run the JAR file
#ENTRYPOINT ["java", "-jar", "course-service.jar"]


#COPY build/libs/*.jar app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]
FROM  aspirebase.azurecr.io/aspire-java-base:latest
COPY *.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]