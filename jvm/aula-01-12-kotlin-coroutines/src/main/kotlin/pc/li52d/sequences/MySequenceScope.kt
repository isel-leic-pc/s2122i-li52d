package pc.li52d.sequences

import mu.KotlinLogging
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.createCoroutineUnintercepted

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

class MySequenceScope<T>(val block : suspend MySequenceScope<T>.() -> Unit) :
    Sequence<T>, Iterator<T>, Continuation<Unit> {
    var done = false
    var cont  = block.createCoroutineUnintercepted(this, this)
    var nextValue : T? = null


    override fun iterator(): Iterator<T> {
        return this;
    }

    override fun hasNext(): Boolean {
        logger.info("start hasNext")
        if (done) return false
        if (nextValue != null) return true
        cont.resume(Unit)
        logger.info("end hasNext")
        return nextValue != null
    }

    override fun next(): T {
        if (nextValue == null) throw IllegalStateException()
        val value = nextValue
        nextValue = null
        return value!!
    }

    suspend fun yield(value: T)  {
        nextValue = value
        logger.info("start yield")
        suspendCoroutine<Unit> {  continuation ->
            cont = continuation
        }
        logger.info("end yield")
    }

    override val context: CoroutineContext
        get() =
            EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) {
        logger.info("all done")
        result.getOrThrow() // just rethrow exception if it is there
        done = true
    }
}