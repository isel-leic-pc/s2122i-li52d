package pc.li52d.sequences

fun <T> sequence( block: suspend  MySequenceScope<T>.() -> Unit) : MySequenceScope<T> {
    return MySequenceScope(block)
}


fun main() {

    val fibonacci : Sequence<Int> = sequence {
        yield(1) // first Fibonacci number
        var cur = 1
        var next = 1
        while (true) {
            yield(next) // next Fibonacci number
            val tmp = cur + next
            cur = next
            next = tmp
        }
    }
    println(fibonacci
            .take(20)
            .toList())

}