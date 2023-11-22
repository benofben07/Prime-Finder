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

    private val repository: PrimeFinderRepository = mockk()
    private val validator: PrimeFinderValidator = mockk()

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private val service = PrimeFinderService(testScope, repository, validator, 2)

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

    /**
     * Tests if jobs have started after calling [PrimeFinderService.startSearch] and they are active.
     */
    @Test
    fun testStart_CoroutineJobsStarted() {
        runTest {
            // GIVEN
            justRun { validator.validateBeforeSearch(any()) }
            every { repository.save(any()) } returns PrimeNumber()
            justRun { repository.saveAll(any() as List<PrimeNumber>) }
            justRun { repository.deleteAll() }

            // WHEN
            service.startSearch(2)

            // THEN
            verify(exactly = 1) { validator.validateBeforeSearch(any()) }
            verify(exactly = 1) { repository.deleteAll() }
            verify(exactly = 1) { repository.save(any()) }

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
        justRun { validator.validateBeforeStoppingSearch() }
        runTest {
            job = Job(testScope.coroutineContext.job)
            assertTrue { job!!.isActive }

            // WHEN
            service.stopSearch()
        }

        //THEN
        verify(exactly = 1) { validator.validateBeforeStoppingSearch() }
        assertNotNull(job)
        assertTrue { job!!.isCancelled }
    }

    /**
     * Tests if listing returns found prime numbers correctly after calling [PrimeFinderService.listPrimes].
     */
    @Test
    fun testListPrimes() {
        // GIVEN
        justRun { validator.validateBeforeListing(2, 10) }
        every { repository.findByNumberBetween(2, 10) } returns listOf(2L, 3L, 5L, 7L).map { PrimeNumber(it) }

        // WHEN
        val response = service.listPrimes(2L, 10L)

        // THEN
        verify(exactly = 1) { validator.validateBeforeListing(any(), any()) }
        assertEquals(4, response.size)
        assertThat(response).hasSameElementsAs(listOf(2L, 3L, 5L, 7L))
    }
}