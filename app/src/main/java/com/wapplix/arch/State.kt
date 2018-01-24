package com.wapplix.arch

sealed class State

object Success : State()
object Loading : State()
data class Error(val exception: Throwable, val retry: (() -> Unit)?) : State()