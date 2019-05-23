package com.rsredsq.revolut.backend

import com.rsredsq.revolut.backend.api.AccountController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.JavalinConfig
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info

fun main() {
  val app = Javalin.create { configure(it) }.start(8080)
  router(app)
}

fun configure(config: JavalinConfig) = config.apply {
  enableWebjars()
  registerPlugin(
    OpenApiOptions(Info())
      .path("/openapi")
      .swagger(SwaggerOptions("/swagger").title("test"))
      .let { options ->
        OpenApiPlugin(options)
      }
  )
}

fun router(app: Javalin) {
  app.routes {
    path("api") {
      path("accounts") {
        post(AccountController::create)
        get(AccountController::allAccounts)
      }
    }
  }
}