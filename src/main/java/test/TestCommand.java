package test;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class TestCommand implements CommandMarker {

	@CliCommand(value = "hw simple", help = "Print a simple hello world message")
	public String simple(
			@CliOption(key = {"message"}, mandatory = true, help = "The hello world message") String message,
			@CliOption(key = {"location"}, mandatory = false, help = "Where you are saying hello", specifiedDefaultValue = "At work") String location) {
		return "Message = [" + message + "] Location = [" + location + "]";
	}
}
