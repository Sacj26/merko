#!/bin/bash
echo "Building application with Maven..."
mvn clean package -DskipTests
echo "Starting Spring Boot application..."
java -jar /home/site/wwwroot/target/merko-0.0.1-SNAPSHOT.jar
