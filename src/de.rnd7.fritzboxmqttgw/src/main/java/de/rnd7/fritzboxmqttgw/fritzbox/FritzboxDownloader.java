package de.rnd7.fritzboxmqttgw.fritzbox;

import de.bausdorf.avm.tr064.Action;
import de.bausdorf.avm.tr064.FritzConnection;
import de.bausdorf.avm.tr064.Response;
import de.bausdorf.avm.tr064.Service;
import de.rnd7.fritzboxmqttgw.config.BoxType;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;

class FritzboxDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FritzboxDownloader.class);

    private final String host;
    private final String username;
    private final String password;
    private final BoxType type;

    FritzboxDownloader(final String host, final String username, final String password, final BoxType type) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.type = type;
    }

    public JSONObject downloadInfo() throws IOException {

        try {
            final FritzConnection connection = new FritzConnection(host, username, password);
            connection.init(null);

            final JSONObject result = new JSONObject();

            switch (type) {
                case dsl: {
                    wanDSLIfConfig(connection, result);
                    linkConfig(connection, result);
                    break;
                }
                case cable: {
                    wanIpConfig(connection, result);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("BoxType <" + type + "> not supported");
            }

            ethernetInterfaceConfig(connection, result);
            wanInterfaceConfig(connection, result);

            return result;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void wanDSLIfConfig(final FritzConnection connection, final JSONObject result) throws IOException {
        final Response response = get(connection, "WANDSLInterfaceConfig:1", "GetInfo");

        Stream.of("NewDownstreamMaxRate", "NewUpstreamMaxRate", "NewDownstreamCurrRate", "NewUpstreamCurrRate")
            .forEach(key -> putLong(key, response, result));
    }

    private void linkConfig(final FritzConnection connection, final JSONObject result) throws IOException {
        final Response response = get(connection, "WANDSLLinkConfig:1", "GetStatistics");

        Stream.of("NewATMCRCErrors", "NewATMTransmittedBlocks", "NewATMReceivedBlocks", "NewAAL5CRCErrors")
            .forEach(key -> putLong(key, response, result));
    }

    private void wanIpConfig(final FritzConnection connection, final JSONObject result) throws IOException {
        final Response response = get(connection, "WANIPConnection:1", "GetInfo");

        Stream.of("NewUptime")
            .forEach(key -> putLong(key, response, result));

        Stream.of("NewEnable", "NewRSIPAvailable", "NewNATEnabled", "NewDNSEnabled", "NewDNSOverrideAllowed")
            .forEach(key -> putBool(key, response, result));

        Stream.of("NewConnectionStatus", "NewPossibleConnectionTypes", "NewConnectionType", "NewName",
            "NewLastConnectionError", "NewExternalIPAddress", "NewDNSServers", "NewMACAddress", "NewConnectionTrigger",
            "NewRouteProtocolRx")
            .forEach(key -> putString(key, response, result));
    }

    private void ethernetInterfaceConfig(final FritzConnection connection, final JSONObject result) throws IOException {
        final Response response = get(connection, "LANEthernetInterfaceConfig:1", "GetStatistics");

        Stream.of("NewBytesReceived", "NewBytesSent", "NewPacketsReceived", "NewPacketsSent")
            .forEach(key -> putLong(key, response, result));
    }

    private void wanInterfaceConfig(final FritzConnection connection, final JSONObject result) throws IOException {
        final Response response1 = get(connection, "WANCommonInterfaceConfig:1", "GetCommonLinkProperties");
        Stream.of("NewLayer1DownstreamMaxBitRate", "NewLayer1UpstreamMaxBitRate")
            .forEach(key -> putLong(key, response1, result));

        Stream.of("NewPhysicalLinkStatus")
            .forEach(key -> putString(key, response1, result));

        final Response response2 = get(connection, "WANCommonInterfaceConfig:1", "GetTotalBytesSent");
        Stream.of("NewTotalBytesSent")
            .forEach(key -> putLong(key, response2, result));

        final Response response3 = get(connection, "WANCommonInterfaceConfig:1", "GetTotalBytesReceived");
        Stream.of("NewTotalBytesReceived")
            .forEach(key -> putLong(key, response3, result));

        if (type == BoxType.dsl) {
            final Response response4 = get(connection, "WANPPPConnection:1", "GetInfo");
            Stream.of("NewUptime")
                .forEach(key -> putLong(key, response4, result));
            Stream.of("NewConnectionStatus", "NewExternalIPAddress")
                .forEach(key -> putString(key, response4, result));
        }
    }

    private void putString(String key, Response source, final JSONObject target) {
        try {
            target.put(key, source.getValueAsString(key));
        } catch (NoSuchFieldException e) {
            notFound(key, e);
        }
    }

    private void putLong(String key, Response source, final JSONObject target) {
        try {
            target.put(key, source.getValueAsLong(key));
        } catch (NoSuchFieldException e) {
            notFound(key, e);
        }
    }

    private void putBool(String key, Response source, final JSONObject target) {
        try {
            target.put(key, source.getValueAsBoolean(key));
        } catch (NoSuchFieldException e) {
            notFound(key, e);
        }
    }

    private void notFound(final String key, final NoSuchFieldException e) {
        LOGGER.error("Value not found: {}", key, e);
    }

    private Response get(final FritzConnection connection, final String serviceName, final String actionName) throws IOException {
        final Service service = connection.getService(serviceName);

        final Action action = service.getAction(actionName);
        return action.execute();
    }
}
