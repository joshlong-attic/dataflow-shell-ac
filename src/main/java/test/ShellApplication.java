package test;

import com.example.EnableShell;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableShell
@ComponentScan("org.springframework.cloud.dataflow.shell")
public class ShellApplication {
	public static void main(String args[]) {
		SpringApplication.run(ShellApplication.class, args);
	}
}
