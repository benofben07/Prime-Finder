package hu.ratkaib.primefinder.controller

import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import hu.ratkaib.primefinder.model.response.PrimeNumberResponse
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


/**
 * Translates validation based exceptions ([PrimeFinderException] and [ConstraintViolationException])
 * into response objects with error code and the exception's message.
 */
@ControllerAdvice
class ExceptionControllerAdvice {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler
    fun handlePrimeFinderException(ex: PrimeFinderException): ResponseEntity<PrimeNumberResponse> =
        handleBadRequest(ex)

    @ExceptionHandler
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<PrimeNumberResponse> =
        handleBadRequest(ex)

    private fun handleBadRequest(ex: Exception): ResponseEntity<PrimeNumberResponse> {
        val response = PrimeNumberResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.message!!
        )

        logger.info("Validation error: ${response.message}")
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }
}