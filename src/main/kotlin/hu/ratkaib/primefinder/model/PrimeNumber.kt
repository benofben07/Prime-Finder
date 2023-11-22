package hu.ratkaib.primefinder.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table


@Entity
@Table(name = "prime_numbers")
class PrimeNumber() {

    constructor(number: Long) : this() {
        this.number = number
    }

    @Id
    @Column(name = "prime_number")
    var number: Long? = null
}