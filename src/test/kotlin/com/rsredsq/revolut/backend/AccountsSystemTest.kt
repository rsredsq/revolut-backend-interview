package com.rsredsq.revolut.backend

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rsredsq.revolut.backend.api.AccountCreateRequest
import com.rsredsq.revolut.backend.api.AccountDto
import com.rsredsq.revolut.backend.api.dto
import com.rsredsq.revolut.backend.domain.repository.AccountRepository
import com.rsredsq.revolut.backend.domain.repository.Accounts
import com.rsredsq.revolut.backend.domain.repository.Transfers
import io.javalin.Javalin
import kong.unirest.GenericType
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
import kotlin.random.Random


const val ACCOUNTS_BASE_URL = "http://localhost:8080/api/accounts"

class AccountsSystemTest {
  private val accountRepository by kodein.instance<AccountRepository>()

  private lateinit var app: Javalin

  companion object {
    private lateinit var app: Javalin
    @BeforeAll
    @JvmStatic
    fun beforeAll() {
      app = startJavalin()
      Unirest.config().apply {
        objectMapper = JacksonObjectMapper(jacksonObjectMapper())
        concurrency(200, 200)
      }
    }

    @AfterAll
    @JvmStatic
    fun afterAll() {
      app.stop()
      Unirest.shutDown()
    }
  }

  @BeforeEach
  fun beforeEach() {
    transaction {
      SchemaUtils.create(Transfers, Accounts)
    }
  }

  @AfterEach
  fun afterEach() {
    transaction {
      SchemaUtils.drop(Transfers, Accounts)
    }
  }

  @Test
  fun `simple account creation`() {
    val accountResponse = Unirest.post(ACCOUNTS_BASE_URL)
      .body(AccountCreateRequest())
      .asObject<AccountDto>()

    assertThat(accountResponse.status).isEqualTo(HttpStatus.CREATED_201)

    val createdAccount = accountResponse.body

    val fetchedAccount = Unirest.get("$ACCOUNTS_BASE_URL/{id}")
      .routeParam("id", createdAccount.id.toString())
      .asObject<AccountDto>()

    assertThat(createdAccount).isEqualTo(fetchedAccount.body)
  }

  @Test
  fun `account creation with initial balance`() {
    val givenInitialBalance = 1488L

    val accountResponse = Unirest.post(ACCOUNTS_BASE_URL)
      .body(AccountCreateRequest(givenInitialBalance))
      .asObject<AccountDto>()

    val createdAccount = accountResponse.body

    assertThat(accountResponse.status).isEqualTo(HttpStatus.CREATED_201)
    assertThat(createdAccount.balance).isEqualTo(givenInitialBalance)

    val fetchedAccount = Unirest.get("$ACCOUNTS_BASE_URL/{id}")
      .routeParam("id", createdAccount.id.toString())
      .asObject<AccountDto>()

    assertThat(createdAccount).isEqualTo(fetchedAccount.body)
  }


  @Test
  fun `parallel accounts creation`() {
    val futureAndGivenBalance =
      mutableListOf<Pair<CompletableFuture<HttpResponse<AccountDto>>, Long>>()

    repeat(200) {
      val balance = Random.nextLong()
      val future = Unirest.post(ACCOUNTS_BASE_URL)
        .body(AccountCreateRequest(balance))
        .asObjectAsync<AccountDto>()

      futureAndGivenBalance.add(future to balance)
    }

    futureAndGivenBalance.map { it.first }.join()

    assertThat(futureAndGivenBalance).allMatch {
      val (future, balance) = it
      return@allMatch future.get().body.balance == balance
    }
  }

  @Test
  fun `account update is not allowed`() {
    val response = Unirest.patch("$ACCOUNTS_BASE_URL/{id}")
      .routeParam("id", "228")
      .asString()

    assertThat(response.status).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED_405)
  }

  @Test
  fun `get all accounts`() {
    val givenAccounts =
      listOf(accountRepository.create(228), accountRepository.create(1488)).map { it.dto() }

    val response = Unirest.get(ACCOUNTS_BASE_URL)
      .asObject(object : GenericType<List<AccountDto>>() {})

    assertThat(response.body).isEqualTo(givenAccounts)
  }

}