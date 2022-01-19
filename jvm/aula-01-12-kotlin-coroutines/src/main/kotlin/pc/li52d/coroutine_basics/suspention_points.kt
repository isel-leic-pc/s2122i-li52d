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

suspend fun suspendFor(millis: Long) {
    logger.info("Before suspention point!")
        suspendCoroutine<Unit> {
            cont  ->
                resumeDelayed(millis, cont)

        }
    logger.info("After suspention point!")
}

suspend fun suspendTest() {
    logger.info("Before suspend test")
    suspendFor(5000)

    logger.info("After suspend test")

}


fun startAndForget(suspendingFunction: suspend () -> Unit) {
    val cont = object: Continuation<Unit> {
        override fun resumeWith(result :Result<Unit>) {
            // forget it
        }

        override val context: CoroutineContext
            get() = EmptyCoroutineContext + Dispatchers.Default
    }
    suspendingFunction.startCoroutine(cont)
}

private fun  main() {
    logger.info("start main")
    startAndForget {
        //println(currentCoroutineContext())
        logger.info("coroutine start!")
        delay(1000)
        logger.info("coroutine done!")
    }
    logger.info("after launch")

    Thread.sleep(3000)
    logger.info("end main")
}

private fun  main0() {

    runBlocking {
        logger.info("start test")
        suspendTest()
        println("end test")
    }
    logger.info("done!")
}
