/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.desktop.logging

import ca.gosyer.jui.desktop.build.BuildConfig
import com.github.weisj.darklaf.LafManager
import okio.Path
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.lighthousegames.logging.logging
import org.slf4j.bridge.SLF4JBridgeHandler
import java.util.logging.LogManager as JLogManager

const val consolePattern =
    "%highlight{%d{" +
        '$' +
        "{LOG_DATEFORMAT_PATTERN:-HH:mm:ss.SSS}} [%t] " +
        '$' +
        "{LOG_LEVEL_PATTERN:-%p}/%c{1}: %m%n" +
        '$' +
        "{LOG_EXCEPTION_CONVERSION_WORD:-%xEx}}{FATAL=red blink, ERROR=red, WARN=yellow bold, INFO=normal, DEBUG=normal, TRACE=normal}"
const val filePattern =
    "%d{" +
        '$' +
        "{LOG_DATEFORMAT_PATTERN:-HH:mm:ss.SSS}} [%t] " +
        '$' +
        "{LOG_LEVEL_PATTERN:-%p}/%c{1}: %m%n" +
        '$' +
        "{LOG_EXCEPTION_CONVERSION_WORD:-%xEx}"

@Suppress("UPPER_BOUND_VIOLATED_WARNING")
fun initializeLogger(loggingLocation: Path) {
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
                        loggingLocation.toString().trimEnd('/', '\\') + "/rolling.log"
                    )
                    .addAttribute(
                        "filePattern",
                        loggingLocation.toString().trimEnd('/', '\\') + "/archive/rolling-%d{yyyy-MM-dd-}.log.gz"
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

    // Initialize Darklaf logger
    LafManager.setLogLevel(
        if (BuildConfig.DEBUG) {
            java.util.logging.Level.FINE
        } else {
            java.util.logging.Level.WARNING
        }
    )
    JLogManager.getLogManager().getLogger("com.github.weisj.darklaf").apply {
        handlers.forEach { removeHandler(it) }
        addHandler(SLF4JBridgeHandler())
    }
    val log = logging("UncaughtException")
    Thread.setDefaultUncaughtExceptionHandler { t, e ->
        log.error(e) { "Uncaught exception in thread [${t.name}@${t.id}]" }
    }
}
