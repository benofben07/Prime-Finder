package hu.ratkaib.primefinder.interfaces

import jakarta.validation.constraints.Min

interface PrimeFinder {

    fun startSearch(@Min(1) threadsForSearchCount: Int)

    fun stopSearch()

    fun listPrimes(@Min(1) minValue: Long, @Min(1) maxValue: Long): List<Long>
}