package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.streams.cancellable.CancellableManager
import com.mirego.trikot.streams.reactive.MockPublisher
import com.mirego.trikot.streams.reactive.StreamsTimeoutException
import com.mirego.trikot.streams.reactive.subscribe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.seconds

class TimeoutProcessorTests {
    val mockTimer = MockTimer()
    val mockTimerFactory = MockTimerFactory(mockTimer)
    val publisher = MockPublisher()

    val timeoutProcessor = TimeoutProcessor(2.seconds, mockTimerFactory, "", publisher)

    @Test
    fun givenSubscribedProcessorWhenNoValueIsEmittedAndTimerExpireThenErrorIsThrown() {
        var receivedResult: String? = null
        var receivedError: Throwable? = null

        timeoutProcessor.subscribe(CancellableManager(),
            onNext = {
                receivedResult = it
            },
            onError = {
                receivedError = it
            }
        )

        mockTimer.executeBlock()

        assertFalse { publisher.getHasSubscriptions }
        assertNull(receivedResult)
        assertTrue { receivedError is StreamsTimeoutException }
        assertEquals(1, mockTimerFactory.singleCall)
    }

    @Test
    fun givenSubscribedProcessorWhenValueIsEmittedThenTimerIsCancelled() {
        val expectedValue = "ExpectedValue"
        var receivedResult: String? = null
        var receivedError: Throwable? = null

        timeoutProcessor.subscribe(CancellableManager(),
            onNext = {
                receivedResult = it
            },
            onError = {
                receivedError = it
            }
        )

        publisher.value = expectedValue

        assertTrue { publisher.getHasSubscriptions }
        assertNull(receivedError)
        assertEquals(expectedValue, receivedResult)
        assertEquals(1, mockTimerFactory.singleCall)
    }
}
