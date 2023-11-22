package hu.ratkaib.primefinder.service.validation

import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import hu.ratkaib.primefinder.service.repository.PrimeFinderRepository
import hu.ratkaib.primefinder.util.findClosestSmallerOrEqualPrime
import hu.ratkaib.primefinder.util.getJobs
import jakarta.validation.constraints.Min
import kotlinx.coroutines.CoroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

/**
 * Contains validations to use before starting, stopping prime number search, and listing.
 */
@Component
@Validated
class PrimeFinderValidator(
    private val coroutineScope: CoroutineScope,
    private val repository: PrimeFinderRepository,
    @Value("\${maxThreadsToUse}") private val maxThreadsToUse: Int = 1
) {

    /**
     * @throws PrimeFinderException if [threadsForSearchCount] is more than the amount specified in the property [maxThreadsToUse]
     * @throws PrimeFinderException if there is an active job running in [coroutineScope]
     */
    fun validateBeforeSearch(@Min(1) threadsForSearchCount: Int) {
        if (threadsForSearchCount > maxThreadsToUse) {
            throw PrimeFinderException("Maximum threads for searching cannot be more than $maxThreadsToUse!")
        }

        if (coroutineScope.getJobs().any { it.isActive }) {
            throw PrimeFinderException("Searching is already running!")
        }
    }


    /**
     * @throws PrimeFinderException running jobs [coroutineScope] are completed already.
     */
    fun validateBeforeStoppingSearch() {
        if (coroutineScope.getJobs().all { it.isCompleted }) {
            throw PrimeFinderException("Searching isn't in progress, thus cannot be stopped!")
        }
    }

    /**
     * @throws PrimeFinderException if [minValue] is greater than [maxValue]
     * @throws PrimeFinderException if searching hasn't progressed up to [maxValue] yet
     */
    fun validateBeforeListing(@Min(1) minValue: Long, @Min(1) maxValue: Long) {
        if (maxValue < minValue) {
            throw PrimeFinderException("Minimum value ($minValue) cannot be bigger than maximum value ($maxValue)!")
        }

        val closestSmallerOrEqualPrime = findClosestSmallerOrEqualPrime(maxValue)!!
        val optClosestPrime = repository.findById(closestSmallerOrEqualPrime)
        if (!optClosestPrime.isPresent) {
            throw PrimeFinderException("Searching hasn't finished yet on interval ($minValue) - ($maxValue)!")
        }
    }
}