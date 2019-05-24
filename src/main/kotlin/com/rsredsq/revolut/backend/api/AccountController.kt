package com.rsredsq.revolut.backend.api

import com.rsredsq.revolut.backend.kodein
import com.rsredsq.revolut.backend.domain.service.AccountService
import io.javalin.apibuilder.CrudHandler
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody
import mu.KLogging
import org.kodein.di.generic.instance

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

    val account = accountService.create(createRequest.initialBalance)

    ctx.json(account)
  }

  override fun delete(ctx: Context, resourceId: String) {
    val id = ctx.pathParam<Int>("id").get()

    accountService.delete(id)
  }

  override fun getAll(ctx: Context) {
    val accounts = accountService.listAll()

    ctx.json(accounts)
  }

  override fun getOne(ctx: Context, resourceId: String) {
    val id = ctx.pathParam<Int>("id").get()

    val account = accountService.findById(id) ?: throw NotFoundResponse()

    ctx.json(account)
  }

  override fun update(ctx: Context, resourceId: String) {
    TODO("not implemented")
  }


}