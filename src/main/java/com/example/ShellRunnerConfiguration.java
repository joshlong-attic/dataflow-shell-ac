package com.example;

import org.springframework.beans.factory.annotation.Autowired;
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


	@Component(value = "commandLine")
	@Order(Integer.MIN_VALUE)
	public static class CommandLineCommandLineRunner extends CommandLine implements CommandLineRunner {

		@Autowired
		private JLineShellComponentCommandLineRunner JLineShellComponentCommandLineRunner;
		private CommandLine delegate;
		private final StopWatch stopWatch = new StopWatch("Spring Shell");
		private final Logger logger = Logger.getLogger(getClass().getName());

		public CommandLineCommandLineRunner() {
			super(null, 0, null);
		}

		@Override
		public void run(String... strings) throws Exception {
			this.logger.info("in " + getClass().getName() + "#run(String ... args)");
			this.delegate = SimpleShellCommandLineOptions.parseCommandLine(strings);
			ExitShellRequest exitShellRequest = doRun(stopWatch, logger, this.delegate, this.JLineShellComponentCommandLineRunner, strings);
			this.logger.info("exiting with " + exitShellRequest.getClass().getName() + " exit code " + exitShellRequest.getExitCode() + '.');
			System.exit(exitShellRequest.getExitCode());
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

		protected ExitShellRequest doRun(StopWatch stopWatch,
		                                 Logger logger,
		                                 CommandLine commandLine,
		                                 JLineShellComponentCommandLineRunner shell,
		                                 String[] args) {

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

		private void nonNull() {
			Assert.notNull(this.delegate, "the delegate hasn't been initialized yet!");
		}
	}


	@Component
	public static class JLineShellComponentCommandLineRunner extends JLineShellComponent implements CommandLineRunner {

		@Override
		public void afterPropertiesSet() {
			// noop
		}

		@Override
		public void run(String... strings) throws Exception {
			super.afterPropertiesSet();
		}

	}


}
