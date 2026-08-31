package com.harmonia.common.observability;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource("classpath:harmonia-observability.properties")
public class ObservabilityAutoConfiguration {
}
