package io.github.jinnosukeKato.milib

interface Builder<T> {
    fun build(): T
}

fun <P, T : Builder<P>> T.buildWith(init: T.() -> Unit): P {
    this.init()
    return this.build()
}
