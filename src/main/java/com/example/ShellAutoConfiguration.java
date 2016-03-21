package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@Configuration
public class ShellAutoConfiguration {

	private static final Log LOG = LogFactory.getLog(ShellAutoConfiguration.class);

	@Configuration
	@ComponentScan({"org.springframework.shell.converters", "org.springframework.shell.plugin.support"})
	public static class DefaultShellComponents {

		@PostConstruct
		public void log() {
			LOG.info("default Spring Shell packages being scanned");
		}
	}

	@Configuration
	@ConditionalOnProperty(value = "disableInternalCommands", havingValue = "false", matchIfMissing = true, relaxedNames = true)
	@ComponentScan("org.springframework.shell.commands")
	public static class RegisterInternalCommands {

		@PostConstruct
		public void log() {
			LOG.info("internal Spring Shell command packages being scanned");
		}
	}
}
