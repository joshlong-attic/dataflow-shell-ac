package test;

import com.example.EnableShell;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableShell
@ComponentScan("org.springframework.cloud.dataflow.shell")
public class ShellApplication {
	public static void main(String args[]) {
		SpringApplication.run(ShellApplication.class, args);
	}
}
