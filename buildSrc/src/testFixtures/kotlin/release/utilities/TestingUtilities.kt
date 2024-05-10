/*
 * Copyright 2024 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package release.utilities

import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.*
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.io.path.createTempFile
import kotlin.io.path.writeBytes
import kotlin.time.Duration
import kotlin.time.toJavaDuration

fun <T> withTemporaryFile(contents: ByteArray, block: (Path) -> T): T {
    val temporaryFile: Path = createTempFile()
    return try {
        temporaryFile.writeBytes(contents)
        block(temporaryFile)
    } finally {
        temporaryFile.toFile().delete()
    }
}

fun <T> withTimeout(timeout: Duration, block: () -> T): T {
    val executor = Executors.newSingleThreadExecutor()
    try {
        val future: Future<T> = executor.submit(Callable { block() })
        val startInstant = Instant.now()
        while(startInstant.plus(timeout.toJavaDuration()) > Instant.now() && !future.isDone) {
            Thread.sleep(10)
        }
        return if (future.isDone) {
            try {
                future.get()
            } catch (executionException: ExecutionException) {
                throw executionException.cause!!
            }
        } else {
            throw TimeoutException("Timed out after $timeout")
        }
    } finally {
        executor.shutdownNow()
        executor.awaitTermination(60, SECONDS)
    }
}