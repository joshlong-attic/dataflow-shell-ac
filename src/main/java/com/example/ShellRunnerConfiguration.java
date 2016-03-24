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
 *
 * TODO try rewriting this by _injecting_ `ApplicationArguments` into the component. It _should_ be available before `#afterPropertiesSet`!!
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@Configuration
public class ShellRunnerConfiguration {


	@Component(value = "commandLine")
	@Order(Integer.MIN_VALUE + 1)
	public static class CommandLineCLR extends CommandLine {

		@Autowired
		private JLineShellComponent lineShellComponentCommandLineRunner;
		private CommandLine delegate;
		private final StopWatch stopWatch = new StopWatch("Spring Shell");
		private final Logger logger = Logger.getLogger(getClass().getName());

		public CommandLineCLR() {
			super(null, 0, null);
		}

		void go(String[] strings) throws Exception {
			this.logger.info("in " + getClass().getName() + "#run(String ... args)");
			this.delegate = SimpleShellCommandLineOptions.parseCommandLine(strings);

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

		public ExitShellRequest doRun(String[] args) {
			return this.doRun(this.stopWatch, this.logger, this.delegate, this.lineShellComponentCommandLineRunner, args);
		}

		protected ExitShellRequest doRun(StopWatch stopWatch,
		                                 Logger logger,
		                                 CommandLine commandLine,
		                                 JLineShellComponent shell,
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


	//@Order(Integer.MIN_VALUE   + 0)
	@Component(value = "shell")
	public static class JLineShellComponentCLR
			extends JLineShellComponent {

		// move the processing based on the
		// availability of args to _after_ the initialization callback
		@Override
		public void afterPropertiesSet() {
			// noop
		}

		void go(String[] args) {
			super.afterPropertiesSet();
		}
	}


	@Component
	@Order(Integer.MIN_VALUE)
	public static class PreCLR implements CommandLineRunner {

		@Override
		public void run(String... strings) throws Exception {
			System.err.println("pre()");
			commandLine.go(strings);
			lineShell.go(strings);
		}

		@Autowired
		private JLineShellComponentCLR lineShell;

		@Autowired
		private CommandLineCLR commandLine;

	}


	@Component
	@Order(Integer.MAX_VALUE)
	public static class PostCLR implements CommandLineRunner {

		@Override
		public void run(String... args) throws Exception {
			System.err.println("post()");
			ExitShellRequest exitShellRequest = commandLine.doRun(args);
			System.exit(exitShellRequest.getExitCode());
		}

		@Autowired
		private JLineShellComponentCLR lineShell;

		@Autowired
		private CommandLineCLR commandLine;
	}

}
