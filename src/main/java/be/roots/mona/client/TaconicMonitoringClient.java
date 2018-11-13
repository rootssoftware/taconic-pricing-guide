package be.roots.mona.client;

import org.springframework.boot.SpringBootVersion;

public class TaconicMonitoringClient extends MonitoringClient {

    public TaconicMonitoringClient(String region, String environment, String username, String password) {
        super(environment, username, password, SpringBootVersion.getVersion());
        this.hostname += "-" + region;
    }

    public TaconicMonitoringClient(String region, String url, String environment, String username, String password) {
        super(url, environment, username, password, SpringBootVersion.getVersion());
        this.hostname += "-" + region;
    }

}