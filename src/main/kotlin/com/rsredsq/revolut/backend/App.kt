package com.rsredsq.revolut.backend

import com.rsredsq.revolut.backend.api.AccountController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.JavalinConfig
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info
import org.jetbrains.exposed.sql.Database

fun main() {
  Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

  val app = Javalin.create { configure(it) }.start(8080)
  router(app)
}

fun configure(config: JavalinConfig) = config.apply {
  enableWebjars()
  enableDevLogging()
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
      crud("accounts/:id", AccountController)
    }
  }
}