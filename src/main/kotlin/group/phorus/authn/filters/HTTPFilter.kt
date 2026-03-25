package group.phorus.authn.filters

import group.phorus.authn.core.context.HTTPContext
import group.phorus.authn.core.dtos.HTTPContextData
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.withContext
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.http.HttpHeaders
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange
import java.time.Instant

/**
 * WebFilter that captures HTTP request metadata and populates the [HTTPContext] coroutine
 * context element with an [HTTPContextData] snapshot.
 *
 * The snapshot includes the request path, HTTP method, headers, query parameters,
 * remote address, timestamp, content type, user agent, and origin header. Downstream
 * handlers and services can access this data via [HTTPContext].
 *
 * This filter runs on every request and does not perform any authentication or authorization.
 *
 * @see HTTPContext
 * @see HTTPContextData
 */
@AutoConfiguration
class HTTPFilter : CoWebFilter() {
    override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
        val request = exchange.request

        val contextData = HTTPContextData(
            path = request.path.value(),
            method = request.method.name(),
            headers = buildMap { request.headers.forEach { key, values -> put(key.lowercase(), values) } },
            queryParams = request.queryParams.toMap(),
            remoteAddress = request.remoteAddress?.address?.hostAddress,
            timestamp = Instant.now(),
            contentType = request.headers.contentType?.toString(),
            userAgent = request.headers.getFirst(HttpHeaders.USER_AGENT),
            origin = request.headers.getFirst(HttpHeaders.ORIGIN)
        )

        return withContext(HTTPContext.context.asContextElement(value = contextData)) {
            chain.filter(exchange)
        }
    }
}
