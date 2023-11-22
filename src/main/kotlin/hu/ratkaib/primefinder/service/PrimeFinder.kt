package hu.ratkaib.primefinder.service

import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import jakarta.validation.constraints.Min

/**
 * This interface declares methods to start or stop searching for prime numbers and to list the found numbers.
 */
interface PrimeFinder {

    /**
     * Starts searching prime numbers on given amount of threads.
     * Runs until [stopSearch] is called or applicaction stops.
     *
     * @param threadsForSearchCount amount of threads to use for searching.
     * @throws PrimeFinderException if the parameter is invalid or search is already in progress.
     */
    fun startSearch(@Min(1) threadsForSearchCount: Int)

    /**
     * Stops searching prime numbers.
     *
     * @throws PrimeFinderException if prime searching is not in progress.
     */
    fun stopSearch()

    /**
     * Lists prime numbers in given range. Can be called while searching is in progress of after it has stopped.
     *
     * @param minValue start of the listing interval (inclusive).
     * @param maxValue end of the listing interval (inclusive).
     * @throws PrimeFinderException if a parameter is invalid.
     */
    fun listPrimes(@Min(1) minValue: Long, @Min(1) maxValue: Long): List<Long>
}