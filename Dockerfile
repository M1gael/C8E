# --- build test 1 (form/mongo) ---
FROM eclipse-temurin:21-jdk-alpine AS builder1
WORKDIR /app1
# copy only the project files inside FormCapture
COPY test1/FormCapture/ .
RUN chmod +x gradlew
# build the executable jar
RUN ./gradlew shadowJar

# --- build test 2 (csv/sqlite) ---
FROM eclipse-temurin:21-jdk-alpine AS builder2
WORKDIR /app2
# copy only the project files inside ArraysAndFileHandling
COPY test2/ArraysAndFileHandling/ .
RUN chmod +x gradlew
# build the executable jar
RUN ./gradlew shadowJar

# --- final runner ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy JARs using wildcards (*) so exact naming doesn't matter
COPY --from=builder1 /app1/build/libs/*.jar ./test1.jar
COPY --from=builder2 /app2/build/libs/*.jar ./test2.jar

# Create a startup script to run BOTH apps in parallel
RUN echo '#!/bin/sh' > run.sh && \
    echo 'java -jar test1.jar &' >> run.sh && \
    echo 'java -jar test2.jar' >> run.sh && \
    chmod +x run.sh

# Expose the ports
EXPOSE 8000 8001

# Run the script
CMD ["./run.sh"]