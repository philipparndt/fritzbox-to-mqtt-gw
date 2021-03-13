package de.rnd7.fritzboxmqttgw.fritzbox;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.rnd7.fritzboxmqttgw.config.ConfigFritzbox;
import de.rnd7.mqttgateway.Events;
import de.rnd7.mqttgateway.PublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FritzboxService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FritzboxService.class);

    private final ConfigFritzbox config;

    public FritzboxService(final ConfigFritzbox config) {
        this.config = config;
    }

    public void start() {
        try {
            final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(this::exec, 0, config.getPollingInterval().getSeconds(), TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void exec() {
        try {
            final FritzboxDownloader downloader = new FritzboxDownloader(config.getHost(),
                    config.getUsername(),
                    config.getPassword(),
                    config.getBoxType());

            final String message = downloader.downloadInfo().toString();
            Events.post(PublishMessage.relative(config.getBoxType().toString(),
                    message));
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
