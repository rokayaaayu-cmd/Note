package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app and ime strings from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Termux Keyboard", context.getString(R.string.app_name))
        assertEquals("Termux Smart Keyboard", context.getString(R.string.ime_name))
    }
}
