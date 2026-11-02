
package com.anonymous.waraclestore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.startsWith
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.containsString
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist

@RunWith(AndroidJUnit4::class)
class CouponTest {

    @get:Rule
    val activityRule =
        ActivityScenarioRule(MainActivity::class.java)

    /**
     * Verifies AC5 for a rejected coupon code.
    *
    * Reuses the same shopping journey and assertions for invalid
    * and empty coupon inputs, while keeping each test independently
    * identifiable in the test report.
    *
     * @param code The coupon code to submit; may be empty.
    */
    private fun verifyRejectedCoupon(code: String) {

        // Arrange: One T-shirt in the basket
        ShoppingFlow.addTshirtToCart()

        // Act: Submit the supplied coupon
        onView(withHint("Coupon code (try WARACLE25)"))
        .perform(replaceText(code), closeSoftKeyboard())

        onView(withText("Apply"))
        .perform(click())

        // Assert AC5: Rejection message
        onView(withText("That coupon is not valid."))
            .check(matches(isDisplayed()))

        // Assert AC5: No discount row
        onView(withText(startsWith("Discount (")))
            .check(doesNotExist())

        // Assert: Shipping and total remain unchanged
        onView(withText("£5.00"))
            .check(matches(isDisplayed()))

        onView(withText("£29.99"))
            .check(matches(isDisplayed()))
    }



    @Test
    fun validCouponAppliesCorrectDiscount() {

        // Arrange: Add one £24.99 T-shirt to the cart
        // using our verified shared shopping flow.
        ShoppingFlow.addTshirtToCart()

        // Act: Apply the valid coupon
        onView(withHint("Coupon code (try WARACLE25)"))
            .perform(replaceText("WARACLE25"), closeSoftKeyboard())

        onView(withText("Apply"))
            .perform(click())

        // Assert: AC1 + AC2
        // AC2: Required 25% discount
        onView(withText("Coupon applied: -£6.25"))
            .check(matches(isDisplayed()))

        // AC3: Required shipping
        onView(withText("£5.00"))
            .check(matches(isDisplayed()))

        // AC4: Required final total
        onView(withText("£23.74"))
            .check(matches(isDisplayed()))
    }



    @Test
    fun invalidCouponIsRejectedWithoutDiscount() {
        // AC5: Verify an unrecognised coupon is rejected without applying a discount.
        verifyRejectedCoupon("INVALID123")
    }

    @Test
    fun emptyCouponIsRejectedWithoutDiscount() {
        // AC5: Verify submitting an empty coupon is rejected without applying a discount.
        verifyRejectedCoupon("")
    }

    @Test
    fun validCouponCalculatesDiscountForTwoIdenticalTshirts() {

    // Arrange: Add the first Men's Logo T-Shirt
    ShoppingFlow.addTshirtToCart()

    // Act: Return from Cart to the product catalogue
    // Cart → ProductDetail: back returns to the previous screen
    onView(withContentDescription("Navigate up"))
        .perform(click())

    // Verify we are back on the same product's detail screen
    onView(withText("Add to Cart"))
        .check(matches(isDisplayed()))


    // Add the same default size (XS)
    onView(withText("Add to Cart"))
        .perform(click())

    // Assert: Two identical shirts have been combined
    onView(withText("Size XS · Qty 2"))
        .check(matches(isDisplayed()))

    // Assert: 2 × £24.99 = £49.98
    // Verify the basket subtotal, not the duplicate line-item price
    onView(
        allOf(
            withText("£49.98"),
            hasSibling(withText("Subtotal"))
        )
    ).check(matches(isDisplayed()))

    // Act (AC1): Apply WARACLE25 to the verified two-item basket
    onView(withHint("Coupon code (try WARACLE25)"))
        .perform(replaceText("WARACLE25"), closeSoftKeyboard())

    onView(withText("Apply"))
        .perform(click())

    // Assert (AC2): 25% of £49.98 = £12.495,
    // rounded to £12.50 for currency display
    onView(withText("Coupon applied: -£12.50"))
        .check(matches(isDisplayed()))

    // Assert (AC3): Standard shipping remains £5.00
    onView(withText("£5.00"))
        .check(matches(isDisplayed()))

    // Assert (AC4): £49.98 - £12.50 + £5.00 = £42.48
    onView(withText("£42.48"))
        .check(matches(isDisplayed()))
    }

