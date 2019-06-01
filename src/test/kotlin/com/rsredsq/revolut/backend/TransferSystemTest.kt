package com.rsredsq.revolut.backend

import com.rsredsq.revolut.backend.api.AccountDto
import com.rsredsq.revolut.backend.api.TransferDto
import com.rsredsq.revolut.backend.api.TransferRequest
import com.rsredsq.revolut.backend.domain.repository.AccountRepository
import com.rsredsq.revolut.backend.domain.repository.Accounts
import com.rsredsq.revolut.backend.domain.repository.TransferRepository
import com.rsredsq.revolut.backend.domain.repository.Transfers
import io.javalin.Javalin
import kong.unirest.*
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.http.HttpStatus
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.*
import org.kodein.di.generic.instance
import java.util.concurrent.CompletableFuture

const val TRANSFERS_BASE_URL = "http://localhost:8080/api/transfers"

class TransferSystemTest {

  private val accountRepository by kodein.instance<AccountRepository>()
  private val transferRepository by kodein.instance<TransferRepository>()

  companion object {
    private lateinit var app: Javalin
    @BeforeAll
    @JvmStatic
    fun beforeAll() {
      app = startJavalin()
      Unirest.config().apply {
        objectMapper = JacksonObjectMapper(globalObjectMapper)
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
    accountRepository.create(0)
    accountRepository.create(10)
  }

  @AfterEach
  fun afterEach() {
    transaction {
      SchemaUtils.drop(Transfers, Accounts)
    }
  }

  @Test
  fun `simple transfer`() {
    val response = Unirest.post(TRANSFERS_BASE_URL)
      .body(TransferRequest(2, 1, 10))
      .asEmpty()

    assertThat(response.status).isEqualTo(HttpStatus.OK_200)

    val acc1 = Unirest.get("$ACCOUNTS_BASE_URL/1").asObject<AccountDto>().body
    val acc2 = Unirest.get("$ACCOUNTS_BASE_URL/2").asObject<AccountDto>().body

    assertThat(acc1.balance).isEqualTo(10)
    assertThat(acc2.balance).isEqualTo(0)
  }

  @Test
  fun `multithreaded transfer test`() {
    val futures = mutableListOf<CompletableFuture<HttpResponse<Empty>>>()

    repeat(228) {
      val response1 = Unirest.post(TRANSFERS_BASE_URL)
        .body(TransferRequest(2, 1, 10))
        .asEmptyAsync()
      futures.add(response1)

      val response2 = Unirest.post(TRANSFERS_BASE_URL)
        .body(TransferRequest(1, 2, 10))
        .asEmptyAsync()
      futures.add(response2)
    }

    futures.join()

    val acc1 = Unirest.get("$ACCOUNTS_BASE_URL/1").asObject<AccountDto>().body
    val acc2 = Unirest.get("$ACCOUNTS_BASE_URL/2").asObject<AccountDto>().body

    assertThat(acc1.balance + acc2.balance).isEqualTo(10)
  }

  @Test
  fun `list transfer`() {
    val expectedTransfer = TransferDto(DateTime.now(), 1, 2, 10)

    val testTransfer = Unirest.post(TRANSFERS_BASE_URL)
      .body(TransferRequest(2, 1, 10))
      .asEmpty()

    require(testTransfer.status == HttpStatus.OK_200)

    val response1 = Unirest.get("${ACCOUNTS_BASE_URL}/1/transfers")
      .asObject(object : GenericType<List<TransferDto>>() {})

    assertThat(response1.status).isEqualTo(HttpStatus.OK_200)

    val transfer1 = response1.body

    assertThat(transfer1).hasSize(1)
    assertThat(transfer1[0]).isEqualToIgnoringGivenFields(expectedTransfer, "createDate")

    val response2 = Unirest.get("${ACCOUNTS_BASE_URL}/2/transfers")
      .asObject(object : GenericType<List<TransferDto>>() {})

    assertThat(response2.status).isEqualTo(HttpStatus.OK_200)

    val transfer2 = response2.body

    assertThat(transfer2).hasSize(1)
    assertThat(transfer2[0]).isEqualToIgnoringGivenFields(expectedTransfer, "createDate")
  }
}