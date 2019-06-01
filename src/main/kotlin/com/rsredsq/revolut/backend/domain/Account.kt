package com.rsredsq.revolut.backend.domain

import com.rsredsq.revolut.backend.domain.repository.AccountId

data class Account(val id: AccountId, val balance: Long)