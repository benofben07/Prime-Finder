package hu.ratkaib.primefinder.service.validation

import hu.ratkaib.primefinder.model.PrimeNumber
import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import hu.ratkaib.primefinder.service.repository.PrimeFinderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class PrimeFinderValidatorTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val primeFinderRepository: PrimeFinderRepository = mockk()

    private val primeFinderValidator = PrimeFinderValidator(testScope, primeFinderRepository, 2)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.scheduler.runCurrent()
        unmockkAll()
    }

    /**
     * Tests that calling [PrimeFinderValidator.validateBeforeSearch] with a valid parameter doesn't cause an exception.
     */
    @Test
    fun testValidateBeforeSearch() {
        assertDoesNotThrow { primeFinderValidator.validateBeforeSearch(2) }
    }

    /**
     * Tests that calling [PrimeFinderValidator.validateBeforeStoppingSearch] with valid state (a running job)
     * doesn't cause an exception.
     */
    @Test
    fun testValidateBeforeStop() {
        val job = createJob()
        assertDoesNotThrow { primeFinderValidator.validateBeforeStoppingSearch() }
        job.complete()
    }

    /**
     * Tests that calling [PrimeFinderValidator.validateBeforeListing] with valid parameters don't cause an exception.
     */
    @Test
    fun testValidateBeforeListing() {
        every { primeFinderRepository.findById(2L) } returns Optional.of(PrimeNumber(2))
        assertDoesNotThrow { primeFinderValidator.validateBeforeListing(1, 2) }
    }

    /**
     * Tests that calling [PrimeFinderValidator.validateBeforeSearch] with an invalid parameter
     * cause a [PrimeFinderException] exception.
     */
    @Test
    fun testValidateBeforeSearch_InvalidArgument() {
        assertFailsWith(
            exceptionClass = PrimeFinderException::class,
            message = "No exception thrown with invalid input parameter for search.",
            block = { primeFinderValidator.validateBeforeSearch(Integer.MAX_VALUE) }
        )
    }

    /**
     * Tests that calling [PrimeFinderValidator.validateBeforeListing] with min value greater than max value
     * cause a [PrimeFinderException] exception.
     */
    @Test
    fun testValidateBeforeListing_InvalidArgument_() {
        assertFailsWith(
            exceptionClass = PrimeFinderException::class,
            message = "No exception thrown with invalid input parameter for listing.",
            block = {
                primeFinderValidator.validateBeforeListing(10, 1)

            }
        )
    }

    /**
     * Tests that calling [PrimeFinderValidator.validateBeforeListing] with too big interval causes an exception.
     */
    @Test
    fun testValidateBeforeListing_InvalidArgument_TooBigInterval() {
        every { primeFinderRepository.findById(97L) } returns Optional.empty()
        assertFailsWith(
            exceptionClass = PrimeFinderException::class,
            message = "No exception thrown with invalid input parameter for listing.",
            block = {
                primeFinderValidator.validateBeforeListing(1, 100)

            }
        )
    }

    /**
     * Tests that calling [PrimeFinderValidator.validateBeforeSearch] with invalid state (a running job)
     * causes a [PrimeFinderException] exception.
     */
    @Test
    fun testValidateBeforeSearch_InvalidState_SearchAlreadyInProgress() {
        val job = createJob()
        assertFailsWith(
            exceptionClass = PrimeFinderException::class,
            message = "No exception thrown with invalid state for validation before search.",
            block = { primeFinderValidator.validateBeforeSearch(1) }
        )

        job.complete()
    }

    /**
     * Tests that calling [PrimeFinderValidator.validateBeforeStoppingSearch] with invalid state (no running jobs)
     * causes a [PrimeFinderException] exception.
     */
    @Test
    fun testValidateBeforeStop_InvalidState_NoSearchInProgress() {
        assertFailsWith(
            exceptionClass = PrimeFinderException::class,
            message = "No exception thrown with invalid state for validation before stopping search.",
            block = { primeFinderValidator.validateBeforeStoppingSearch() }
        )
    }

    private fun createJob() = Job(testScope.coroutineContext.job)
}