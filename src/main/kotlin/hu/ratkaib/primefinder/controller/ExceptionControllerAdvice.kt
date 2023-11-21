package hu.ratkaib.primefinder.controller

import hu.ratkaib.primefinder.model.exception.ErrorMessage
import hu.ratkaib.primefinder.model.exception.PrimeFinderException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class ExceptionControllerAdvice {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler
    fun handlePrimeFinderException(ex: PrimeFinderException): ResponseEntity<ErrorMessage> =
        handleBadRequest(ex)

    @ExceptionHandler
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ErrorMessage> =
        handleBadRequest(ex)

    private fun handleBadRequest(ex: Exception): ResponseEntity<ErrorMessage> {
        val errorMessage = ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            ex.message!!
        )

        logger.info("Validation error: ${errorMessage.message}")
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }
}