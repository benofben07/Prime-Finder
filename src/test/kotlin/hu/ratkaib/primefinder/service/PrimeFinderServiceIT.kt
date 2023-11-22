package hu.ratkaib.primefinder.service


import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import hu.ratkaib.primefinder.service.repository.PrimeFinderRepository
import hu.ratkaib.primefinder.service.validation.PrimeFinderValidator
import hu.ratkaib.primefinder.util.getJobs
import io.mockk.unmockkAll
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@SpringBootTest
@ExtendWith(SpringExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PrimeFinderServiceIT(
    @Autowired private val repository: PrimeFinderRepository,
) {

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private val validator = PrimeFinderValidator(testScope, repository, 2)
    private val primeFinder = PrimeFinderService(testScope, repository, validator, 2)

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        dispatcher.scheduler.runCurrent()
        unmockkAll()
    }

    /**
     * Tests if job has been created after calling [PrimeFinderService.startSearch] and is actively running.
     */
    @Test
    fun testSearchStartedOnOneThread_ThenStop() {
        assertDoesNotThrow {
            primeFinder.startSearch(1)
        }
        val jobs = testScope.getJobs()
        assertEquals(1, jobs.size)
        val job = jobs[0]

        assertThat(job.toString().contains("prime-search"))
        assertTrue { job.isActive }

        assertDoesNotThrow {
            primeFinder.stopSearch()
        }
    }

    /**
     * Tests if multiple jobs have been created after calling [PrimeFinderService.startSearch] and are actively running.
     */
    @Test
    fun testSearchStartedOnTwoThreads_ThenStop() {
        assertDoesNotThrow {
            primeFinder.startSearch(2)
        }
        val jobs = testScope.getJobs()
        assertEquals(2, jobs.size)

        val firstJob = jobs[0]
        val secondJob = jobs[1]

        assertThat(firstJob.toString().contains("prime-search"))
        assertThat(secondJob.toString().contains("prime-search"))
        assertTrue { firstJob.isActive }
        assertTrue { secondJob.isActive }

        assertDoesNotThrow {
            primeFinder.stopSearch()
        }
    }

    /**
     * Tests if [PrimeFinderService.listPrimes] listing is returning prime numbers
     * while [PrimeFinderService.startSearch] search is running.
     */
    @Test
    fun testListOnInterval_WhileSearchInProgress() {
        assertDoesNotThrow {
            primeFinder.startSearch(1)
        }

        val result = primeFinder.listPrimes(1, 2)
        assertTrue { result.isNotEmpty() }
        assertDoesNotThrow {
            primeFinder.stopSearch()
        }
    }

    /**
     * Tests if [PrimeFinderService.listPrimes] listing is returning prime numbers
     * after search was [PrimeFinderService.stopSearch] stopped.
     */
    @Test
    fun testListOnInterval_AfterSearchIsDone() {
        assertDoesNotThrow {
            primeFinder.startSearch(1)
        }

        assertDoesNotThrow {
            primeFinder.stopSearch()
        }

        val result = primeFinder.listPrimes(1, 2)
        assertTrue { result.isNotEmpty() }
    }

    /**
     * Tests that calling [PrimeFinderService.startSearch] with invalid parameters
     * results in a [PrimeFinderException] exception.
     */
    @Test
    fun testStartValidationFails_InvalidParameter() {
        assertThrows<PrimeFinderException> {
            primeFinder.startSearch(999999)
        }
    }

    /**
     * Tests that calling [PrimeFinderService.listPrimes] with invalid parameters
     * results in a [PrimeFinderException] exception.
     */
    @Test
    fun testListingValidationFails_InvalidParameter() {
        assertThrows<PrimeFinderException> {
            primeFinder.listPrimes(10, 1)
        }
    }

    /**
     * Tests that multiple calls on [PrimeFinderService.startSearch] results in a [PrimeFinderException] exception.
     */
    @Test
    fun testStartValidationFails_SearchIsAlreadyRunning() {
        assertDoesNotThrow {
            primeFinder.startSearch(1)
        }

        assertThrows<PrimeFinderException> {
            primeFinder.startSearch(1)
        }

        assertDoesNotThrow {
            primeFinder.stopSearch()
        }
    }

    /**
     * Tests that before calling [PrimeFinderService.startSearch], calling [PrimeFinderService.stopSearch]
     * results in a [PrimeFinderException] exception.
     */
    @Test
    fun testStopValidationFails_SearchIsNotRunning() {
        assertThrows<PrimeFinderException> {
            primeFinder.stopSearch()
        }
    }
}