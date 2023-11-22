package hu.ratkaib.primefinder.service.validation

import hu.ratkaib.primefinder.model.PrimeNumber
import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import hu.ratkaib.primefinder.service.repository.PrimeFinderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Contains validations to use before starting, stopping prime number search, and listing.
 */
@Component
class PrimeFinderValidator(
    private val coroutineScope: CoroutineScope,
    private val repository: PrimeFinderRepository,
    @Value("\${maxThreadsToUse}") private val maxThreadsToUse: Int = 1
) {

    /**
     * @throws PrimeFinderException if [threadsForSearchCount] is more than the amount specified in the property [maxThreadsToUse]
     * @throws PrimeFinderException if there is an active job running in [coroutineScope]
     */
    fun validateBeforeSearch(threadsForSearchCount: Int) {
        if (threadsForSearchCount > maxThreadsToUse) {
            throw PrimeFinderException("Maximum threads for searching cannot be more than $maxThreadsToUse!")
        }

        if (getJobs().any { it.isActive }) {
            throw PrimeFinderException("Searching is already running!")
        }
    }


    /**
     * @throws PrimeFinderException running jobs [coroutineScope] are completed already.
     */
    fun validateBeforeStoppingSearch() {
        if (getJobs().all { it.isCompleted }) {
            throw PrimeFinderException("Searching isn't in progress, thus cannot be stopped!")
        }
    }

    /**
     * @throws PrimeFinderException if [minValue] is greater than [maxValue]
     * @throws PrimeFinderException if searching hasn't progressed up to [maxValue] yet
     */
    fun validateBeforeListing(minValue: Long, maxValue: Long) {
        if (maxValue < minValue) {
            throw PrimeFinderException("Minimum value ($minValue) cannot be bigger than maximum value ($maxValue)!")
        }

        val maxNumberFound = repository.findTopByOrderByNumberDesc().orElseGet { PrimeNumber(0L) }.number!!
        if (maxNumberFound < maxValue) {
            throw PrimeFinderException("Searching hasn't finished yet on interval ($minValue) - ($maxValue)!")
        }
    }

    private fun getJobs(): List<Job> = coroutineScope.coroutineContext.job.children.toList()
}