package com.rsredsq.revolut.backend

import kong.unirest.HttpRequest
import kong.unirest.HttpResponse
import kong.unirest.RequestBodyEntity
import java.util.concurrent.CompletableFuture

inline fun <reified T> RequestBodyEntity.asObject(): HttpResponse<T> = asObject(T::class.java)

fun <T> List<CompletableFuture<T>>.join(){
  CompletableFuture.allOf(*this.toTypedArray()).join()
}

inline fun <reified T> HttpRequest<*>.asObject(): HttpResponse<T> =
  asObject(T::class.java)

inline fun <reified T> HttpRequest<*>.asObjectAsync(): CompletableFuture<HttpResponse<T>> =
  asObjectAsync(T::class.java)
