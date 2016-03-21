package com.example;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactoryBean;
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

import java.lang.reflect.Method;
import java.util.logging.Logger;


/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@Configuration
public class ShellRunnerConfiguration {

	private final StopWatch stopWatch = new StopWatch("Spring Shell");
	private final Logger logger = Logger.getLogger(getClass().getName());
	private CommandLine commandLine;

	/**
	 * create a lazy-initialized {@link CommandLine} that simply forwards calls
	 * to the underlying {@link CommandLine} which is only properly initialized <EM>after</EM>
	 * the {@link CommandLineRunner#run(String...)} callback method is honored.
	 *
	 */
	@Bean
	public CommandLine commandLine(DeferringJLineShellComponent deferringJLineShellComponent) throws NoSuchMethodException {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setProxyTargetClass(true);
		proxyFactoryBean.addInterface(CommandLineRunner.class);
		proxyFactoryBean.setTargetClass(CommandLine.class);
		Method clrRunMethod = CommandLineRunner.class.getMethod("run", String[].class);
		MethodInterceptor methodInterceptor = invocation -> {
			Method method = invocation.getMethod();
			if (method.equals(clrRunMethod)) {
				logger.info("in the run(String[] args) method!");
				Object params = invocation.getArguments()[0];
				doRun(deferringJLineShellComponent, (String[]) params);
				return null;
			} else {
				Assert.notNull(commandLine, "the commandLine hasn't been initialized yet!");
				return method.invoke(commandLine, invocation.getArguments());
			}
		};
		proxyFactoryBean.addAdvice(methodInterceptor);
		proxyFactoryBean.setSingleton(true);
		return CommandLine.class.cast(proxyFactoryBean.getObject());
	}


	@Component
	public static class DeferringJLineShellComponent extends JLineShellComponent {
		@Override
		public void afterPropertiesSet() {
			// noop
		}

		public void deferredAfterPropertiesSet() {
			super.afterPropertiesSet();
		}
	}

	protected void doRun(DeferringJLineShellComponent shell, String[] args) {

		this.stopWatch.start();
		try {
			this.commandLine = SimpleShellCommandLineOptions.parseCommandLine(args);

			shell.deferredAfterPropertiesSet();

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
				System.out.println("Total execution time: " + this.stopWatch
						.getLastTaskTimeMillis() + " ms");
			}

		} catch (Exception ex) {
			throw new ShellException(ex.getMessage(), ex);
		} finally {
			HandlerUtils.flushAllHandlers(this.logger);
			this.stopWatch.stop();
		}
	}
}
