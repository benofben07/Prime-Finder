package hu.ratkaib.primefinder.config

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConfigurationBean {

    @Bean("primeFinderCoroutineScope")
    fun getCoroutineScope(coroutineDispatcher: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(coroutineDispatcher)

    @Bean("primeFinderCoroutineDispatcher")
    fun getCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.IO
}