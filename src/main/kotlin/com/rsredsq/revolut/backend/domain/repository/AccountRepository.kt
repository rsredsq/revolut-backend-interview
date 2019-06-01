package com.rsredsq.revolut.backend.domain.repository

import com.rsredsq.revolut.backend.domain.Account

typealias AccountId = Int

interface AccountRepository {
  fun create(balance: Long = 0): Account
  fun findAll(): List<Account>
  fun findById(id: AccountId): Account?
  fun delete(id: AccountId)
}
