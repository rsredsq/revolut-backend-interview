package com.rsredsq.revolut.backend.domain.repository

import com.rsredsq.revolut.backend.domain.Transfer
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object Transfers : IntIdTable() {
  val from = reference("from", Accounts)
  val to = reference("to", Accounts)
  val amount = long("amount")
  val createDate = datetime("createDate")
}

class TransferOrmEntity(id: IntEntityID) : IntEntity(id) {
  companion object : IntEntityClass<TransferOrmEntity>(Transfers)

  var from by AccountOrmEntity referencedOn Transfers.from
  var to by AccountOrmEntity referencedOn Transfers.to
  var amount by Transfers.amount
  var createDate by Transfers.createDate

  fun toDomain(): Transfer = Transfer(createDate, from.id.value, to.id.value, amount)
}

class TransferOrmRepository : TransferRepository {
  init {
    transaction {
      SchemaUtils.create(Transfers)
    }
  }

  @Synchronized
  override fun performTransfer(fromId: Int, toId: Int, amount: Long) = transaction {
    val from = AccountOrmEntity[fromId]
    val to = AccountOrmEntity[toId]

    from.balance -= amount
    to.balance += amount

    val transfer = TransferOrmEntity.new {
      this.from = from
      this.to = to
      this.amount = amount
      this.createDate = DateTime.now()
    }

    transfer.id.value
  }

  override fun listTransfers(id: AccountId): List<Transfer> = transaction {
    TransferOrmEntity.find {
      (Transfers.from eq id) or (Transfers.to eq id)
    }.map { it.toDomain() }
  }
}