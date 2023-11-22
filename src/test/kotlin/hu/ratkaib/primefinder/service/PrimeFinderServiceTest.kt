package hu.ratkaib.primefinder.service


import hu.ratkaib.primefinder.interfaces.PrimeFinder
import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ExtendWith(SpringExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PrimeFinderServiceTest(
    @Autowired val primeFinder: PrimeFinder,
    @Autowired private val coroutineScope: CoroutineScope,
    @Autowired private val repository: PrimeFinderRepository
) {

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun testSearchStartedOnOneThread_ThenStop() {
        assertDoesNotThrow {
            primeFinder.startSearch(1)
        }
        val jobs = getJobs()
        assertEquals(1, jobs.size)
        assertThat(jobs[0].toString().contains("prime-search"))
        assertDoesNotThrow {
            primeFinder.stopSearch()
        }
    }

    @Test
    fun testSearchStartedOnTwoThreads_ThenStop() {
        assertDoesNotThrow {
            primeFinder.startSearch(2)
        }
        val jobs = getJobs()
        assertEquals(2, jobs.size)
        assertThat(jobs[0].toString().contains("prime-search"))
        assertThat(jobs[1].toString().contains("prime-search"))
        assertDoesNotThrow {
            primeFinder.stopSearch()
        }
    }

    @Test
    fun testSearchOnInterval_WhileSearchInProgress() {
        assertDoesNotThrow {
            primeFinder.startSearch(1)
        }

        val result = primeFinder.listPrimes(1, 10000)
        assertTrue { result.size >= 3 }
        assertThat(result, hasItems(2, 3, 5))
        assertDoesNotThrow {
            primeFinder.stopSearch()
        }
    }

    @Test
    fun testSearchOnInterval_AfterSearchIsDone() {
        runTest {
            assertDoesNotThrow {
                primeFinder.startSearch(1)
            }

            assertDoesNotThrow {
                delay(1L)
                primeFinder.stopSearch()
            }

            val result = primeFinder.listPrimes(1, 10000)
            assertTrue { result.size >= 3 }
            assertThat(result, hasItems(2, 3, 5))
        }
    }

    @Test
    fun testStartValidationFails_InvalidParameter() {
        assertThrows<PrimeFinderException> {
            primeFinder.startSearch(999999)
        }
    }

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

    @Test
    fun testStopValidationFails_SearchIsNotRunning() {
        assertThrows<PrimeFinderException> {
            primeFinder.stopSearch()
        }
    }

    @Test
    fun testListingValidationFails_InvalidParameter() {
        assertThrows<PrimeFinderException> {
            primeFinder.listPrimes(10, 1)
        }
    }

    private fun getJobs(): List<Job> = coroutineScope.coroutineContext.job.children.toList()
}