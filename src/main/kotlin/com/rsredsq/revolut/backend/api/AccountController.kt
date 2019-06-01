package com.rsredsq.revolut.backend.api

import com.rsredsq.revolut.backend.domain.Account
import com.rsredsq.revolut.backend.domain.repository.AccountId
import com.rsredsq.revolut.backend.domain.service.AccountService
import com.rsredsq.revolut.backend.kodein
import io.javalin.apibuilder.CrudHandler
import io.javalin.http.Context
import io.javalin.http.MethodNotAllowedResponse
import io.javalin.http.NotFoundResponse
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiContent
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody
import io.javalin.plugin.openapi.annotations.OpenApiResponse
import mu.KLogging
import org.eclipse.jetty.http.HttpStatus
import org.kodein.di.generic.instance

data class AccountDto(val id: AccountId, val balance: Long)

fun Account.dto() = AccountDto(id, balance)

data class AccountCreateRequest(val initialBalance: Long = 0)

object AccountController : KLogging(), CrudHandler {

  private val accountService by kodein.instance<AccountService>()

  @OpenApi(
    summary = "create new account",
    requestBody = OpenApiRequestBody([OpenApiContent(AccountCreateRequest::class)]),
    responses = [OpenApiResponse("200", [OpenApiContent(AccountDto::class)])]
  )
  override fun create(ctx: Context) {
    val createRequest = ctx.body<AccountCreateRequest>()

    val account = accountService.create(createRequest.initialBalance).dto()

    ctx.json(account)
    ctx.status(HttpStatus.CREATED_201)
  }

  override fun delete(ctx: Context, resourceId: String) {
    val id = ctx.pathParam<AccountId>("id").get()

    accountService.delete(id)
  }

  @OpenApi(
    responses = [OpenApiResponse("200", [OpenApiContent(Array<AccountDto>::class)])]
  )
  override fun getAll(ctx: Context) {
    val accounts = accountService.listAll().map { it.dto() }

    ctx.json(accounts)
  }

  @OpenApi(
    responses = [OpenApiResponse("200", [OpenApiContent(AccountDto::class)])]
  )
  override fun getOne(ctx: Context, resourceId: String) {
    val id = ctx.pathParam<AccountId>("id").get()

    val account = accountService.findById(id)?.dto() ?: throw NotFoundResponse()

    ctx.json(account)
  }

  override fun update(ctx: Context, resourceId: String) {
    throw MethodNotAllowedResponse(details = emptyMap())
  }


}