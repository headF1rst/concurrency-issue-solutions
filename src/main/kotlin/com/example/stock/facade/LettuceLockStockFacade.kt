package com.example.stock.facade

import com.example.stock.repository.RedisLockRepository
import com.example.stock.service.StockService
import org.springframework.stereotype.Service

@Service
class LettuceLockStockFacade(
    private val redisLockRepository: RedisLockRepository,
    private val stockService: StockService,
) {

    fun decrease(key: Long, quantity: Long) {
        while (!redisLockRepository.lock(key)) {
            Thread.sleep(100)
        }

        try {
            stockService.decrease(key, quantity)
        } finally {
            redisLockRepository.unlock(key)
        }
    }
}
