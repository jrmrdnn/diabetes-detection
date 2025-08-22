package com.medilabo.noteService;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@Profile("test")
public class TestMongoConfig {

    private MongodExecutable mongodExecutable;

    @Bean
    public MongodExecutable embeddedMongoServer() throws IOException {
        String ip = "localhost";
        int port = 27017;

        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();

        MongodStarter starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        return mongodExecutable;
    }

    @PreDestroy
    public void stopEmbeddedMongoServer() {
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }
}