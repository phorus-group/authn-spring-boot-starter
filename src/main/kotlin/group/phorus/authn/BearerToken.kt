package group.phorus.authn

import group.phorus.exception.core.Unauthorized

private const val SCHEME = "Bearer"

/**
 * Reads the token out of an `Authorization` header value holding the Bearer scheme.
 *
 * [RFC 6750 §2.1](https://datatracker.ietf.org/doc/html/rfc6750#section-2.1) gives the header value
 * as `"Bearer" 1*SP b64token`, so the scheme is anchored at the start and at least one space
 * separates it from the token. [RFC 9110 §11.1](https://datatracker.ietf.org/doc/html/rfc9110#section-11.1)
 * makes an authentication scheme case-insensitive, so `bearer` and `BEARER` are the same scheme.
 *
 * Both call sites in this library read the header through this function, and a consumer reading the
 * header itself has the same parse available.
 *
 * @param header The raw `Authorization` header value.
 * @return The token that follows the scheme.
 * @throws Unauthorized when the value does not carry the Bearer scheme, or carries it with no token.
 */
fun extractBearerToken(header: String): String {
    if (!header.regionMatches(0, SCHEME, 0, SCHEME.length, ignoreCase = true))
        throw Unauthorized("Bearer token not found")

    val afterScheme = header.substring(SCHEME.length)
    val token = afterScheme.trimStart(' ')

    if (token.length == afterScheme.length) throw Unauthorized("Bearer token not found")
    if (token.isEmpty()) throw Unauthorized("Invalid authorization header size")

    return token
}
