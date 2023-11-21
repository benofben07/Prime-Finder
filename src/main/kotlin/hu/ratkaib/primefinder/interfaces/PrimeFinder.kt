package hu.ratkaib.primefinder.interfaces

import java.math.BigDecimal
import java.math.BigInteger

interface PrimeFinder {

     fun startSearch(threadsForSearchCount: Int)

     fun stopSearch()

     fun listPrimes(minValue: Long, maxValue: Long): List<Long>
}