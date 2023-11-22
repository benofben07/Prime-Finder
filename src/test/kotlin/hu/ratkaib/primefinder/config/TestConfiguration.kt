package hu.ratkaib.primefinder.config

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
@OptIn(ExperimentalCoroutinesApi::class)
class PrimeNumberTestConfiguration {

    @Bean("testPrimeFinderCoroutineScope")
    @Primary
    fun getCoroutineScope(testPrimeFinderCoroutineDispatcher: CoroutineDispatcher): CoroutineScope =
        TestScope(testPrimeFinderCoroutineDispatcher)

    @Bean("testPrimeFinderCoroutineDispatcher")
    @Primary
    fun getCoroutineDispatcher(): CoroutineDispatcher = StandardTestDispatcher()
}