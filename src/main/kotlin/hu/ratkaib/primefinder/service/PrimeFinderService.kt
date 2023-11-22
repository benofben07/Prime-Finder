package hu.ratkaib.primefinder.service

import hu.ratkaib.primefinder.interfaces.PrimeFinder
import hu.ratkaib.primefinder.model.PrimeNumber
import hu.ratkaib.primefinder.service.validation.PrimeFinderValidator
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
        val searcherJobs = getJobs()
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

    private suspend fun search(startingNumber: Long, incrementBy: Long) {
        var i = startingNumber
        while (true) {
            yield()
            if (isNumberPrime(i)) {
                repository.save(PrimeNumber(i))
            }

            i += incrementBy * 2
        }
    }

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

    private fun getJobs(): List<Job> = coroutineScope.coroutineContext.job.children.toList()
}