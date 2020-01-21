package com.mirego.trikot.streams.flow

import com.mirego.trikot.foundation.concurrent.AtomicReference
import kotlinx.coroutines.AbstractCoroutine
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.internal.ChannelFlow
import kotlinx.coroutines.flow.internal.SendingCollector
import kotlinx.coroutines.handleCoroutineException
import kotlinx.coroutines.intrinsics.startCoroutineCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Transforms the given flow to a reactive specification compliant [Publisher].
 *
 * This function is integrated with `ReactorContext` from `kotlinx-coroutines-reactor` module,
 * see its documentation for additional details.
 */
fun <T : Any> Flow<T>.asFlowPublisher(): Publisher<T> = FlowAsPublisher(this)

/**
 * Transforms the given reactive [Publisher] into [Flow].
 * Use [buffer] operator on the resulting flow to specify the size of the backpressure.
 * More precisely, it specifies the value of the subscription's [request][Subscription.request].
 * `1` is used by default.
 *
 * If any of the resulting flow transformations fails, subscription is immediately cancelled and all in-flights elements
 * are discarded.
 *
 * This function is integrated with `ReactorContext` from `kotlinx-coroutines-reactor` module,
 * see its documentation for additional details.
 */
fun <T : Any> Publisher<T>.asFlow(): Flow<T> = PublisherAsFlow(this, 1)

/**
 * Adapter that transforms [Flow] into TCK-complaint [Publisher].
 * [cancel] invocation cancels the original flow.
 */
private class FlowAsPublisher<T : Any>(private val flow: Flow<T>) : Publisher<T> {
    override fun subscribe(s: Subscriber<in T>) {
        val subscription = FlowSubscription(flow, s)
        s.onSubscribe(subscription)
        subscription.request(Long.MAX_VALUE)
    }
}

@UseExperimental(InternalCoroutinesApi::class)
class FlowSubscription<T>(
    val flow: Flow<T>,
    val subscriber: Subscriber<in T>
) : Subscription, AbstractCoroutine<Unit>(Dispatchers.Unconfined, false) {
    private val requested = AtomicReference(0L)
    private val producer = AtomicReference<CancellableContinuation<Unit>?>(null)

    override fun onStart() {
        ::flowProcessing.startCoroutineCancellable(this)
    }

    private suspend fun flowProcessing() {
        try {
            consumeFlow()
            subscriber.onComplete()
        } catch (e: Throwable) {
            try {
                if (e is CancellationException) {
                    subscriber.onComplete()
                } else {
                    subscriber.onError(e)
                }
            } catch (e: Throwable) {
                // Last ditch report
                handleCoroutineException(coroutineContext, e)
            }
        }
    }

    /*
     * This method has at most one caller at any time (triggered from the `request` method)
     */
    private suspend fun consumeFlow() {
        flow.collect { value ->
            /*
             * Flow is scopeless, thus if it's not active, its subscription was cancelled.
             * No intermediate "child failed, but flow coroutine is not" states are allowed.
             */
            coroutineContext.ensureActive()
            if (requested.value <= 0L) {
                suspendCancellableCoroutine<Unit> {
                    producer.setOrThrow(producer.value, it)
                    if (requested.value != 0L) it.resumeSafely()
                }
            }
            requested.value.also {
                requested.setOrThrow(requested.value, it - 1L)
            }

            subscriber.onNext(value)
        }
    }

    override fun cancel() {
        cancel(null)
    }

    override fun request(n: Long) {
        if (n <= 0) {
            return
        }
        start()
        requested.value.also { value ->
            val newValue = value + n
            requested.setOrThrow(value, if (newValue <= 0L) Long.MAX_VALUE else newValue)
        }
        producer.value.also { currentProducer ->
            producer.setOrThrow(currentProducer, null)
            currentProducer?.resumeSafely()
        }
    }

    private fun CancellableContinuation<Unit>.resumeSafely() {
        val token = tryResume(Unit)
        if (token != null) {
            completeResume(token)
        }
    }
}

@UseExperimental(InternalCoroutinesApi::class)
private class PublisherAsFlow<T : Any>(
    private val publisher: Publisher<T>,
    capacity: Int
) : ChannelFlow<T>(EmptyCoroutineContext, capacity) {
    override fun create(context: CoroutineContext, capacity: Int): ChannelFlow<T> =
        PublisherAsFlow(publisher, capacity)

    override fun produceImpl(scope: CoroutineScope): ReceiveChannel<T> {
        TODO("Not implemented")
    }

    private val requestSize: Long
        get() = when (capacity) {
            Channel.CONFLATED -> Long.MAX_VALUE // request all and conflate incoming
            Channel.RENDEZVOUS -> 1L // need to request at least one anyway
            Channel.UNLIMITED -> Long.MAX_VALUE // reactive streams way to say "give all" must be Long.MAX_VALUE
            else -> capacity.toLong().also { check(it >= 1) }
        }

    override suspend fun collect(collector: FlowCollector<T>) {
        val subscriber = ReactiveSubscriber<T>(capacity, requestSize)
        publisher.subscribe(subscriber)
        try {
            var consumed = 0L
            while (true) {
                val value = subscriber.takeNextOrNull() ?: break
                collector.emit(value)
                if (++consumed == requestSize) {
                    consumed = 0L
                    subscriber.makeRequest()
                }
            }
        } finally {
            subscriber.cancel()
        }
    }

    // The second channel here is used only for broadcast
    override suspend fun collectTo(scope: ProducerScope<T>) =
        collect(SendingCollector(scope.channel))
}

@Suppress("SubscriberImplementation")
@UseExperimental(InternalCoroutinesApi::class)
private class ReactiveSubscriber<T : Any>(
    capacity: Int,
    private val requestSize: Long
) : Subscriber<T> {
    private lateinit var subscription: Subscription
    private val channel = Channel<T>(capacity)

    suspend fun takeNextOrNull(): T? = channel.receiveOrClosed().valueOrNull

    override fun onNext(value: T) {
        // Controlled by requestSize
        require(channel.offer(value)) { "Element $value was not added to channel because it was full, $channel" }
    }

    override fun onComplete() {
        channel.close()
    }

    override fun onError(t: Throwable) {
        channel.close(t)
    }

    override fun onSubscribe(s: Subscription) {
        subscription = s
        makeRequest()
    }

    fun makeRequest() {
        subscription.request(requestSize)
    }

    fun cancel() {
        subscription.cancel()
    }
}
