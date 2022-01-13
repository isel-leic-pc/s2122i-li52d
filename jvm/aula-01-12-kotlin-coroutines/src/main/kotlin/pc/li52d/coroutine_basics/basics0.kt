package pc.li52d.coroutine_basics

import kotlinx.coroutines.*
import mu.KotlinLogging

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

suspend fun sayHello() {

    delay(3000)
    println("Hello")
}

private fun main() {
    logger.info("begin main")
    val job : Job = GlobalScope.launch {
        logger.info("begin coroutine")
        sayHello()
        logger.info("end coroutine")
    }

    runBlocking {
        logger.info("start join")
        job.join()
        logger.info("end join")
    }

    logger.info("end main")
}