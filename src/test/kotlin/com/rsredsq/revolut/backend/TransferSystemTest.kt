package com.rsredsq.revolut.backend

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rsredsq.revolut.backend.api.TransferRequest
import com.rsredsq.revolut.backend.domain.repository.AccountRepository
import com.rsredsq.revolut.backend.domain.repository.Accounts
import com.rsredsq.revolut.backend.domain.repository.TransferRepository
import com.rsredsq.revolut.backend.domain.repository.Transfers
import io.javalin.Javalin
import kong.unirest.Empty
import kong.unirest.HttpResponse
import kong.unirest.JacksonObjectMapper
import kong.unirest.Unirest
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.http.HttpStatus
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.kodein.di.generic.instance
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

const val TRANSFERS_BASE_URL = "http://localhost:8080/api/transfers"

class TransferSystemTest {

  private val accountRepository by kodein.instance<AccountRepository>()
  private val transferRepository by kodein.instance<TransferRepository>()

  private lateinit var app: Javalin

  @BeforeAll
  fun beforeAll() {
    app = startJavalin()
    Unirest.config().apply {
      objectMapper = JacksonObjectMapper(jacksonObjectMapper())
      concurrency(200, 200)
    }
  }

  @BeforeEach
  fun beforeEach() {
    transaction {
      SchemaUtils.create(Transfers, Accounts)
    }
    accountRepository.create(0.0)
    accountRepository.create(228.0)
    accountRepository.create(10.0)
    accountRepository.create(0.0)
  }

  @AfterEach
  fun afterEach() {
    transaction {
      SchemaUtils.drop(Transfers, Accounts)
    }
  }

  @AfterAll
  fun afterAll() {
    app.stop()
    Unirest.shutDown()
  }

  @Test
  fun `simple transfer`() {
    val response = Unirest.post(TRANSFERS_BASE_URL)
      .body(TransferRequest(2, 1, 228.0))
      .asEmpty()

    assertThat(response.status).isEqualTo(HttpStatus.OK_200)

    assertThat(accountRepository.findById(1)?.balance).isEqualTo(228.0)
    assertThat(accountRepository.findById(2)?.balance).isEqualTo(0.0)
  }

  @Test
  fun `multithreaded transfer test`() {
    val threads = Executors.newFixedThreadPool(9)

    val futures = mutableListOf<CompletableFuture<HttpResponse<Empty>>>()

    val latch = CountDownLatch(1)
    val wait = CountDownLatch(9)
    repeat(9) {
      threads.submit {
        latch.await()
        val response1 = Unirest.post(TRANSFERS_BASE_URL)
          .body(TransferRequest(3, 4, 10.0))
          .asEmptyAsync()
        val response2 = Unirest.post(TRANSFERS_BASE_URL)
          .body(TransferRequest(4, 3, 10.0))
          .asEmptyAsync()
        synchronized(futures) {
          futures.add(response1)
          futures.add(response2)
        }
        wait.countDown()
      }
    }

    latch.countDown()

    wait.await()

    futures.join()

    assertThat(accountRepository.findById(4)?.balance).isEqualTo(10.0)
    assertThat(accountRepository.findById(3)?.balance).isEqualTo(0.0)
  }
}