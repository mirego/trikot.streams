package com.mirego.trikot.streams.reactive

import com.mirego.trikot.streams.cancellable.CancellableManager
import com.mirego.trikot.foundation.concurrent.dispatchQueue.DispatchQueue
import com.mirego.trikot.streams.reactive.processors.MapProcessor
import com.mirego.trikot.streams.reactive.processors.MapProcessorBlock
import com.mirego.trikot.streams.reactive.processors.SwitchMapProcessor
import com.mirego.trikot.streams.reactive.processors.SwitchMapProcessorBlock
import com.mirego.trikot.streams.reactive.processors.ObserveOnProcessor
import com.mirego.trikot.streams.reactive.processors.SubscribeOnProcessor
import com.mirego.trikot.streams.reactive.processors.FirstProcessor
import com.mirego.trikot.streams.reactive.processors.WithCancellableManagerProcessor
import com.mirego.trikot.streams.reactive.processors.WithCancellableManagerProcessorResultType
import com.mirego.trikot.streams.reactive.processors.FilterProcessorBlock
import com.mirego.trikot.streams.reactive.processors.FilterProcessor
import com.mirego.trikot.streams.reactive.processors.SharedProcessor
import com.mirego.trikot.streams.reactive.processors.OnErrorReturnProcessor
import com.mirego.trikot.streams.reactive.processors.OnErrorReturnProcessorBlock
import com.mirego.trikot.streams.reactive.processors.DistinctUntilChangedProcessor
import com.mirego.trikot.streams.reactive.processors.WithPreviousValueProcessor
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

typealias SubscriptionBlock<T> = (T) -> Unit
typealias SubscriptionErrorBlock = (Throwable) -> Unit
typealias SubscriptionCompletedBlock = () -> Unit

fun <T> Publisher<T>.subscribe(cancellableManager: CancellableManager, onNext: SubscriptionBlock<T>) {
    subscribe(cancellableManager, onNext, null, null)
}

fun <T> Publisher<T>.subscribe(cancellableManager: CancellableManager, onNext: SubscriptionBlock<T>, onError: SubscriptionErrorBlock?) {
    subscribe(cancellableManager, onNext, onError, null)
}

fun <T> Publisher<T>.subscribe(
    cancellableManager: CancellableManager,
    onNext: SubscriptionBlock<T>,
    onError: SubscriptionErrorBlock?,
    onCompleted: SubscriptionCompletedBlock?
) {
    subscribe(SubscriberFromBlock(cancellableManager, onNext, onError, onCompleted))
}

fun <T, R> Publisher<T>.map(block: MapProcessorBlock<T, R>): Publisher<R> {
    return map(null, block)
}

fun <T, R> Publisher<T>.map(name: String? = null, block: MapProcessorBlock<T, R>): Publisher<R> {
    return MapProcessor(this, block, name)
}

fun <T, R> Publisher<T>.switchMap(name: String? = null, block: SwitchMapProcessorBlock<T, R>): Publisher<R> {
    return SwitchMapProcessor(this, block, name)
}

fun <T> Publisher<T>.observeOn(dispatcher: DispatchQueue): Publisher<T> {
    return observeOn(null, dispatcher)
}

fun <T> Publisher<T>.observeOn(name: String? = null, dispatcher: DispatchQueue): Publisher<T> {
    return ObserveOnProcessor(this, dispatcher, name)
}

fun <T> Publisher<T>.subscribeOn(dispatcher: DispatchQueue): Publisher<T> {
    return subscribeOn(null, dispatcher)
}

fun <T> Publisher<T>.subscribeOn(name: String? = null, dispatcher: DispatchQueue): Publisher<T> {
    return SubscribeOnProcessor(this, dispatcher, name)
}

fun <T> Publisher<T>.first(name: String? = null): Publisher<T> {
    return FirstProcessor(this, name)
}

fun <T> Publisher<T>.withCancellableManager(name: String? = null): Publisher<WithCancellableManagerProcessorResultType<T>> {
    return WithCancellableManagerProcessor(this, name)
}

fun <T> Publisher<T>.filter(name: String? = null, block: FilterProcessorBlock<T>): Publisher<T> {
    return FilterProcessor(this, block, name)
}

fun <T> Publisher<T>.shared(name: String? = null): Publisher<T> {
    return SharedProcessor(this, name)
}

fun <T> Publisher<T>.onErrorReturn(name: String? = null, block: OnErrorReturnProcessorBlock<T>): Publisher<T> {
    return OnErrorReturnProcessor(this, block, name)
}

fun <T> Publisher<T>.distinctUntilChanged(name: String? = null): Publisher<T> {
    return DistinctUntilChangedProcessor(this, name)
}

fun <T> Publisher<T>.withPreviousValue(name: String? = null): Publisher<Pair<T?, T>> {
    return WithPreviousValueProcessor(this, name)
}

fun <T> Publisher<T>.asMutable(): BehaviorSubject<T> {
    val referencePublisher = this
    return object : BehaviorSubject<T> {
        override var value: T?
            get() {
                throw IllegalArgumentException("Cannot use get value on publisher of type on ${referencePublisher::class}.")
            }
            set(_) {
                throw IllegalArgumentException("Cannot use set value on publisher of type on ${referencePublisher::class}.")
            }
        override var error: Throwable?
            get() {
                throw IllegalArgumentException("Cannot use get error on publisher of type on ${referencePublisher::class}.")
            }
            set(_) {
                throw IllegalArgumentException("Cannot use set error on publisher of type on ${referencePublisher::class}.")
            }

        override fun complete() {
            throw IllegalArgumentException("Cannot use complete on publisher of type on ${referencePublisher::class}.")
        }

        override fun subscribe(s: Subscriber<in T>) {
            referencePublisher.subscribe(s)
        }
    }
}

fun <T> T.asPublisher(): Publisher<T> {
    return Publishers.behaviorSubject(this)
}
