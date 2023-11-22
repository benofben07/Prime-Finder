package hu.ratkaib.primefinder.service.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConfigurationBean {

    @Bean("primeFinderCoroutineScope")
    fun getCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)
}