    @Test
    fun shippingIsFivePoundsForNonEmptyBasket() {

    // Arrange: Add one £24.99 T-shirt to the basket
    ShoppingFlow.addTshirtToCart()

    // Assert AC3: A non-empty basket incurs £5 shipping
    onView(
        allOf(
            withText("£5.00"),
            hasSibling(withText("Shipping"))
        )
    ).check(matches(isDisplayed()))
    }

    @Test
    fun totalIsCorrectWithoutCoupon() {

    // Arrange: Add one £24.99 T-shirt to the basket
    ShoppingFlow.addTshirtToCart()

    // Assert AC4: £24.99 subtotal + £5.00 shipping
    // with no coupon discount = £29.99 total.
    onView(
        allOf(
            withText("£29.99"),
            hasSibling(withText("Total"))
        )
    ).check(matches(isDisplayed()))
    }


    @Test
    fun totalIsCorrectWithValidCoupon() {

    // Arrange: Add one £24.99 T-shirt to the basket
    ShoppingFlow.addTshirtToCart()

    // Act (AC1): Apply the valid 25% coupon
    onView(withHint("Coupon code (try WARACLE25)"))
        .perform(replaceText("WARACLE25"), closeSoftKeyboard())

    onView(withText("Apply"))
        .perform(click())

    // Assert AC4: £24.99 - £6.25 + £5.00 = £23.74
    // Check the labelled Total field independently of AC2.
    onView(
        allOf(
            withText("£23.74"),
            hasSibling(withText("Total"))
        )
    ).check(matches(isDisplayed()))
    }


    @Test
    fun couponCheckoutReachesShippingDetailsAfterLogin() {

    // Arrange: Add one T-shirt to the basket
    ShoppingFlow.addTshirtToCart()

    // Act: Apply the valid coupon
    onView(withHint("Coupon code (try WARACLE25)"))
        .perform(replaceText("WARACLE25"), closeSoftKeyboard())

    onView(withText("Apply"))
        .perform(click())

    // Act: Proceed from Cart
    onView(withText("Proceed to Checkout"))
        .perform(click())

    // Verify the login form, rather than matching the duplicated
    // "Sign in" text in the toolbar and screen heading.
    onView(withHint("you@example.com"))
    .check(matches(isDisplayed()))

    // LoginScreen has prefilled test credentials.
    // Their acceptance by the API is verified by this run.
    onView(withText("Sign In"))
        .perform(click())

    // Assert: Successful authentication reaches Checkout
    onView(withText("Shipping details"))
        .check(matches(isDisplayed()))


    // Act: Complete the shipping details using fictional data
    onView(withTagValue(equalTo("checkout_name_input")))
        .perform(replaceText("Alex Tester"), closeSoftKeyboard())

    onView(withHint("you@example.com"))
        .perform(replaceText("alex.tester@example.com"), closeSoftKeyboard())

    onView(withHint("1 Example Street"))
        .perform(replaceText("10 Test Street"), closeSoftKeyboard())

    onView(withHint("Glasgow"))
        .perform(replaceText("London"), closeSoftKeyboard())

    onView(withHint("G1 1AA"))
        .perform(replaceText("SW1A 1AA"), closeSoftKeyboard())

    // Verify the shipping details have been entered
    onView(withText("SW1A 1AA"))
        .check(matches(isDisplayed()))


    // Act: Complete demo payment fields using stable testIDs
    onView(withTagValue(equalTo("checkout_cardname_input")))
        .perform(scrollTo(), replaceText("Alex Tester"), closeSoftKeyboard())

    onView(withTagValue(equalTo("checkout_cardnumber_input")))
        .perform(scrollTo(), replaceText("4242424242424242"), closeSoftKeyboard())

    onView(withTagValue(equalTo("checkout_expiry_input")))
        .perform(scrollTo(), replaceText("1228"), closeSoftKeyboard())

    onView(withTagValue(equalTo("checkout_cvc_input")))
        .perform(scrollTo(), replaceText("123"), closeSoftKeyboard())

    // Check that the checkout action is available
    onView(withText(startsWith("Pay & Place Order")))
        .check(matches(isDisplayed()))


    // Act: Submit the completed demo checkout form
    onView(withText(startsWith("Pay & Place Order")))
        .perform(click())

    // Assert: Checkout successfully reaches confirmation
    onView(withText("Total paid"))
        .check(matches(isDisplayed()))

    // AC6: Confirmation must display the applied coupon code
    onView(withText(containsString("WARACLE25")))
        .check(matches(isDisplayed()))

    }





}
