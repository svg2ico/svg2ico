/*
 * Copyright 2024 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package release

sealed interface VersionNumber : Comparable<VersionNumber> {
    companion object {
        fun fromString(value: String): ReleaseVersion {
            return if (value.count { it == '.' } == 1) {
                try {
                    val (major, minor) = value.split(".").map { Integer.parseInt(it) }
                    ReleaseVersion.of(major, minor)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException(value, e)
                }
            } else {
                throw IllegalArgumentException(value)
            }
        }
    }

    object DevelopmentVersion : VersionNumber {
        override fun increment() = this

        override fun compareTo(other: VersionNumber): Int = if (other == DevelopmentVersion) 0 else 1

        override fun toString(): String {
            return "development"
        }
    }

    sealed class ReleaseVersion : VersionNumber {

        abstract val majorVersion: Int
        abstract val minorVersion: Int

        companion object {
            fun of(majorVersion: Int, minorVersion: Int): ReleaseVersion {
                return ReleaseVersionData(
                    if (majorVersion >= 0) majorVersion else throw IllegalArgumentException("majorVersion $majorVersion"),
                    if (minorVersion >= 0) minorVersion else throw IllegalArgumentException("minorVersion $minorVersion"),
                )
            }
        }

        private data class ReleaseVersionData(override val majorVersion: Int, override val minorVersion: Int) : ReleaseVersion() {
            override fun toString(): String {
                return "$majorVersion.$minorVersion"
            }
        }

        override fun increment(): VersionNumber {
            return ReleaseVersionData(majorVersion, minorVersion + 1)
        }

        override fun compareTo(other: VersionNumber): Int = when (other) {
            DevelopmentVersion -> -1
            is ReleaseVersion -> {
                compareBy<ReleaseVersion>(
                    { it.majorVersion },
                    { it.minorVersion }
                ).compare(this, other)
            }
        }
    }

    fun increment(): VersionNumber
}