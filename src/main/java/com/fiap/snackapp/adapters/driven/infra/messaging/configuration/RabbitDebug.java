package com.fiap.snackapp.adapters.driven.infra.messaging.configuration;

import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

@Component
public class RabbitDebug {

    public RabbitDebug(RabbitProperties props) {
        System.out.println("RABBIT HOST = " + props.getHost());
        System.out.println("RABBIT PORT = " + props.getPort());
        System.out.println("RABBIT SSL  = " + props.getSsl().getEnabled());
    }
}
