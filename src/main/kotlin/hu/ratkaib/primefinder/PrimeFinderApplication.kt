package hu.ratkaib.primefinder

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrimeFinderApplication

fun main(args: Array<String>) {
    runApplication<PrimeFinderApplication>(*args)
}
