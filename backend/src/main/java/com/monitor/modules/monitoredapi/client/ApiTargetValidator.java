package com.monitor.modules.monitoredapi.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;

@Component
public class ApiTargetValidator {

    private final boolean allowPrivateTargets;

    public ApiTargetValidator(@Value("${monitor.security.allow-private-targets:false}") boolean allowPrivateTargets) {
        this.allowPrivateTargets = allowPrivateTargets;
    }

    public void validate(String targetUrl) {
        URI uri;
        try {
            uri = URI.create(targetUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("URL de monitoramento invalida.");
        }

        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
            throw new IllegalArgumentException("Apenas URLs HTTP e HTTPS com host valido sao permitidas.");
        }

        if (allowPrivateTargets) {
            return;
        }

        try {
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new IllegalArgumentException("O destino aponta para uma rede privada ou reservada.");
                }
            }
        } catch (java.net.UnknownHostException exception) {
            throw new IllegalArgumentException("Nao foi possivel resolver o host do endpoint.");
        }
    }
}
