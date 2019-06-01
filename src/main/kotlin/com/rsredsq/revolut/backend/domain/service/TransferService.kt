package com.rsredsq.revolut.backend.domain.service

import com.rsredsq.revolut.backend.domain.Transfer
import com.rsredsq.revolut.backend.domain.repository.AccountId
import com.rsredsq.revolut.backend.domain.repository.TransferRepository
import com.rsredsq.revolut.backend.kodein
import org.kodein.di.generic.instance

class TransferService {
  private val transferRepository by kodein.instance<TransferRepository>()

  fun performTransfer(from: AccountId, to: AccountId, amount: Long): Int =
    transferRepository.performTransfer(from, to, amount)

  fun listTransfers(id: AccountId): List<Transfer> =
    transferRepository.listTransfers(id)
}