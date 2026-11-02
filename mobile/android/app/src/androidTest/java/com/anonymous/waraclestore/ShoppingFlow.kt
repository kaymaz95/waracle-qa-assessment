
package com.anonymous.waraclestore

import android.os.SystemClock
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Assert.fail

/**
 * Reusable navigation for tests that require one
 * Men's Logo T-Shirt in the shopping cart.
 */
object ShoppingFlow {

    private fun waitForShopNow(timeoutMs: Long = 20000) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            try {
                onView(withText("Shop Now"))
                    .check(matches(isDisplayed()))
                return
            } catch (e: NoMatchingViewException) {
                SystemClock.sleep(250)
            }
        }

        fail("Shop Now was not displayed within $timeoutMs ms")
    }

    fun addTshirtToCart() {
        waitForShopNow()

        onView(withText("Shop Now"))
            .perform(click())

        onView(withText("All Products"))
            .check(matches(isDisplayed()))

        onView(withText("Men's Logo T-Shirt"))
            .perform(click())

        onView(withText("Add to Cart"))
            .check(matches(isDisplayed()))

        onView(withText("Add to Cart"))
            .perform(click())

        onView(withText("Proceed to Checkout"))
            .check(matches(isDisplayed()))
    }
}
