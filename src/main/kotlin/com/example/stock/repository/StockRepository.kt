package com.example.stock.repository

import com.example.stock.domain.Stock
import org.springframework.data.jpa.repository.JpaRepository

interface StockRepository: JpaRepository<Stock, Long> {
}
