package pc.li52d.coroutine_basics


import kotlinx.coroutines.*
import mu.KotlinLogging

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}


suspend fun runAll() = coroutineScope {
    logger.info("start runAll")
    for(i in 1..100) {
        launch {
            //println("coroutine $i ")
            delay(5000)
            logger.info("coroutine $i ")

        }

        //logger.info("index = $i  ")
    }
    logger.info("end runAll")
}

private fun main()  {
    val job : Job = GlobalScope.launch {
        logger.info("before runAll")
        runAll()
        logger.info("after runAll")
    }

    runBlocking {
        job.join()
    }
    logger.info("end main")
}