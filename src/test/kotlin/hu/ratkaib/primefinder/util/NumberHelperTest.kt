package hu.ratkaib.primefinder.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NumberHelperTest {

    @Test
    fun testIsNegativePrime() {
        assertFalse { isNumberPrime(-1) }
    }

    @Test
    fun testIs1Prime() {
        assertFalse { isNumberPrime(1) }

    }

    @Test
    fun testIs2Prime() {
        assertTrue { isNumberPrime(2) }
    }

    @Test
    fun testAreNumbersPrime() {
        assertTrue { isNumberPrime(3) }
        assertTrue { isNumberPrime(5) }
        assertTrue { isNumberPrime(199) }
        assertTrue { isNumberPrime(6091) }

    }

    @Test
    fun testAreNumbersNotPrime() {
        assertFalse { isNumberPrime(6) }
        assertFalse { isNumberPrime(8) }
        assertFalse { isNumberPrime(55) }
        assertFalse { isNumberPrime(6093) }
    }

    @Test
    fun testClosestSmallerPrimeSmallerThan2() {
        assertNull(findClosestSmallerOrEqualPrime(-1))
        assertNull(findClosestSmallerOrEqualPrime(0))
        assertNull(findClosestSmallerOrEqualPrime(1))
    }

    @Test
    fun testClosestSmallerPrimeGreaterThan1() {
        assertEquals(2, findClosestSmallerOrEqualPrime(2))

        assertEquals(3, findClosestSmallerOrEqualPrime(3))
        assertEquals(3, findClosestSmallerOrEqualPrime(4))

        assertEquals(5, findClosestSmallerOrEqualPrime(5))

        assertEquals(7, findClosestSmallerOrEqualPrime(7))
        assertEquals(7, findClosestSmallerOrEqualPrime(8))
        assertEquals(7, findClosestSmallerOrEqualPrime(9))

        assertEquals(5011, findClosestSmallerOrEqualPrime(5020))
        assertEquals(5021, findClosestSmallerOrEqualPrime(5021))
        assertEquals(5021, findClosestSmallerOrEqualPrime(5022))
    }


}