package hu.ratkaib.primefinder.controller

import com.ninjasquad.springmockk.MockkBean
import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import hu.ratkaib.primefinder.service.PrimeFinder
import io.mockk.every
import io.mockk.justRun
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@WebMvcTest
class PrimeFinderControllerIT(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var primeFinder: PrimeFinder

    /**
     * Tests that calling [PrimeFinder.startSearch] endpoint with a valid parameter
     * results in an OK response.
     */
    @Test
    fun testSearchStarted() {
        val threads = 1
        justRun { primeFinder.startSearch(threads) }

        mockMvc.perform(post("/api/start?threads=$threads"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("started")))
    }


    /**
     * Tests that calling [PrimeFinder.stopSearch] endpoint results in an OK response.
     */
    @Test
    fun testSearchStopped() {
        justRun { primeFinder.stopSearch() }

        mockMvc.perform(post("/api/stop"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("stopped")))
    }

    /**
     * Tests that calling [PrimeFinder.listPrimes] endpoint with valid parameters
     * result in an OK response.
     */
    @Test
    fun testListingContainsCorrectNumbers() {
        val (min, max) = 1L to 3L
        every { primeFinder.listPrimes(min, max) } returns listOf(2, 3)

        mockMvc.perform(get("/api/list?min=$min&max=$max"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("2")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("3")))
    }

    /**
     * Tests that calling [PrimeFinder.startSearch] endpoint with an invalid parameter
     * result in a BAD_REQUEST response.
     */
    @Test
    fun testSearchStartValidationFails() {
        val threads = 1
        every { primeFinder.startSearch(threads) } throws PrimeFinderException("")

        mockMvc.perform(post("/api/start?threads=$threads"))
            .andExpect(status().isBadRequest)
    }

    /**
     * Tests that calling [PrimeFinder.stopSearch] endpoint with an invalid state
     * result in a BAD_REQUEST response.
     */
    @Test
    fun testSearchStopValidationFails() {
        every { primeFinder.stopSearch() } throws PrimeFinderException("")

        mockMvc.perform(post("/api/stop"))
            .andExpect(status().isBadRequest)
    }

    /**
     * Tests that calling [PrimeFinder.listPrimes] endpoint with invalid parameters
     * result in a BAD_REQUEST response.
     */
    @Test
    fun testListingValidationFails() {
        val min = 0L
        every { primeFinder.listPrimes(min, min) } throws PrimeFinderException("")

        mockMvc.perform(get("/api/list?min=$min&max=$min"))
            .andExpect(status().isBadRequest)
    }
}
