package com.rsredsq.revolut.backend.domain.repository

import com.rsredsq.revolut.backend.db
import com.rsredsq.revolut.backend.domain.Account
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.exceptions.EntityNotFoundException
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

typealias IntEntityID = EntityID<Int>

object Accounts : IntIdTable() {
  val balance = double("balance")
}

class AccountOrmEntity(id: IntEntityID) : IntEntity(id) {
  companion object : IntEntityClass<AccountOrmEntity>(Accounts)

  var balance by Accounts.balance

  fun toDomain() = Account(id.value, balance)
}

class AccountOrmRepository : AccountRepository {
  init {
    transaction(db) {
      SchemaUtils.create(Accounts)
    }
  }

  override fun findAll(): List<Account> =
    AccountOrmEntity.all().map { it.toDomain() }.toList()

  override fun findById(id: Int): Account? = AccountOrmEntity.findById(id)?.toDomain()

  override fun delete(id: Int){
    transaction(db) {
      AccountOrmEntity.findById(id)?.delete() ?: throw EntityNotFoundException(
        IntEntityID(
          id,
          Accounts
        ), AccountOrmEntity.Companion
      )
    }

  }

  override fun create(balance: Double): Account =
    AccountOrmEntity.new {
      this.balance = balance
    }.toDomain()
}