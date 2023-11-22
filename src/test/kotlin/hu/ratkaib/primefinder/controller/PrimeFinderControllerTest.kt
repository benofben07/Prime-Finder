package hu.ratkaib.primefinder.controller

import com.ninjasquad.springmockk.MockkBean
import hu.ratkaib.primefinder.interfaces.PrimeFinder
import hu.ratkaib.primefinder.model.exception.PrimeFinderException
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
class PrimeFinderControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var primeFinder: PrimeFinder

    @Test
    fun testSearchStarted() {
        val threads = 1
        justRun { primeFinder.startSearch(threads) }

        mockMvc.perform(post("/api/start?threads=$threads"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("started")))
    }

    @Test
    fun testSearchStartValidationFails() {
        val threads = 1
        every { primeFinder.startSearch(threads) } throws PrimeFinderException("")

        mockMvc.perform(post("/api/start?threads=$threads"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun testSearchStopped() {
        justRun { primeFinder.stopSearch() }

        mockMvc.perform(post("/api/stop"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("stopped")))
    }

    @Test
    fun testSearchStopValidationFails() {
        every { primeFinder.stopSearch() } throws PrimeFinderException("")

        mockMvc.perform(post("/api/stop"))
            .andExpect(status().isBadRequest)
    }

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

    @Test
    fun testListingValidationFails() {
        val min = 0L
        every { primeFinder.listPrimes(min, min) } throws PrimeFinderException("")

        mockMvc.perform(get("/api/list?min=$min&max=$min"))
            .andExpect(status().isBadRequest)
    }
}
