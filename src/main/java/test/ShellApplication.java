package test;

import com.example.EnableShell;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@EnableAutoConfiguration
@EnableShell
public class ShellApplication {
	public static void main(String args[]) {
		SpringApplication.run(ShellApplication.class, args);
	}
}
