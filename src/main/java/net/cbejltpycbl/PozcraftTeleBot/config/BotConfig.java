package net.cbejltpycbl.PozcraftTeleBot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {

    @Value("${bot.token}")
    private String token;

    @Value("#{'${bot.admins}'.split(',')}")
    private int[] admins;

    @Value("${bot.name}")
    private String botName;

}
