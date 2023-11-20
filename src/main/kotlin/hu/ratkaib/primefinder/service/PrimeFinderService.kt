package hu.ratkaib.primefinder.service

import hu.ratkaib.primefinder.interfaces.PrimeFinder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class PrimeFinderService : PrimeFinder {

    private val primeNumbers = ConcurrentHashMap.newKeySet<Long>()
    private lateinit var jobs: List<Job>

    override fun startSearch(threadsForSearchCount: Int) {
        primeNumbers.add(2L)
        jobs = (3L..Long.MAX_VALUE step 2).take(threadsForSearchCount).map {
            GlobalScope.launch {
                search(it, threadsForSearchCount.toLong())
            }
        }
    }

    override fun listPrimes(minValue: Long, maxValue: Long): List<Long> =
        primeNumbers.filter { it in minValue..maxValue }.sorted()

    override fun stopSearch() {
        jobs.forEach {
            it.cancel()
        }

        println("primes: ${listPrimes(0L, Long.MAX_VALUE)}")
    }

    private suspend fun search(startingNumber: Long, incrementBy: Long) {
        var i = startingNumber
        while (true) {
            if (isOddNumberPrime(i)) {
                primeNumbers.add(i)
            }

            i += incrementBy * 2
        }
    }

    private suspend fun isOddNumberPrime(number: Long): Boolean {
        if (number <= 1L) {
            return false
        }

        for (i in 2L..number / 2) {
            if (number % i == 0L) {
                return false
            }
        }

        return true
    }
}