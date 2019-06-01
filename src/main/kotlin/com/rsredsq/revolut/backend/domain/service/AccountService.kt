package com.rsredsq.revolut.backend.domain.service

import com.rsredsq.revolut.backend.domain.Account
import com.rsredsq.revolut.backend.domain.repository.AccountRepository
import com.rsredsq.revolut.backend.kodein
import org.kodein.di.generic.instance

class AccountService {

  private val accountRepository by kodein.instance<AccountRepository>()

  fun create(initialBalance: Long = 0): Account =
    accountRepository.create(initialBalance)

  fun listAll(): List<Account> =
    accountRepository.findAll()

  fun findById(id: Int): Account? = accountRepository.findById(id)

  fun delete(id: Int) {
    accountRepository.delete(id)
  }
}