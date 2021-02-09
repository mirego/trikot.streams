package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.streams.cancellable.CancellableManager
import com.mirego.trikot.streams.reactive.Publishers
import com.mirego.trikot.streams.reactive.sample
import com.mirego.trikot.streams.reactive.subscribe
import com.mirego.trikot.streams.utils.MockTimer
import com.mirego.trikot.streams.utils.MockTimerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.seconds

class SampleProcessorTests {
    private val timer = MockTimer()

    @Test
    fun givenSubscribeToPublisherWithValueWhenSampleThenValueIsDispatchedWhenIntervalIsReached() {
        val publisher = Publishers.behaviorSubject(1)
        val publishedValues = mutableListOf<Int>()

        publisher
            .sample(1.seconds, MockTimerFactory { _, _ -> timer })
            .subscribe(CancellableManager()) {
                publishedValues.add(it)
            }

        assertEquals(emptyList<Int>(), publishedValues)
        timer.executeBlock()
        assertEquals(listOf(1), publishedValues)
    }

    @Test
    fun givenAPublisherThatEmitsTwoValuesWithinAnIntervalWhenSampleThenOnlyLastValueIsDispatched() {
        val publisher = Publishers.behaviorSubject(1)
        val publishedValues = mutableListOf<Int>()

        publisher
            .sample(1.seconds, MockTimerFactory { _, _ -> timer })
            .subscribe(CancellableManager()) {
                publishedValues.add(it)
            }

        timer.executeBlock()
        publisher.value = 2
        publisher.value = 3
        timer.executeBlock()

        assertEquals(listOf(1, 3), publishedValues)
    }

    @Test
    fun givenAPublisherThatEmitsTheSameValueInTwoIntervalsWhenSampleThenTheSameValueIsDispatched() {
        val publisher = Publishers.behaviorSubject(1)
        val publishedValues = mutableListOf<Int>()

        publisher
            .sample(1.seconds, MockTimerFactory { _, _ -> timer })
            .subscribe(CancellableManager()) {
                publishedValues.add(it)
            }

        timer.executeBlock()
        publisher.value = 2
        timer.executeBlock()
        publisher.value = 2
        timer.executeBlock()

        assertEquals(listOf(1, 2, 2), publishedValues)
    }

    @Test
    fun givenAPublisherThatDoesNotEmitsWithinAnIntervalWhenSampleThenTheSameValueIsNotDispatched() {
        val publisher = Publishers.behaviorSubject(1)
        val publishedValues = mutableListOf<Int>()

        publisher
            .sample(1.seconds, MockTimerFactory { _, _ -> timer })
            .subscribe(CancellableManager()) {
                publishedValues.add(it)
            }

        timer.executeBlock()
        publisher.value = 2
        timer.executeBlock()
        timer.executeBlock()

        assertEquals(listOf(1, 2), publishedValues)
    }

    @Test
    fun givenAPublisherThatDoesNotEmitValueWithinAnIntervalAndASubscriptionIsMadeAfterWhenSampleThenItReceivesTheLastValue() {
        val publisher = Publishers.behaviorSubject(1)
        val secondSubscriptionPublishedValues = mutableListOf<Int>()

        val samplePublisher = publisher.sample(1.seconds, MockTimerFactory { _, _ -> timer })

        samplePublisher.subscribe(CancellableManager()) {}

        publisher.value = 2
        timer.executeBlock()

        samplePublisher.subscribe(CancellableManager()) {
            secondSubscriptionPublishedValues.add(it)
        }

        timer.executeBlock()
        assertEquals(listOf(2), secondSubscriptionPublishedValues)
    }

    @Test
    fun givenAPublisherThatEmitsAValueAndCompletesBeforeAnIntervalIsReachedThenTheLastValueIsDispatched() {
        val publisher = Publishers.behaviorSubject(1)
        val publishedValues = mutableListOf<Int>()

        publisher
            .sample(1.seconds, MockTimerFactory { _, _ -> timer })
            .subscribe(CancellableManager()) {
                publishedValues.add(it)
            }

        timer.executeBlock()
        publisher.value = 2
        publisher.complete()

        assertEquals(listOf(1, 2), publishedValues)
    }
}
