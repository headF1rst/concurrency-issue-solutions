package com.example.stock.facade

import com.example.stock.service.OptimisticLockStockService
import org.springframework.stereotype.Service

@Service
class OptimisticLockStockFacade(
    private val optimisticLockStockService: OptimisticLockStockService,
) {

    fun decrease(id: Long, quantity: Long) {
        while (true) {
            try {
                optimisticLockStockService.decrease(id, quantity)
                break
            } catch (e: Exception) {
                Thread.sleep(50)
            }
        }
    }
}
