package com.rsredsq.revolut.backend.domain.repository

import com.rsredsq.revolut.backend.domain.Transfer

interface TransferRepository {
  fun performTransfer(fromId: AccountId, toId: AccountId, amount: Long): Int
  fun listTransfers(id: AccountId): List<Transfer>
}