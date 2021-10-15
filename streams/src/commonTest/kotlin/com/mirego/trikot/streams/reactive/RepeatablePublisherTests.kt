package com.mirego.trikot.streams.reactive

import com.mirego.trikot.foundation.concurrent.AtomicReference
import com.mirego.trikot.foundation.concurrent.setOrThrow
import com.mirego.trikot.streams.cancellable.CancellableManager
import com.mirego.trikot.streams.utils.MockTimer
import com.mirego.trikot.streams.utils.MockTimerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration

class RepeatablePublisherTests {
    @Test
    fun blockIsExecutedOnSubscription() {
        var executed = false

        val repeatable = Publishers.repeat(Duration.minutes(1)) {
            executed = true
            Publishers.behaviorSubject<String>()
        }
        repeatable.subscribe(CancellableManager()) {}
        kotlin.test.assertTrue { executed }
    }

    @Test
    fun blockIsReexecutedIfResubscribed() {
        var executionCount = AtomicReference(0)
        val repeatable = Publishers.repeat(Duration.minutes(1)) {
            executionCount.setOrThrow(executionCount.value + 1)
            Publishers.behaviorSubject<String>()
        }
        val cancellableManager = CancellableManager()

        repeatable.subscribe(cancellableManager) {}
        cancellableManager.cancel()
        repeatable.subscribe(CancellableManager()) {}

        assertEquals(2, executionCount.value)
    }

    @Test
    fun blockIsReExecutedWhenRepeated() {
        var executions = AtomicReference(0)
        var timer: MockTimer? = null
        val timerFactory = MockTimerFactory { _, duration ->
            assertEquals(Duration.minutes(1), duration)
            MockTimer().also { timer = it }
        }
        val repeatable = Publishers.repeat(Duration.minutes(1), timerFactory) {
            executions.setOrThrow(executions.value + 1)
            Publishers.behaviorSubject<String>()
        }

        repeatable.subscribe(CancellableManager()) {}
        timer?.executeBlock()

        assertEquals(2, executions.value)
    }
}
