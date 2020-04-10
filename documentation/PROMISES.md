# Promises

## Context

Trikot's promises are highly inspired from [the Javascript's Promise](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise).

If you are not familiar with the concept of Promises, you should take a look at their great [documentation](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise).

## Description

A promise represents the eventual result of an **asynchronous operation**.

A promise must be in one of three states: **pending, fulfilled, or rejected**.

1. When **pending**, a promise:
    - may transition to either the fulfilled or rejected state.
2. When **fulfilled**, a promise:
    - must not transition to any other state.
    - must have a value, which must not change.
3. When **rejected**, a promise:
    - must not transition to any other state.
    - must have a reason, which must not change.

A promise is a **Publisher**. You can subscribe to its result and compose it as you wish in your streams. 

A promises is **eager** (executed at its creation). You do not need to subscribe to a Promise for its reactive chain to be executed.

The **number of subscribers** does not influence the behavior of the promise. It is running (and running once) regardless the number of subscribers (0..n).

A promise is **cancellable**. You can optionally provide a `CancellableManager` to your new promise using the ```Promise.from(publisher: Publisher<T>, cancellableManager: CancellableManager?)``` initializer.

## Creation

A promise is usually created from a `Publisher`, where only one of its published value will be use to fulfill the promise.

Promises can be created using the following initializers :

#### `Promise.from(publisher, cancellableManager?)`
Returns a new Promise instance that will resolve or reject depending on the provided publisher's state. The promise will not settle untill the upstream publisher notifies a value, an error or a completed signal.

When the upstream *publisher* :
1. emits no value, error or completed signal
    - The Promise is never settled and considered as "pending"
2. emits one or more value
    - The Promise is resolved with the **first** value it receives
3. emits an error
    - The Promise is rejected with that same error
4. emits a completed signal and did not emit any value or error
    - The Promise is rejected with a `EmptyPromiseException`

Using this initializer allows for a `CancellableManager` to be provided optionnally.

When the provided *cancellableManager* :
1. Is cancelled **before** the Promise's creation
    - The Promise is rejected with a `CancelledPromiseException`
2. Is cancelled **after** the Promise's creation, but **before** the upstream publisher has notified a state
    - The Promise is rejected with a `CancelledPromiseException`
3. Is cancelled **after** the Promise's creation, and **after** the upstream publisher has notified a state
    - The Promise is not affected and is settled as usual with the publisher's value or error.
4. Is `null`
    - The Promise is not affected and is settled as usual with the publisher's value or error.

#### `Promise.resolve(value)`
Returns a new Promise instance that is resolved with the given value. Shorthand for `Promise.from(Publishers.just(value))`.

#### `Promise.reject(throwable)`
Returns a new Promise instance that is rejected with the given throwable. Shorthand for `Promise.from(Publishers.error(value))`.

## Chaining 

Promises can be chained using the following operators :

#### `onSuccess(accept)`
Executes the provided block when the previous promise is resolved. The returned promise has the same untouched state as the original promise.

#### `onError()`

#### `onSuccessReturn()`

#### `onErrorReturn()`

#### `then()`

#### `thenReturn()`

#### `finally()`
