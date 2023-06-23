package com.example.stock.service

import com.example.stock.domain.Stock
import com.example.stock.repository.StockRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SpringBootTest
class StockServiceTest @Autowired constructor(
    private val stockService: PessimisticLockStockService,
    private val stockRepository: StockRepository,
) {

    @BeforeEach
    fun before() {
        val stock: Stock = Stock(1L, 100L)
        stockRepository.saveAndFlush(stock)
    }

    @AfterEach
    fun after() {
        stockRepository.deleteAll()
    }

    @Test
    fun stock_decrease() {
        stockService.decrease(1L, 1L)

        val stock = stockRepository.findById(1L).orElseThrow()

        assertThat(stock.quantity).isEqualTo(99)
    }

    @Test
    fun 동시에_100개의_요청() {
        val threadCount: Int = 100
        val executorService: ExecutorService = Executors.newFixedThreadPool(32)
        val latch: CountDownLatch = CountDownLatch(threadCount)

        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    stockService.decrease(1L, 1L)
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await()

        val stock = stockRepository.findById(1L).orElseThrow()

        assertThat(stock.quantity).isEqualTo(0L)
    }
}
