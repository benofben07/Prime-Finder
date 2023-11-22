package hu.ratkaib.primefinder.service

import hu.ratkaib.primefinder.model.PrimeNumber
import hu.ratkaib.primefinder.service.repository.PrimeFinderRepository
import hu.ratkaib.primefinder.service.validation.PrimeFinderValidator
import hu.ratkaib.primefinder.util.getJobs
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@OptIn(ExperimentalCoroutinesApi::class)
class PrimeFinderServiceTest {

    private val primeFinderRepository: PrimeFinderRepository = mockk()
    private val primeFinderValidator: PrimeFinderValidator = mockk()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val primeFinderService = PrimeFinderService(testScope, primeFinderRepository, primeFinderValidator, 2)

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
     * Tests if jobs have started after calling [PrimeFinderService.startSearch] and they are active.
     */
    @Test
    fun testStart_CoroutineJobsStarted() {
        runTest {
            // GIVEN
            justRun { primeFinderValidator.validateBeforeSearch(any()) }
            every { primeFinderRepository.save(any()) } returns PrimeNumber()
            justRun { primeFinderRepository.saveAll(any() as List<PrimeNumber>) }
            justRun { primeFinderRepository.deleteAll() }

            // WHEN
            primeFinderService.startSearch(2)

            // THEN
            verify(exactly = 1) { primeFinderValidator.validateBeforeSearch(any()) }
            verify(exactly = 1) { primeFinderRepository.deleteAll() }
            verify(exactly = 1) { primeFinderRepository.save(any()) }

            val jobs = testScope.getJobs()
            assertEquals(2, jobs.size)

            val firstJob = jobs[0]
            val secondJob = jobs[1]

            assertThat(firstJob.toString().contains("prime-search"))
            assertThat(secondJob.toString().contains("prime-search"))
            assertTrue { firstJob.isActive }
            assertTrue { secondJob.isActive }

            jobs.forEach { it.cancel() }
        }
    }

    /**
     * Tests if job has stopped after calling [PrimeFinderService.stopSearch].
     */
    @Test
    fun testStop_CoroutineJobsStoppedV2() {
        // GIVEN
        var job: Job? = null
        justRun { primeFinderValidator.validateBeforeStoppingSearch() }
        runTest {
            job = Job(testScope.coroutineContext.job)
            assertTrue { job!!.isActive }

            // WHEN
            primeFinderService.stopSearch()
        }

        //THEN
        verify(exactly = 1) { primeFinderValidator.validateBeforeStoppingSearch() }
        assertNotNull(job)
        assertTrue { job!!.isCancelled }
    }

    /**
     * Tests if listing returns found prime numbers correctly after calling [PrimeFinderService.listPrimes].
     */
    @Test
    fun testListPrimes() {
        // GIVEN
        justRun { primeFinderValidator.validateBeforeListing(2, 10) }
        every { primeFinderRepository.findByNumberBetween(2, 10) } returns listOf(2L, 3L, 5L, 7L).map { PrimeNumber(it) }

        // WHEN
        val response = primeFinderService.listPrimes(2L, 10L)

        // THEN
        verify(exactly = 1) { primeFinderValidator.validateBeforeListing(any(), any()) }
        assertEquals(4, response.size)
        assertThat(response).hasSameElementsAs(listOf(2L, 3L, 5L, 7L))
    }
}