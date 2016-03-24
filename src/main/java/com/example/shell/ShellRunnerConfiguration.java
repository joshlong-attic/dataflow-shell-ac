package com.example.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.shell.CommandLine;
import org.springframework.shell.ShellException;
import org.springframework.shell.SimpleShellCommandLineOptions;
import org.springframework.shell.core.ExitShellRequest;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

import java.util.logging.Logger;


/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@Configuration
public class ShellRunnerConfiguration {

	/**
	 * This does basically the same thing as {@link org.springframework.shell.Bootstrap} in Spring Shell,
	 * but using Spring Boot's {@link CommandLineRunner} as a callback hook for initialization, instead
	 * of squatting on the application's one {@code main(String []args)} method.
	 */
	@Component
	public static class ShellBootstrapCommandLineRunner implements CommandLineRunner {

		private StopWatch stopWatch = new StopWatch("Spring Shell");
		private Logger logger = Logger.getLogger(getClass().getName());

		@Autowired
		private JLineShellComponent lineShellComponent;

		@Autowired
		private CommandLine commandLine;

		@Override
		public void run(String... args) throws Exception {
			ExitShellRequest exitShellRequest = this.doRun();
			System.exit(exitShellRequest.getExitCode());
		}

		private ExitShellRequest doRun() {
			this.stopWatch.start();
			try {

				String[] commandsToExecuteAndThenQuit = this.commandLine.getShellCommandsToExecute();
				ExitShellRequest exitShellRequest;
				if (null != commandsToExecuteAndThenQuit) {

					boolean successful = false;
					exitShellRequest = ExitShellRequest.FATAL_EXIT;

					for (String cmd : commandsToExecuteAndThenQuit) {
						if (!(successful = this.lineShellComponent.executeCommand(cmd).isSuccess()))
							break;
					}

					if (successful) {
						exitShellRequest = ExitShellRequest.NORMAL_EXIT;
					}
				} else {
					this.lineShellComponent.start();
					this.lineShellComponent.promptLoop();
					exitShellRequest = this.lineShellComponent.getExitShellRequest();
					if (exitShellRequest == null) {
						exitShellRequest = ExitShellRequest.NORMAL_EXIT;
					}
					this.lineShellComponent.waitForComplete();
				}

				if (this.lineShellComponent.isDevelopmentMode()) {
					System.out.println("Total execution time: " + this.stopWatch
							.getLastTaskTimeMillis() + " ms");
				}

				return exitShellRequest;
			} catch (Exception ex) {
				throw new ShellException(ex.getMessage(), ex);
			} finally {
				HandlerUtils.flushAllHandlers(this.logger);
				this.stopWatch.stop();
			}
		}

	}

	@Component(value = "commandLine")
	public static class ApplicationArgsAwareCommandLine extends CommandLine {

		private CommandLine delegate;

		public ApplicationArgsAwareCommandLine() {
			super(null, 0, null);
		}

		@Autowired
		protected void configArguments(ApplicationArguments args) throws Exception {
			//System.out.println ("setting up the " + ApplicationArguments.class.getName() +'.');
			this.delegate = SimpleShellCommandLineOptions.parseCommandLine(
					args.getSourceArgs());
		}

		@Override
		public String[] getArgs() {
			this.nonNull();
			return this.delegate.getArgs();
		}

		@Override
		public int getHistorySize() {
			this.nonNull();
			return this.delegate.getHistorySize();
		}

		@Override
		public String[] getShellCommandsToExecute() {
			this.nonNull();
			return this.delegate.getShellCommandsToExecute();
		}

		@Override
		public boolean getDisableInternalCommands() {
			this.nonNull();
			return this.delegate.getDisableInternalCommands();
		}

		private void nonNull() {
			Assert.notNull(this.delegate, "the delegate hasn't been initialized yet!");
		}
	}

	@Bean
	public JLineShellComponent shell() {
		return new JLineShellComponent();
	}
}
