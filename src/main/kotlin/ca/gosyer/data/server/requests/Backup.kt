/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.requests

@Post
fun backupImportRequest() =
    "/api/v1/backup/import"

@Post
fun backupFileImportRequest() =
    "/api/v1/backup/import/file"

@Post
fun backupExportRequest() =
    "/api/v1/backup/export"

@Post
fun backupFileExportRequest() =
    "/api/v1/backup/export/file"

@Post
fun validateBackupFileRequest() =
    "/api/v1/backup/validate/file"
