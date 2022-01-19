package pc.li52d.web


import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.sf.image4j.codec.ico.ICODecoder
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest


import java.net.http.HttpResponse.BodyHandlers


import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}


suspend fun <T> CompletableFuture<T>.value() : T {

    return suspendCoroutine<T> { cont : Continuation<T> ->
        this.whenComplete { t, e ->
            /*
            if (e == null)
                cont.resume(t)
            else
                cont.resumeWithException(e)

             */
            cont.resumeWithException(IllegalCallerException("Test exception!"))
        }
    }

}

fun  getAsync(url:String): CompletableFuture<String> {
    val client: HttpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build()

    val request: HttpRequest = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .GET()
        .build()

   return client.sendAsync(request, BodyHandlers.ofString() )
             .thenApply { resp ->
                val str = resp.body()
                str
            }
}

fun  getIconAsync(url:String): CompletableFuture<BufferedImage> {
    val client: HttpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build()

    val request: HttpRequest = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .GET()
        .build()

    return client.sendAsync(request, BodyHandlers.ofByteArray( ) )
           .thenApply { resp ->
                logger.info("future continuation")
                val input : ByteArray = resp.body()
                var img : BufferedImage?

                val  images = ICODecoder.read(ByteArrayInputStream(input))
                img = images.get(images.size -1)

                img!!
           }
}



suspend fun getIcon(url:String, d : Long) : BufferedImage {
    logger.info("start getImage")

    delay(d)
    val image = getIconAsync(url)
               .value()
    logger.info("end getImage")
    return image
}

