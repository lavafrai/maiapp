package ru.lavafrai.maiapp.utils

fun <T> T.contextual(case: Boolean, block: T.() -> T): T {
    return if (case) this.block() else this
}

fun <T> T.contextual(case: T.(T) -> Boolean, block: T.() -> T): T {
    return if (this.case(this)) this.block() else this
}

fun <T> T.modify(block: T.() -> T): T {
    return this.block()
}