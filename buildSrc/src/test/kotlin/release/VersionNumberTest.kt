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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import release.VersionNumber.DevelopmentVersion
import release.VersionNumber.ReleaseVersion

class VersionNumberTest {
    @Test
    fun `development is later than a release version`() {
        DevelopmentVersion shouldBeGreaterThan ReleaseVersion.of(1, 1)
    }

    @Test
    fun `development is equal to development using compare to`() {
        DevelopmentVersion shouldBeEqualComparingTo DevelopmentVersion
    }

    @Test
    fun `release version is earlier than development`() {
        ReleaseVersion.of(1, 1) shouldBeLessThan DevelopmentVersion
    }

    @Test
    fun `release version is equal to the same release version using compare to`() {
        ReleaseVersion.of(1, 1) shouldBeEqualComparingTo ReleaseVersion.of(1, 1)
    }

    @Test
    fun `release version is greater than release version with same major version but smaller minor version`() {
        ReleaseVersion.of(1, 1) shouldBeGreaterThan ReleaseVersion.of(1, 0)
    }

    @Test
    fun `release version is greater than release version with smaller major version but larger minor version`() {
        ReleaseVersion.of(2, 0) shouldBeGreaterThan ReleaseVersion.of(1, 1)
    }

    @Test
    fun `major version cannot be negative`() {
        shouldThrow<IllegalArgumentException> {
            ReleaseVersion.of(-1, 0)
        }.message shouldBe "majorVersion -1"
    }

    @Test
    fun `minor version cannot be negative`() {
        shouldThrow<IllegalArgumentException> {
            ReleaseVersion.of(0, -1)
        }.message shouldBe "minorVersion -1"
    }

    @Test
    fun `can increment release version`() {
        ReleaseVersion.of(0, 0).increment() shouldBe ReleaseVersion.of(0, 1)
    }

    @Test
    fun `incremented development version is itself`() {
        DevelopmentVersion.increment() shouldBe DevelopmentVersion
    }

    @Test
    fun `release version toString is correct`() {
        ReleaseVersion.of(0, 0).toString() shouldBe "0.0"
    }

    @Test
    fun `development version toString is correct`() {
        DevelopmentVersion.toString() shouldBe "development"
    }

    @Test
    fun `can construct release version from valid string`() {
        VersionNumber.fromString("1.70") shouldBe ReleaseVersion.of(1, 70)
    }

    @Test
    fun `attempting to construct release version from string with no dots throws`() {
        val value = "1"
        shouldThrow<IllegalArgumentException> {
            VersionNumber.fromString(value)
        }.message shouldBe value
    }

    @Test
    fun `attempting to construct release version from string with two dots throws`() {
        val value = "1.70.1"
        shouldThrow<IllegalArgumentException> {
            VersionNumber.fromString(value)
        }.message shouldBe value
    }

    @Test
    fun `attempting to construct release version with empty major version throws`() {
        val value = ".70"
        shouldThrow<IllegalArgumentException> {
            VersionNumber.fromString(value)
        }.message shouldBe value
    }

    @Test
    fun `attempting to construct release version with empty minor version throws`() {
        val value = "1."
        shouldThrow<IllegalArgumentException> {
            VersionNumber.fromString(value)
        }.message shouldBe value
    }

    @Test
    fun `attempting to construct release version with negative major version throws`() {
        val value = "-1.70"
        shouldThrow<IllegalArgumentException> {
            VersionNumber.fromString(value)
        }.message shouldBe value
    }

    @Test
    fun `attempting to construct release version with negative minor version throws`() {
        val value = "1.-70"
        shouldThrow<IllegalArgumentException> {
            VersionNumber.fromString(value)
        }.message shouldBe value
    }

    @Test
    fun `attempting to construct release version with non-integer major version throws`() {
        val value = "foo.70"
        shouldThrow<IllegalArgumentException> {
            VersionNumber.fromString(value)
        }.message shouldBe value
    }

    @Test
    fun `attempting to construct release version with non-integer minor version throws`() {
        val value = "1.foo"
        shouldThrow<IllegalArgumentException> {
            VersionNumber.fromString(value)
        }.message shouldBe value
    }
}