package de.rnd7.fritzboxmqttgw;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import de.rnd7.mqttgateway.GwMqttClient;
import de.rnd7.mqttgateway.config.ConfigParser;
import de.rnd7.fritzboxmqttgw.config.BoxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rnd7.fritzboxmqttgw.config.Config;
import de.rnd7.fritzboxmqttgw.fritzbox.FritzboxService;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final Config config;

    @SuppressWarnings("squid:S2189")
    public Main(final Config config) {
        this.config = config;

        final GwMqttClient client = GwMqttClient.start(config.getMqtt()
                .setDefaultClientId("fritzbox-mqtt-gw")
                .setDefaultTopic("fritzbox"));

        client.online();

        new FritzboxService(config.getFritzbox()).start();
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            LOGGER.error("Expected configuration file as argument");
            return;
        }

        try {
            final Config config = ConfigParser.parse(new File(args[0]), Config.class);

            if (EnumSet.of(BoxType.dsl, BoxType.cable).contains(config.getFritzbox().getBoxType())) {
                new Main(config);
            }
            else {
                LOGGER.error("BoxType <" + config.getFritzbox().getBoxType() + "> not supported", new Throwable());
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
