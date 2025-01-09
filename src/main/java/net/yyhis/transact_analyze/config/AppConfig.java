package net.yyhis.transact_analyze.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.yyhis.transact_analyze.util.PriceRange;

@Configuration
public class AppConfig {

    @Bean
    public PriceRange listFillter() {
        return new PriceRange();
    }
}
