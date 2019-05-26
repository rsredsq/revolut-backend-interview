package com.rsredsq.revolut.backend.domain.service

import com.rsredsq.revolut.backend.domain.repository.TransferRepository
import com.rsredsq.revolut.backend.kodein
import org.kodein.di.generic.instance

class TransferService {
  private val transferRepository by kodein.instance<TransferRepository>()

  fun performTransfer(fromId: Int, toId: Int, amount: Double): Int =
    transferRepository.performTransfer(fromId, toId, amount)
}