FROM maven:3.6.3-openjdk-15
WORKDIR /tests
COPY . .
CMD mvn clean test