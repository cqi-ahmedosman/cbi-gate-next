FROM amd64/eclipse-temurin:17.0.5_8-jre-jammy
RUN mkdir /app
COPY /build/install/* /app/
RUN chmod 777 /app/bin/*

ENTRYPOINT [ "/app/bin/q2-issuing" ]
