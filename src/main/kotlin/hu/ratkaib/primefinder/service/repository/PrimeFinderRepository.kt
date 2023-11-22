package hu.ratkaib.primefinder.service.repository

import hu.ratkaib.primefinder.model.PrimeNumber
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PrimeFinderRepository : JpaRepository<PrimeNumber, Long> {
    fun findByNumberBetween(min: Long, max: Long): List<PrimeNumber>
}