package com.rsredsq.revolut.backend.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Accounts : IntIdTable() {
  val balance = double("balance")
}

class AccountEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<AccountEntity>(Accounts)

  var balance by Accounts.balance
}