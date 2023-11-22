package hu.ratkaib.primefinder.service

import hu.ratkaib.primefinder.model.PrimeNumber
import hu.ratkaib.primefinder.service.repository.PrimeFinderRepository
import hu.ratkaib.primefinder.service.validation.PrimeFinderValidator
import hu.ratkaib.primefinder.util.getJobs
import jakarta.annotation.PreDestroy
import jakarta.validation.constraints.Min
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
class PrimeFinderService(
    private val coroutineScope: CoroutineScope,
    private val repository: PrimeFinderRepository,
    private val validator: PrimeFinderValidator,
    @Value("\${maxThreadsToUse}") private val maxThreadsToUse: Int = 1
) : PrimeFinder {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Validates parameter then starts searching for prime numbers on given amount of coroutines.
     * Checks only odd numbers. Since 2 is the only even prime number, saving it manually at the start.
     * Each coroutine checks a sequence of odd numbers. The difference between two elements are determined by the coroutine count.
     * Each coroutine receives a starting number which is the next available one after and including 3 (first odd prime number).
     *
     *
     * After checking the observed number if it is prime,
     * the next one is observed in its sequence until [stopSearch] is called or the application exits.
     *
     * In case of 3 coroutines:
     * coroutine#1: 3 -> 9 -> 15 -> ...
     * coroutine#2: 5 -> 11 -> 17 -> ...
     * coroutine#3: 7 -> 13 -> 19 -> ...
     */
    override fun startSearch(@Min(1) threadsForSearchCount: Int) {
        validator.validateBeforeSearch(threadsForSearchCount)
        resetSearch()
        // 2 is a special even prime number, saving it separately
        repository.save(PrimeNumber(2L))
        var i = 1
        (3L..3 + (maxThreadsToUse * 2) step 2).take(threadsForSearchCount).map {
            coroutineScope.launch(CoroutineName("prime-search-${i++}")) {
                search(it, threadsForSearchCount.toLong())
            }
        }

        logger.info("Started searching prime numbers with $threadsForSearchCount coroutines.")
    }

    override fun stopSearch() {
        validator.validateBeforeStoppingSearch()
        val searcherJobs = coroutineScope.getJobs()
        coroutineScope.launch {
            searcherJobs.forEach {
                it.cancelAndJoin()
            }

            logger.info("Prime number searching stopped.")
        }

        logger.info("Stopping prime number searching.")
    }

    override fun listPrimes(@Min(1) minValue: Long, @Min(1) maxValue: Long): List<Long> {
        validator.validateBeforeListing(minValue, maxValue)
        return repository.findByNumberBetween(minValue, maxValue).map { it.number!! }
    }

    @PreDestroy
    fun onExit() {
        coroutineScope.cancel()
    }

    private fun resetSearch() {
        repository.deleteAll()
    }

    /**
     * Searches prime numbers in an infinite loop. If a prime number is found it is then saved.
     *
     * @param startingNumber number to start searching from.
     * @param incrementBy increment the currently observed number by this amount to get the next observable number.
     */
    private suspend fun search(startingNumber: Long, incrementBy: Long) {
        var observedNumber = startingNumber
        while (true) {
            yield()
            if (isNumberPrime(observedNumber)) {
                repository.save(PrimeNumber(observedNumber))
            }

            observedNumber += incrementBy * 2
        }
    }

    /**
     * Checks if a number is prime. Negative numbers are considered non prime and won't be checked.
     * 1 and 2 are handled as special cases, other numbers will only be checked if they have any odd divider
     * (between 3 and the observed number's half), since the only prime number that can have an even divider is 2.
     *
     * @return true if a number is prime, false otherwise.
     */
    private fun isNumberPrime(number: Long): Boolean {
        if (number <= 1L) {
            return false
        } else if (number == 2L) {
            return true
        }

        for (i in 3L..number / 2 step 2) {
            if (number % i == 0L) {
                return false
            }
        }

        return true
    }
}