package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CampusRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name and campus topographical data`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("وجهني - الملاحة التكتيكية", appName)
        assertEquals(5, CampusRepository.buildings.size)
        assertTrue(CampusRepository.halls.any { it.code == "#315" && it.steps.size == 4 })
        assertTrue(CampusRepository.halls.any { it.code == "#402" })
        assertTrue(CampusRepository.halls.any { it.code == "#208" })
    }
}
