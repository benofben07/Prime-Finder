package hu.ratkaib.primefinder.util

/**
 * Finds the closes number below the given number (returns itself if given number is prime).
 *
 * @return the closes number below the given number (returns itself if given number is prime).
 * Returns null if the number is 1 or smaller.
 */
fun findClosestSmallerOrEqualPrime(maxValue: Long): Long? {
    if (maxValue < 2) {
        return null
    }

    var observedNumber = maxValue
    // only check odd numbers
    if (2L != observedNumber && observedNumber % 2 == 0L) {
        observedNumber--
    }

    while (true) {
        if (isNumberPrime(observedNumber)) {
            return observedNumber
        } else {
            observedNumber -= 2
        }
    }
}

/**
 * Checks if a number is prime. Negative numbers are considered non prime and won't be checked.
 * 1 and 2 are handled as special cases, other numbers will only be checked if they have any odd divider
 * (between 3 and the observed number's half), since the only prime number that can have an even divider is 2.
 * Dividing by 2 at the start to sort out even numbers.
 *
 * @return true if a number is prime, false otherwise.
 */
fun isNumberPrime(number: Long): Boolean {
    if (number <= 1L) {
        return false
    } else if (number == 2L) {
        return true
    } else if (number % 2L == 0L) {
        return false
    }

    for (i in 3L..number / 2 step 2) {
        if (number % i == 0L) {
            return false
        }
    }

    return true
}