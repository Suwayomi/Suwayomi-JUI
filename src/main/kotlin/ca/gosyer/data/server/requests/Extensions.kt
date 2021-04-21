/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.requests

@Get
fun extensionListQuery() =
    "/api/v1/extension/list"

@Get
fun apkInstallQuery(apkName: String) =
    "/api/v1/extension/install/$apkName"

@Get
fun apkUninstallQuery(apkName: String) =
    "/api/v1/extension/uninstall/$apkName"

@Get
fun apkIconQuery(apkName: String) =
    "/api/v1/extension/icon/$apkName"