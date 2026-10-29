FROM amazoncorretto:25-alpine3.22-jdk
ADD target/app.jar /home/app.jar
WORKDIR /home/
ENV EUREKA_HOST=localhost
ENV EUREKA_PORT=8761
ENV DB_HOST=localhost
ENV DB_PORT=3307
ENV DB_USER=root
ENV DB_PASSWORD=root
ENV DATABASE=Bankingsystem
CMD java -jar app.jar