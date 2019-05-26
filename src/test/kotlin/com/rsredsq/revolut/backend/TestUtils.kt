package com.rsredsq.revolut.backend

import kong.unirest.HttpResponse
import kong.unirest.RequestBodyEntity
import java.util.concurrent.CompletableFuture

inline fun <reified T> RequestBodyEntity.asObject(): HttpResponse<T> = asObject(T::class.java)

fun <T> List<CompletableFuture<T>>.join() =
  CompletableFuture.allOf(*this.toTypedArray()).join()
