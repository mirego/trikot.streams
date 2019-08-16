package com.mirego.trikot.streams.android.ktx

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.Observer
import org.reactivestreams.Publisher

typealias ObserveBlock<T> = (T) -> Unit

fun <T> Publisher<T>.observe(lifecycleOwner: LifecycleOwner, observeBlock: ObserveBlock<T>) {
    return asLiveData().observe(lifecycleOwner, observeBlock)
}

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, observeBlock: ObserveBlock<T>) {
    this.observe(lifecycleOwner, Observer { observeBlock(it) })
}

fun <T> Publisher<T>.asLiveData(): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this)
}
