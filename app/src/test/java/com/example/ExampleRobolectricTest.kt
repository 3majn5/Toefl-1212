package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.VocabRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TOEFL 1212", appName)
    }

    @Test
    fun `verify vocabulary dataset has 1212 entries across 46 pages`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = VocabRepository(context)
        val audits = repository.getPagesAudit()

        assertEquals(46, audits.size)
        val totalCount = audits.sumOf { it.entryCount }
        assertEquals(1212, totalCount)

        // Verify page 1 and page 46 ranges
        assertEquals(1, audits.first().page)
        assertEquals(1, audits.first().startNumber)
        assertEquals(21, audits.first().endNumber)

        assertEquals(46, audits.last().page)
        assertEquals(1209, audits.last().startNumber)
        assertEquals(1212, audits.last().endNumber)
    }

    @Test
    fun `verify initializeDatabaseIfNeeded inserts all 1212 entries without crash`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = VocabRepository(context)
        repository.initializeDatabaseIfNeeded()
        val all = repository.getAllEntriesDirect()
        assertEquals(1212, all.size)
    }

    @Test
    fun `launch MainActivity without crash`() {
        androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
            }
        }
    }
}
