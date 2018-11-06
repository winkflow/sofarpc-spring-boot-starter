package io.wink.tool.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rpc.sofa")
public class SofaProperties {
    //默认url
    private String zkHost = "127.0.0.1";

    private int zkPort = 2181;

    private int sofaTimeout = 3000;

    private int sofaPort = 22101;

    public String getZkHost() {
        return zkHost;
    }

    public void setZkHost(String zkHost) {
        this.zkHost = zkHost;
    }

    public int getZkPort() {
        return zkPort;
    }

    public void setZkPort(int zkPort) {
        this.zkPort = zkPort;
    }

    public int getSofaTimeout() {
        return sofaTimeout;
    }

    public void setSofaTimeout(int sofaTimeout) {
        this.sofaTimeout = sofaTimeout;
    }

    public int getSofaPort() {
        return sofaPort;
    }

    public void setSofaPort(int sofaPort) {
        this.sofaPort = sofaPort;
    }
}
