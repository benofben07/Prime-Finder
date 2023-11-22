package hu.ratkaib.primefinder.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job

fun CoroutineScope.getJobs() = this.coroutineContext.job.children.toList()