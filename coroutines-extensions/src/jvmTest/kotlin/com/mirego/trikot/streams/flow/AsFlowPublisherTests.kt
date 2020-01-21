package com.mirego.trikot.streams.flow

import com.mirego.trikot.streams.cancellable.CancellableManager
import com.mirego.trikot.streams.reactive.subscribe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@UseExperimental(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AsFlowPublisherTests {
    @Test
    fun simpleasFlowPublisherTest() = runBlockingTest {
        val flowMock = flowMock()
        val publisher = flowMock.asFlowPublisher()

        val values = ArrayList<Int>()
        var isCompleted = false
        var error: Throwable? = null
        publisher.subscribe(CancellableManager(),
            onNext = { values.add(it) },
            onError = { error = it },
            onCompleted = { isCompleted = true }
        )
        assertEquals(listOf(0, 1), values)
        assertTrue { isCompleted }
        assertNull(error)
    }

    @Test
    fun erroredFlowPublisherTest() = runBlockingTest {
        val exception = IllegalStateException()
        val flowMock = flowWithException(exception)
        val publisher = flowMock.asFlowPublisher()

        val values = ArrayList<Int>()
        var isCompleted = false
        var error: Throwable? = null
        publisher.subscribe(CancellableManager(),
            onNext = { values.add(it) },
            onError = { error = it },
            onCompleted = { isCompleted = true }
        )
        assertEquals(listOf(0, 1), values)
        assertFalse { isCompleted }
        assertEquals(exception, error)
    }

    @Test
    fun cancelledFlowPublisherTest() = runBlockingTest {
        var exception: Exception? = null
        val flowMock = flowWithDelay(1000) { exception = it }
        val publisher = flowMock.asFlowPublisher()

        val values = ArrayList<Int>()
        var isCompleted = false
        var error: Throwable? = null
        val cancellableManager = CancellableManager()
        publisher.subscribe(cancellableManager,
            onNext = { values.add(it) },
            onError = { error = it },
            onCompleted = { isCompleted = true }
        )
        cancellableManager.cancel()
        assertTrue { exception is CancellationException }
        assertEquals(emptyList<Int>(), values)
        assertFalse { isCompleted }
        assertNull(error)
    }

    private fun flowMock(): Flow<Int> = flow {
        emit(0)
        emit(1)
    }

    private fun flowWithException(exception: IllegalStateException): Flow<Int> = flow {
        emit(0)
        emit(1)
        throw exception
    }

    private fun flowWithDelay(delayMs: Long = 1000, callback: (Exception) -> Unit): Flow<Int> = flow {
        try {
            delay(delayMs)
            emit(0)
            emit(1)
        } catch (e: CancellationException) {
            callback(e)
        }
    }
}
