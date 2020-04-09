package com.mirego.trikot.streams.reactive.promise

import com.mirego.trikot.streams.reactive.Publishers
import com.mirego.trikot.streams.reactive.promise.exception.EmptyPromiseException
import com.mirego.trikot.streams.reactive.verify
import kotlin.test.Test

class PromiseInitializerTests {

    @Test
    fun testFromSingleValuePublisher() {
        val value = 22

        Promise.from(Publishers.just(value))
            .verify(
                value = value,
                error = null,
                completed = true
            )
    }

    @Test
    fun testFromErrorPublisher() {
        val throwable = Throwable()

        Promise.from<Int>(Publishers.error(throwable))
            .verify(
                value = null,
                error = throwable,
                completed = false
            )
    }

    @Test
    fun testFromEmptyPublisher() {
        Promise.from<Int>(Publishers.empty())
            .verify(
                value = null,
                error = EmptyPromiseException,
                completed = false
            )
    }

    @Test
    fun testFromNeverPublisher() {
        Promise.from<Int>(Publishers.never())
            .verify(
                value = null,
                error = null,
                completed = false
            )
    }

    @Test
    fun testResolve() {
        val value = 22

        Promise.resolve(22)
            .verify(
                value = value,
                error = null,
                completed = true
            )
    }

    @Test
    fun testReject() {
        val throwable = Throwable()

        Promise.reject<String>(throwable)
            .verify(
                value = null,
                error = throwable,
                completed = false
            )
    }
}
