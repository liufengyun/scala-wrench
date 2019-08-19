package org.scalaverify
package test

import org.junit.{ Test, BeforeClass, AfterClass }
import org.junit.Assert._

class CompilationTests {
    @Test
    def initTests = {
        assertEquals(3, 3)
    }
}