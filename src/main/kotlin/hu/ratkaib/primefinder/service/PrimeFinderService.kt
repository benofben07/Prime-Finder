package hu.ratkaib.primefinder.service

import hu.ratkaib.primefinder.interfaces.PrimeFinder
import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class PrimeFinderService(@Value("\${maxThreadsToUse}") private val maxThreadsToUse: Int = 1) : PrimeFinder {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val primeNumbers = ConcurrentHashMap.newKeySet<Long>()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    private var jobs: List<Job> = emptyList()

    override fun startSearch(threadsForSearchCount: Int) {
        validateBeforeSearch(threadsForSearchCount)
        resetSearch()
        primeNumbers.add(2L)
        jobs = (3L..Long.MAX_VALUE step 2).take(threadsForSearchCount).map {
            coroutineScope.launch {
                search(it, threadsForSearchCount.toLong())
            }
        }

        logger.info("Started searching prime numbers with $threadsForSearchCount coroutines.")
    }

    override fun stopSearch() {
        validateBeforeStoppingSearch()
        coroutineScope.launch {
            jobs.forEach {
                it.cancelAndJoin()
                logger.info("Prime number searching stopped.")
            }
        }
    }

    override fun listPrimes(minValue: Long, maxValue: Long): List<Long> {
        validateBeforeListing(minValue, maxValue)
        return primeNumbers.filter { it in minValue..maxValue }.sorted()
    }

    @PreDestroy
    fun onExit() {
        coroutineScope.cancel()
    }

    private fun validateBeforeSearch(threadsForSearchCount: Int) {
        if (threadsForSearchCount > maxThreadsToUse) {
            throw PrimeFinderException("Maximum threads for searching cannot be more than $maxThreadsToUse!")
        }

        if (jobs.any { it.isActive }) {
            throw PrimeFinderException("Searching is already running!")
        }
    }

    private fun resetSearch() {
        primeNumbers.clear()
        jobs = emptyList()
    }

    private suspend fun search(startingNumber: Long, incrementBy: Long) {
        var i = startingNumber
        while (true) {
            yield()
            if (isOddNumberPrime(i)) {
                primeNumbers.add(i)
            }

            i += incrementBy * 2
        }
    }

    private fun isOddNumberPrime(number: Long): Boolean {
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

    private fun validateBeforeStoppingSearch() {
        if (jobs.all { it.isCompleted }) {
            throw PrimeFinderException("Searching isn't in progress, thus cannot be stopped!")
        }
    }

    private fun validateBeforeListing(minValue: Long, maxValue: Long) {
        if (maxValue < minValue) {
            throw PrimeFinderException("Minimum value ($minValue) cannot be bigger than maximum value ($maxValue)!")
        }
    }
}