package hu.ratkaib.primefinder.controller

import hu.ratkaib.primefinder.service.PrimeFinderService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PrimeFinderController(private val finder: PrimeFinderService) {

    @GetMapping("/start")
    fun start(): ResponseEntity<String> {
        finder.startSearch(4)
        return ResponseEntity.ok("Started")
    }

    @GetMapping("/stop")
    fun stop(): ResponseEntity<String> {
        finder.stopSearch()
        return ResponseEntity.ok("Stopped")
    }

    @GetMapping("/list")
    fun list() {

    }

}