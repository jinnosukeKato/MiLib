package com.github.jinnosukeKato.milib

interface Builder<T> {
    fun build(): T
}

fun <P, T : Builder<P>> build(builder: T, init: T.() -> Unit): P {
    builder.init()
    return builder.build()
}
