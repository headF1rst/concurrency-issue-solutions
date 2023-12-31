package com.example.stock.service

import com.example.stock.domain.Stock
import com.example.stock.repository.StockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class StockService(
    private val stockRepository: StockRepository,
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun decrease(id: Long, quantity: Long) {
        val stock: Stock = stockRepository.findById(id).orElseThrow{ IllegalArgumentException("Stock is Empty") }

        stock.decrease(quantity)

        stockRepository.saveAndFlush(stock)
    }
}
