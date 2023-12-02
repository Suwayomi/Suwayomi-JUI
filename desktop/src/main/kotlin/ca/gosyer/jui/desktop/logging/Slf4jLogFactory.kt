/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.desktop.logging

import org.lighthousegames.logging.KmLog
import org.lighthousegames.logging.LogFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Slf4jLogFactory : LogFactory {
    override fun createKmLog(
        tag: String,
        className: String,
    ): KmLog = Slf4jLog(tag)
}

class Slf4jLog(
    tag: String,
) : KmLog(tag) {
    private val logger: Logger = LoggerFactory.getLogger(tag)

    override fun verbose(
        tag: String,
        msg: String,
    ) {
        super.verbose(tag, msg)
        logger.trace(msg)
    }

    override fun debug(
        tag: String,
        msg: String,
    ) {
        super.debug(tag, msg)
        logger.debug(msg)
    }

    override fun info(
        tag: String,
        msg: String,
    ) {
        super.info(tag, msg)
        logger.info(msg)
    }

    override fun warn(
        tag: String,
        msg: String,
        t: Throwable?,
    ) {
        super.warn(tag, msg, t)
        if (t != null) {
            logger.warn(msg, t)
        } else {
            logger.warn(msg)
        }
    }

    override fun error(
        tag: String,
        msg: String,
        t: Throwable?,
    ) {
        super.error(tag, msg, t)
        if (t != null) {
            logger.error(msg, t)
        } else {
            logger.error(msg)
        }
    }
}
