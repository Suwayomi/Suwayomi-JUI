/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.requests

@WS
fun downloadsQuery() =
    "/api/v1/downloads"

@Get
fun downloadsStartRequest() =
    "/api/v1/downloads/start"

@Get
fun downloadsStopRequest() =
    "/api/v1/downloads/stop"

@Get
fun downloadsClearRequest() =
    "/api/v1/downloads/clear"
