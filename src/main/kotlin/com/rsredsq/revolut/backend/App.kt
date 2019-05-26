package com.rsredsq.revolut.backend

import com.rsredsq.revolut.backend.api.AccountController
import com.rsredsq.revolut.backend.domain.repository.AccountOrmRepository
import com.rsredsq.revolut.backend.domain.repository.AccountRepository
import com.rsredsq.revolut.backend.domain.service.AccountService
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.crud
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.core.JavalinConfig
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.annotations.ContentType
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info
import org.eclipse.jetty.http.HttpStatus
import org.jetbrains.exposed.exceptions.EntityNotFoundException
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

val kodein = Kodein {
  bind<AccountService>() with singleton { AccountService() }
  bind<AccountRepository>() with singleton { AccountOrmRepository() }
}

val db = Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1", "org.h2.Driver")

fun main() {
  startJavalin()
}

fun startJavalin(): Javalin =
  Javalin
    .create { config -> initialConfig(config) }
    .start(8080)
    .also { app -> configure(app) }

fun initialConfig(config: JavalinConfig) = config.apply {
  defaultContentType = ContentType.JSON
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

fun configure(app: Javalin) {
  app.exception(EntityNotFoundException::class.java) { _, ctx ->
    ctx.status(HttpStatus.NOT_FOUND_404)
  }
  router(app)
}

fun router(app: Javalin) {
  app.routes {
    path("api") {
      crud("accounts/:id", AccountController)
    }
  }
}