package pc.li52d.coroutine_basics


import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.*
import kotlin.coroutines.suspendCoroutine

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

fun resumeDelayed(millis: Long, cont : Continuation<Unit>) {
    GlobalScope.launch {
        delay(millis)
        logger.info("continuation resume")
        cont.resume(Unit)
    }
}

suspend fun suspendFor(millis: Long) = suspendCoroutine<Unit> {
        cont ->
            resumeDelayed(millis, cont)

}

suspend fun suspendTest() {
    logger.debug("Before suspend test")
    suspendFor(5000)

    logger.info("After suspend test")

}


fun startAndForget(suspendingFunction: suspend () -> Unit) {
    val cont = object: Continuation<Unit> {
        override fun resumeWith(result :Result<Unit>) {
            // forget it
        }

        override val context: CoroutineContext
            get() = EmptyCoroutineContext
    }
    suspendingFunction.startCoroutine(cont)
}

fun  main() {

    runBlocking(Dispatchers.Unconfined){
        logger.info("start test")
        suspendTest()
        println("end test")
    }
    logger.info("done!")
}
