package hu.ratkaib.primefinder.controller

import hu.ratkaib.primefinder.model.response.PrimeNumberResponse
import hu.ratkaib.primefinder.service.PrimeFinder
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api", produces = ["application/json"])
@Validated
class PrimeFinderController(private val primeFinder: PrimeFinder) {

    @PostMapping("/start")
    fun start(@RequestParam @Min(1) threads: Int): ResponseEntity<PrimeNumberResponse> {
        primeFinder.startSearch(threads)
        return createOkResponse("Prime number searching started.")
    }

    @PostMapping("/stop")
    fun stop(): ResponseEntity<PrimeNumberResponse> {
        primeFinder.stopSearch()
        return createOkResponse("Prime number searching stopped.")
    }

    @GetMapping("/list")
    fun list(
        @RequestParam(name = "min") @Min(1) minValue: Long,
        @RequestParam(name = "max") @Min(1) maxValue: Long
    ): List<Long> {
        return primeFinder.listPrimes(minValue, maxValue)
    }

    private fun createOkResponse(message: String): ResponseEntity<PrimeNumberResponse> {
        val response = PrimeNumberResponse(HttpStatus.OK.value(), message)
        return ResponseEntity.ok(response)
    }
}

