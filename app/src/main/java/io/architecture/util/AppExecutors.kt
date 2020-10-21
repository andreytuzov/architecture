package io.architecture.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors(
    val diskIO: Executor,
    val networkIO: Executor,
    val mainThread: Executor
) {

    companion object {

        private const val THREAD_COUNT = 3

        fun create() = AppExecutors(
            diskIO = DiskIOThreadExecutor(),
            networkIO = Executors.newFixedThreadPool(THREAD_COUNT),
            mainThread = MainThreadExecutor()
        )

        class MainThreadExecutor : Executor {

            private val handler = Handler(Looper.getMainLooper())

            override fun execute(command: Runnable) {
                handler.post(command)
            }

        }
    }
}