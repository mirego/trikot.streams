package com.mirego.trikot.streams.flow

import com.mirego.trikot.streams.reactive.Publishers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@UseExperimental(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AsFlowTests {
    @Test
    fun behaviorAsFlowTest() = runBlockingTest {
        val publisher = Publishers.behaviorSubject(0)
        val flow = publisher.asFlow()
        val values = ArrayList<Int>()
        val job = launch {
            flow.collect {
                values.add(it)
            }
        }
        publisher.value = 1
        job.cancel()

        assertEquals(listOf(0, 1), values)
    }

    @Test
    fun publishAsFlowTest() = runBlockingTest {
        val publisher = Publishers.publishSubject<Int>()
        val flow = publisher.asFlow()
        val values = ArrayList<Int>()
        val job = launch {
            flow.collect {
                values.add(it)
            }
        }
        publisher.value = 1

        assertEquals(listOf(1), values)
        assertFalse { job.isCompleted }

        job.cancel()
    }

    @Test
    fun errorFlowTest() = runBlockingTest {
        val expectedError = IllegalStateException()
        val publisher = Publishers.publishSubject<Int>()
        val flow = publisher.asFlow()
        val values = ArrayList<Int>()
        val job = launch {
            flow.collect {
                values.add(it)
            }
        }
        publisher.error = expectedError

        assertTrue { job.isCompleted }
    }

    @Test
    fun completionFlowTest() = runBlockingTest {
        val publisher = Publishers.publishSubject<Int>()
        val flow = publisher.asFlow()
        val values = ArrayList<Int>()
        val job = launch {
            flow.collect {
                values.add(it)
            }
        }
        publisher.complete()

        assertTrue { job.isCompleted }
    }
}
