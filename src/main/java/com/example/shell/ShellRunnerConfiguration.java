package com.example.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
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
			ExitShellRequest exitShellRequest = this.doRun(args);
			System.exit(exitShellRequest.getExitCode());
		}

		private ExitShellRequest doRun(String[] args) {
			return this.doRun(this.stopWatch, this.logger,
					this.commandLine, this.lineShellComponent);
		}

		private ExitShellRequest doRun(StopWatch stopWatch,
		                               Logger logger,
		                               CommandLine commandLine,
		                               JLineShellComponent shell) {
			stopWatch.start();
			try {

				String[] commandsToExecuteAndThenQuit = commandLine.getShellCommandsToExecute();
				ExitShellRequest exitShellRequest;
				if (null != commandsToExecuteAndThenQuit) {

					boolean successful = false;
					exitShellRequest = ExitShellRequest.FATAL_EXIT;

					for (String cmd : commandsToExecuteAndThenQuit) {
						if (!(successful = shell.executeCommand(cmd).isSuccess()))
							break;
					}

					if (successful) {
						exitShellRequest = ExitShellRequest.NORMAL_EXIT;
					}
				} else {
					shell.start();
					shell.promptLoop();
					exitShellRequest = shell.getExitShellRequest();
					if (exitShellRequest == null) {
						exitShellRequest = ExitShellRequest.NORMAL_EXIT;
					}
					shell.waitForComplete();
				}

				if (shell.isDevelopmentMode()) {
					System.out.println("Total execution time: " + stopWatch
							.getLastTaskTimeMillis() + " ms");
				}

				return exitShellRequest;
			} catch (Exception ex) {
				throw new ShellException(ex.getMessage(), ex);
			} finally {
				HandlerUtils.flushAllHandlers(logger);
				stopWatch.stop();
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
