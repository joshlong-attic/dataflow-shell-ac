package test;

import com.example.dataflow.shell.EnableDataflowShell;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDataflowShell
public class ShellApplication {
	public static void main(String args[]) {
		SpringApplication.run(ShellApplication.class, args);
	}
}
