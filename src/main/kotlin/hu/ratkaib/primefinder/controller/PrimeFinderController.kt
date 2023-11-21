package hu.ratkaib.primefinder.controller

import hu.ratkaib.primefinder.interfaces.PrimeFinder
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/")
@Validated
class PrimeFinderController(private val finder: PrimeFinder) {

    @PostMapping("/start")
    fun start(@RequestParam @Min(1) threads: Int): ResponseEntity<String> {
        finder.startSearch(threads)
        return ResponseEntity.ok("Started")
    }

    @PostMapping("/stop")
    fun stop(): ResponseEntity<String> {
        finder.stopSearch()
        return ResponseEntity.ok("Stopped")
    }

    @GetMapping("/list")
    fun list(
        @RequestParam(name = "min") @Min(1) minValue: Long,
        @RequestParam(name = "max") @Min(1) maxValue: Long
    ): List<Long> {
        return finder.listPrimes(minValue, maxValue)
    }
}

