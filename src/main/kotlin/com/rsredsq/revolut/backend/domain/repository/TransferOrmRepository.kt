package com.rsredsq.revolut.backend.domain.repository

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

object Transfers : IntIdTable() {
  val from = reference("from", Accounts)
  val to = reference("to", Accounts)
  val amount = long("amount")
}

class TransferOrmEntity(id: IntEntityID) : IntEntity(id) {
  companion object : IntEntityClass<TransferOrmEntity>(Transfers)

  var from by AccountOrmEntity referencedOn Transfers.from
  var to by AccountOrmEntity referencedOn Transfers.to
  var amount by Transfers.amount
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
    }

    transfer.id.value
  }
}