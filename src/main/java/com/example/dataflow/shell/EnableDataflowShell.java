package com.example.dataflow.shell;

import com.example.shell.EnableShell;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(DataflowImportConfig.class)
@EnableShell
public @interface EnableDataflowShell {
}
