@file:OptIn(DelicateCoroutinesApi::class)

package ru.lavafrai.maiapp.server.workbalancer

import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WorkBalancer {
    private val dispatcher = newFixedThreadPoolContext(16, "workBalancerDispatcher")
    private val tasksAccess = Mutex()
    private val scope = CoroutineScope(dispatcher)
    private val tasks = mutableMapOf<String, ListenableFuture<*>>()

    suspend fun <T>runTask(key: String, task: suspend CoroutineScope.() -> T): ListenableFuture<T> {
        tasksAccess.withLock {
            if (tasks.contains(key)) return tasks[key] as ListenableFuture<T>
            val future = scope.future(block = task)
            tasks[key] = future
            return future
        }
    }
}