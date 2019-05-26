package com.rsredsq.revolut.backend.api

import com.rsredsq.revolut.backend.domain.Account
import com.rsredsq.revolut.backend.domain.service.AccountService
import com.rsredsq.revolut.backend.kodein
import io.javalin.apibuilder.CrudHandler
import io.javalin.http.Context
import io.javalin.http.MethodNotAllowedResponse
import io.javalin.http.NotFoundResponse
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody
import mu.KLogging
import org.eclipse.jetty.http.HttpStatus
import org.kodein.di.generic.instance

data class AccountDto(val id: Int, val balance: Double)

fun Account.dto() = AccountDto(id, balance)

data class AccountCreateRequest(val initialBalance: Double = 0.0)

object AccountController : KLogging(), CrudHandler {

  private val accountService by kodein.instance<AccountService>()

  @OpenApi(
    summary = "create new account",
    requestBodies = [
      OpenApiRequestBody(AccountCreateRequest::class)
    ]
  )
  override fun create(ctx: Context) {
    val createRequest = ctx.body<AccountCreateRequest>()

    val account = accountService.create(createRequest.initialBalance).dto()

    ctx.json(account)
    ctx.status(HttpStatus.CREATED_201)
  }

  override fun delete(ctx: Context, resourceId: String) {
    val id = ctx.pathParam<Int>("id").get()

    accountService.delete(id)
  }

  override fun getAll(ctx: Context) {
    val accounts = accountService.listAll().map { it.dto() }

    ctx.json(accounts)
  }

  override fun getOne(ctx: Context, resourceId: String) {
    val id = ctx.pathParam<Int>("id").get()

    val account = accountService.findById(id)?.dto() ?: throw NotFoundResponse()

    ctx.json(account)
  }

  override fun update(ctx: Context, resourceId: String) {
    throw MethodNotAllowedResponse(details = emptyMap())
  }


}