package com.outsystems.plugins.healthfitness

import org.junit.Assert
import kotlin.test.Test

import com.outsystems.plugins.healthfitness.store.HealthStore;

class ExampleUnitTestKoltin {

    @Test
    fun testSum0() {
        val expected = 42
        Assert.assertEquals(expected.toLong(), 42)
    }

    @Test
    fun testSum1() {
        val expected = 43
        Assert.assertEquals(expected.toLong(), 43)
    }

    @Test
    fun testSum2() {
        val expected = 42
        Assert.assertEquals(expected.toLong(), 43)
    }

}