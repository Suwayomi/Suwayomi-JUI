/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.core.logging

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import java.io.File

const val consolePattern =
    "%highlight{%d{" + '$' + "{LOG_DATEFORMAT_PATTERN:-HH:mm:ss.SSS}} [%t] " + '$' + "{LOG_LEVEL_PATTERN:-%p}/%c{1}: %m%n" + '$' + "{LOG_EXCEPTION_CONVERSION_WORD:-%xEx}}{FATAL=red blink, ERROR=red, WARN=yellow bold, INFO=black, DEBUG=black, TRACE=black}"
const val filePattern =
    "%d{" + '$' + "{LOG_DATEFORMAT_PATTERN:-HH:mm:ss.SSS}} [%t] " + '$' + "{LOG_LEVEL_PATTERN:-%p}/%c{1}: %m%n" + '$' + "{LOG_EXCEPTION_CONVERSION_WORD:-%xEx}"

@Suppress("UPPER_BOUND_VIOLATED_WARNING")
fun initializeLogger(loggingLocation: File) {
    val ctx = LogManager.getContext(false) as LoggerContext
    val builder = ConfigurationBuilderFactory.newConfigurationBuilder()
        .apply {
            setStatusLevel(Level.WARN)
            setConfigurationName("LoggerBuilder")
            add(
                newAppender("Console", "Console")
                    .addAttribute(
                        "target",
                        ConsoleAppender.Target.SYSTEM_OUT
                    )
                    .add(
                        newLayout("PatternLayout")
                            .addAttribute("disableAnsi", "false")
                            .addAttribute("pattern", consolePattern)
                    )
            )
            add(
                newAppender("Rolling", "RollingFile")
                    .addAttribute(
                        "fileName",
                        loggingLocation.absolutePath.trimEnd { it == '/' || it == '\\' } + "/rolling.log"
                    )
                    .addAttribute(
                        "filePattern",
                        loggingLocation.absolutePath.trimEnd { it == '/' || it == '\\' } + "/archive/rolling-%d{MM-dd-yy}.log.gz"
                    )
                    .add(
                        newLayout("PatternLayout")
                            .addAttribute("pattern", filePattern)
                    )
                    .addComponent(
                        newComponent<ComponentBuilder<*>>("Policies")
                            .addComponent(
                                newComponent<ComponentBuilder<*>>("CronTriggeringPolicy")
                                    .addAttribute("schedule", "0 0 0 * * ?")
                                    .addAttribute("evaluateOnStartup", "true")
                            )
                            .addComponent(
                                newComponent<ComponentBuilder<*>>("SizeBasedTriggeringPolicy")
                                    .addAttribute("size", "100M")
                            )
                    )
            )
            add(
                newRootLogger(Level.DEBUG)
                    .add(newAppenderRef("Console"))
                    .add(newAppenderRef("Rolling"))
            )
        }
    ctx.configuration = builder.build()
    ctx.updateLoggers()
}
