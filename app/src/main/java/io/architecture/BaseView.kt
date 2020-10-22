package io.architecture

interface BaseView<T> {
    fun setPresenter(presenter: T)
}