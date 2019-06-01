package com.rsredsq.revolut.backend.api

import com.rsredsq.revolut.backend.domain.Transfer
import com.rsredsq.revolut.backend.domain.repository.AccountId
import com.rsredsq.revolut.backend.domain.service.TransferService
import com.rsredsq.revolut.backend.kodein
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiContent
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody
import io.javalin.plugin.openapi.annotations.OpenApiResponse
import org.eclipse.jetty.http.HttpStatus
import org.joda.time.DateTime
import org.kodein.di.generic.instance

data class TransferRequest(val from: AccountId, val to: AccountId, val amount: Long)

data class TransferDto(
  val createDate: DateTime,
  val to: AccountId,
  val from: AccountId,
  val amount: Long
)

fun Transfer.dto() = TransferDto(date, to, from, amount)

object TransferController {
  private val transferService by kodein.instance<TransferService>()

  @OpenApi(
    summary = "create new transfer",
    requestBody = OpenApiRequestBody([OpenApiContent(TransferRequest::class)])
  )

  fun create(ctx: Context) {
    val (from, to, amount) = ctx.body<TransferRequest>()

    transferService.performTransfer(from, to, amount)

    ctx.status(HttpStatus.OK_200)
  }

  @OpenApi(
    responses = [OpenApiResponse("200", [OpenApiContent(Array<TransferDto>::class)])]
  )
  fun listTransfers(ctx: Context) {
    val accountId = ctx.pathParam<AccountId>("id").get()

    val transfers = transferService.listTransfers(accountId).map { it.dto() }

    ctx.json(transfers)
  }
}