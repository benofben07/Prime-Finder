package hu.ratkaib.primefinder.service.validation

import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PrimeFinderValidator(
    private val coroutineScope: CoroutineScope,
    @Value("\${maxThreadsToUse}") private val maxThreadsToUse: Int = 1
) {
    fun validateBeforeSearch(threadsForSearchCount: Int) {
        if (threadsForSearchCount > maxThreadsToUse) {
            throw PrimeFinderException("Maximum threads for searching cannot be more than $maxThreadsToUse!")
        }

        if (getJobs().any { it.isActive }) {
            throw PrimeFinderException("Searching is already running!")
        }
    }

    fun validateBeforeStoppingSearch() {
        if (getJobs().all { it.isCompleted }) {
            throw PrimeFinderException("Searching isn't in progress, thus cannot be stopped!")
        }
    }

    fun validateBeforeListing(minValue: Long, maxValue: Long) {
        if (maxValue < minValue) {
            throw PrimeFinderException("Minimum value ($minValue) cannot be bigger than maximum value ($maxValue)!")
        }
    }

    private fun getJobs(): List<Job> = coroutineScope.coroutineContext.job.children.toList()
}