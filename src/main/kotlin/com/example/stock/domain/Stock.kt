package com.example.stock.domain

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Stock(
    private val productId: Long,
    private var quantity: Long,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
) {

    fun decrease(quantity: Long) {
        if (this.quantity - quantity < 0) {
            throw RuntimeException("foo")
        }
        this.quantity = this.quantity - quantity
    }

    fun getQuantity(): Long {
        return quantity
    }
}
