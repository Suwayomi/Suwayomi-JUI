/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.requests

fun downloadsQuery() =
    "/api/v1/downloads"

fun downloadsStartRequest() =
    "/api/v1/downloads/start"

fun downloadsStopRequest() =
    "/api/v1/downloads/stop"

fun downloadsClearRequest() =
    "/api/v1/downloads/clear"
