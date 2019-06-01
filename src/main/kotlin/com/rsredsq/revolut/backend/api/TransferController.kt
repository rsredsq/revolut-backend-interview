package com.rsredsq.revolut.backend.api

import com.rsredsq.revolut.backend.domain.service.TransferService
import com.rsredsq.revolut.backend.kodein
import io.javalin.http.Context
import org.eclipse.jetty.http.HttpStatus
import org.kodein.di.generic.instance

data class TransferRequest(val from: Int, val to: Int, val amount: Long)

object TransferController {
  private val transferService by kodein.instance<TransferService>()

  fun create(ctx: Context) {
    val (from, to, amount) = ctx.body<TransferRequest>()

    transferService.performTransfer(from, to, amount)

    ctx.status(HttpStatus.OK_200)
  }
}