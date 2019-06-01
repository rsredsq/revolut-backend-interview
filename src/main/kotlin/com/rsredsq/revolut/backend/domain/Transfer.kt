package com.rsredsq.revolut.backend.domain

import com.rsredsq.revolut.backend.domain.repository.AccountId
import org.joda.time.DateTime

data class Transfer(val date: DateTime, val from: AccountId, val to: AccountId, val amount: Long)