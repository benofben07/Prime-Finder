package hu.ratkaib.primefinder.service.validation

import hu.ratkaib.primefinder.model.exception.PrimeFinderException
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
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class PrimeFinderValidatorTest {

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val validator = PrimeFinderValidator(testScope, 2)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        dispatcher.scheduler.runCurrent()
        unmockkAll()
    }

    @Test
    fun testValidateBeforeSearch() {
        assertDoesNotThrow { validator.validateBeforeSearch(2) }
    }

    @Test
    fun testValidateBeforeStop() {
        val job = createJob()
        assertDoesNotThrow { validator.validateBeforeStoppingSearch() }
        job.complete()
    }

    @Test
    fun testValidateBeforeListing() {
        assertDoesNotThrow { validator.validateBeforeListing(1, 2) }
    }

    @Test
    fun testValidateBeforeSearch_InvalidArgument() {
        assertFailsWith(
            exceptionClass = PrimeFinderException::class,
            message = "No exception thrown with invalid input parameter for search.",
            block = { validator.validateBeforeSearch(Integer.MAX_VALUE) }
        )
    }

    @Test
    fun testValidateBeforeSearch_InvalidState_SearchAlreadyInProgress() {
        val job = createJob()
        assertFailsWith(
            exceptionClass = PrimeFinderException::class,
            message = "No exception thrown with invalid state for validation before search.",
            block = { validator.validateBeforeSearch(1) }
        )

        job.complete()
    }


    @Test
    fun testValidateBeforeStop_InvalidState_NoSearchInProgress() {
        assertFailsWith(
            exceptionClass = PrimeFinderException::class,
            message = "No exception thrown with invalid state for validation before stopping search.",
            block = { validator.validateBeforeStoppingSearch() }
        )
    }

    @Test
    fun testValidateBeforeListing_InvalidArgument() {
        assertFailsWith(
            exceptionClass = PrimeFinderException::class,
            message = "No exception thrown with invalid input parameter for listing.",
            block = {
                validator.validateBeforeListing(10, 1)

            }
        )
    }

    private fun createJob() = Job(testScope.coroutineContext.job)
}