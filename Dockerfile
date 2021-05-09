# ---- Prod ----
FROM openjdk:17-jdk-alpine
LABEL maintainer="Philipp Arndt <2f.mail@gmx.de>"
LABEL version="1.0"
LABEL description="fritzbox to mqtt gateway"

RUN mkdir /opt/app
WORKDIR /opt/app
COPY src/de.rnd7.fritzboxmqttgw/target/fritzbox-to-mqtt-gw.jar .

CMD java -jar ./fritzbox-to-mqtt-gw.jar /var/lib/fritzbox-to-mqtt-gw/config.json
