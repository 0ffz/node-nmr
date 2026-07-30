package me.dvyy.nmr

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import io.kotest.matchers.equals.shouldEqual
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DerivedStateTests {
    @Test
    fun `should update derived state with reference to other state`() {
        var ref by mutableStateOf(mutableStateOf("test"))
        val derived by derivedStateOf {
            println("Calculating")
            ref.value
        }
        derived shouldEqual "test"
        ref = mutableStateOf("something else")
        derived shouldEqual "something else"
        ref.value = "something else 2"
        derived shouldEqual "something else 2"
    }
}
