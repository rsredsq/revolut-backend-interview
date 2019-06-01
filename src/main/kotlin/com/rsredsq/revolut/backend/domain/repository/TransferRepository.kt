package com.rsredsq.revolut.backend.domain.repository

interface TransferRepository {
  fun performTransfer(fromId: Int, toId: Int, amount: Long): Int
}