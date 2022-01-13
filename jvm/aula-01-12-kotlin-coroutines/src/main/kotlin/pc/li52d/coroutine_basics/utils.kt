package pc.li52d.coroutine_basics

import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

val schedulledPool = Executors.newScheduledThreadPool(4);

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

suspend fun myDelay(millis: Long ) {
    suspendCoroutine<Unit> { cont  ->
        schedulledPool.schedule({cont.resume(Unit) }, millis, TimeUnit.MILLISECONDS)
    }
}