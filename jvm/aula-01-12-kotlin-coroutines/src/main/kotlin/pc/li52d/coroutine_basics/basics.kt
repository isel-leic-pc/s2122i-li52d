package pc.li52d.coroutine_basics

import kotlinx.coroutines.*
import mu.KotlinLogging

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

suspend fun sayHello2( ) = coroutineScope {
    logger.info("start sayHello")
    launch(Dispatchers.IO) {
        logger.info("start sayHello in context")
        delay(2000)
        print("Hello")
    }
    logger.info("end sayHello")
}

private fun main() {
    runBlocking {
        sayHello2()
        println(", World!")
    }
}

