package com.msgtodo.core

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Io {
    private val pool: ExecutorService = Executors.newSingleThreadExecutor { r ->
        Thread(r, "io-demo").apply { isDaemon = true }
    }

    fun <T> run(block: () -> T): T = pool.submit(block).get()
}
