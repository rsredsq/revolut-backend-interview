package com.rsredsq.revolut.backend.api

import com.rsredsq.revolut.backend.service.AccountService
import io.javalin.apibuilder.CrudHandler
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody
import mu.KLogging

data class AccountCreateRequest(val initialBalance: Double = 0.0)

object AccountController : KLogging(), CrudHandler {

  @OpenApi(
    summary = "create new account",
    requestBodies = [
      OpenApiRequestBody(AccountCreateRequest::class)
    ]
  )
  override fun create(ctx: Context) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun delete(ctx: Context, resourceId: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getAll(ctx: Context) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getOne(ctx: Context, resourceId: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun update(ctx: Context, resourceId: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }


}