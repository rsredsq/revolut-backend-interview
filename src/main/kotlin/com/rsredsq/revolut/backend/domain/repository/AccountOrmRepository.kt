package com.rsredsq.revolut.backend.domain.repository

import com.rsredsq.revolut.backend.domain.Account
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.exceptions.EntityNotFoundException
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

typealias IntEntityID = EntityID<Int>

object Accounts : IntIdTable() {
  val balance = long("balance")
}

class AccountOrmEntity(id: IntEntityID) : IntEntity(id) {
  companion object : IntEntityClass<AccountOrmEntity>(Accounts)

  var balance by Accounts.balance

  fun toDomain() = Account(id.value, balance)
}

class AccountOrmRepository : AccountRepository {
  init {
    transaction {
      SchemaUtils.create(Accounts)
    }
  }

  override fun findAll(): List<Account> = transaction {
    AccountOrmEntity.all().map { it.toDomain() }.toList()
  }

  override fun findById(id: Int): Account? = transaction {
    AccountOrmEntity.findById(id)?.toDomain()
  }

  override fun delete(id: Int) {
    transaction {
      AccountOrmEntity.findById(id)?.delete() ?: throw EntityNotFoundException(
        IntEntityID(
          id,
          Accounts
        ), AccountOrmEntity.Companion
      )
    }
  }

  override fun deleteAll() {
    transaction {
      Accounts.deleteAll()
    }
  }

  override fun create(balance: Long): Account = transaction {
    AccountOrmEntity.new {
      this.balance = balance
    }.toDomain()
  }
}