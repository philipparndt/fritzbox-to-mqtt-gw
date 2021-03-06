package de.rnd7.fritzboxmqttgw.fritzbox;

import de.bausdorf.avm.tr064.Action;
import de.bausdorf.avm.tr064.FritzConnection;
import de.bausdorf.avm.tr064.Response;
import de.bausdorf.avm.tr064.Service;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class FritzboxDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FritzboxDownloader.class);

    private final String host;
    private final String username;
    private final String password;

    FritzboxDownloader(final String host, final String username, final String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public JSONObject downloadInfo() throws IOException {

        try {
            final FritzConnection connection = new FritzConnection(host, username, password);
            connection.init(null);

            final JSONObject result = new JSONObject();
            wanDSLIfConfig(connection, result);
            linkConfig(connection, result);
            ethernetInterfaceConfig(connection, result);
            wanInterfaceConfig(connection, result);

            return result;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void wanDSLIfConfig(final FritzConnection connection, final JSONObject result) throws IOException, NoSuchFieldException {
        final Response response = get(connection, "WANDSLInterfaceConfig:1", "GetInfo");

        result.put("NewDownstreamMaxRate", response.getValueAsLong("NewDownstreamMaxRate"));
        result.put("NewUpstreamMaxRate", response.getValueAsLong("NewUpstreamMaxRate"));
        result.put("NewDownstreamCurrRate", response.getValueAsLong("NewDownstreamCurrRate"));
        result.put("NewUpstreamCurrRate", response.getValueAsLong("NewUpstreamCurrRate"));
    }


    private void linkConfig(final FritzConnection connection, final JSONObject result) throws IOException, NoSuchFieldException {
        final Response response = get(connection, "WANDSLLinkConfig:1", "GetStatistics");
        result.put("NewATMCRCErrors", response.getValueAsLong("NewATMCRCErrors"));
        result.put("NewATMTransmittedBlocks", response.getValueAsLong("NewATMTransmittedBlocks"));
        result.put("NewATMReceivedBlocks", response.getValueAsLong("NewATMReceivedBlocks"));
        result.put("NewAAL5CRCErrors", response.getValueAsLong("NewAAL5CRCErrors"));
    }

    private void ethernetInterfaceConfig(final FritzConnection connection, final JSONObject result) throws IOException, NoSuchFieldException {
        final Response response = get(connection, "LANEthernetInterfaceConfig:1", "GetStatistics");
        result.put("NewBytesReceived", response.getValueAsLong("NewBytesReceived"));
        result.put("NewBytesSent", response.getValueAsLong("NewBytesSent"));
        result.put("NewPacketsReceived", response.getValueAsLong("NewPacketsReceived"));
        result.put("NewPacketsSent", response.getValueAsLong("NewPacketsSent"));
    }

    private void wanInterfaceConfig(final FritzConnection connection, final JSONObject result) throws IOException, NoSuchFieldException {
        Response response = get(connection, "WANCommonInterfaceConfig:1", "GetCommonLinkProperties");
        result.put("NewLayer1DownstreamMaxBitRate", response.getValueAsLong("NewLayer1DownstreamMaxBitRate"));
        result.put("NewLayer1UpstreamMaxBitRate", response.getValueAsLong("NewLayer1UpstreamMaxBitRate"));
        result.put("NewPhysicalLinkStatus", response.getValueAsString("NewPhysicalLinkStatus").equalsIgnoreCase("up") ? 1 : 0);

        response = get(connection, "WANCommonInterfaceConfig:1", "GetTotalBytesSent");
        result.put("NewTotalBytesSent", response.getValueAsLong("NewTotalBytesSent"));

        response = get(connection, "WANCommonInterfaceConfig:1", "GetTotalBytesReceived");
        result.put("NewTotalBytesReceived", response.getValueAsLong("NewTotalBytesReceived"));

        response = get(connection, "WANPPPConnection:1", "GetInfo");
        result.put("NewConnectionStatus", response.getValueAsString("NewConnectionStatus").equalsIgnoreCase("connected") ? 1 : 0);
        result.put("NewUptime", response.getValueAsLong("NewUptime"));
        result.put("NewExternalIPAddress", response.getValueAsString("NewExternalIPAddress"));

    }

    private Response get(final FritzConnection connection, final String serviceName, final String actionName) throws IOException {
        final Service service = connection.getService(serviceName);

        final Action action = service.getAction(actionName);
        return action.execute();
    }

}
