package com.mirego.trikot.streams.reactive.promise

import com.mirego.trikot.streams.cancellable.CancellableManager
import com.mirego.trikot.streams.reactive.BehaviorSubjectImpl
import com.mirego.trikot.streams.reactive.Publishers
import com.mirego.trikot.streams.reactive.first
import com.mirego.trikot.streams.reactive.promise.exception.EmptyPromiseException
import com.mirego.trikot.streams.reactive.subscribe
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

class Promise<T> internal constructor(
    upstream: Publisher<T>
) : Publisher<T> {

    private val result = BehaviorSubjectImpl<T>()
    private val cancellableManager = CancellableManager()

    init {
        upstream
            .first() // TODO Remove .first() once we introduce the Single Publisher
            .subscribe(cancellableManager,
                onNext = { value ->
                    result.value = value
                },
                onError = { error ->
                    result.error = error
                },
                onCompleted = {
                    if (result.value == null && result.error == null) {
                        result.error = EmptyPromiseException
                    } else {
                        result.complete()
                    }
                }
            )
    }

    override fun subscribe(s: Subscriber<in T>) {
        result.subscribe(s)
    }

    fun onSuccess(accept: (T) -> Unit): Promise<T> = then(
        onSuccess = { accept(it); resolve(it) },
        onError = ::reject
    )

    fun <R> onSuccessReturn(apply: (T) -> Promise<R>): Promise<R> = then(
        onSuccess = apply,
        onError = ::reject
    )

    fun onError(accept: (Throwable) -> Unit): Promise<T> = then(
        onSuccess = ::resolve,
        onError = { accept(it); reject(it) }
    )

    fun onErrorReturn(apply: (Throwable) -> Promise<T>): Promise<T> = then(
        onSuccess = ::resolve,
        onError = apply
    )

    fun finally(execute: () -> Unit): Publisher<T> = then(
        onSuccess = { execute(); resolve(it) },
        onError = { execute(); reject(it) }
    )

    fun <R> then(onSuccess: (T) -> Promise<R>, onError: (Throwable) -> Promise<R>): Promise<R> {
        val result = BehaviorSubjectImpl<R>()
        val cancellableManager = CancellableManager()

        subscribe(cancellableManager,
            onNext = { t ->
                onSuccess(t).subscribe(cancellableManager,
                    onNext = { r ->
                        result.value = r
                    },
                    onError = { error ->
                        result.error = error
                    },
                    onCompleted = {
                        result.complete()
                    }
                )
            },
            onError = { e ->
                onError(e).subscribe(cancellableManager,
                    onNext = { r ->
                        result.value = r
                    },
                    onError = { error ->
                        result.error = error
                    },
                    onCompleted = {
                        result.complete()
                    }
                )
            }
        )

        return Promise(result)
    }

    companion object {
        fun <T> from(single: Publisher<T>): Promise<T> = Promise(single)

        fun <T> resolve(value: T): Promise<T> = from(Publishers.just(value))

        fun <T> reject(throwable: Throwable): Promise<T> = from(Publishers.error(throwable))
    }
}
