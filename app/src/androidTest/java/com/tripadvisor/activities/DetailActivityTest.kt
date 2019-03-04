package com.tripadvisor.activities

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.tripadvisor.R
import com.tripadvisor.entities.City
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailActivityTest {
    @get:Rule
    val activityTestRule = object : ActivityTestRule<DetailActivity>(DetailActivity::class.java) {
        override fun getActivityIntent(): Intent {
            val city = City("Chisinau", "Moldova", "www.image.com", "Short description here")
            return Intent().putExtra(DetailActivity.extraCity, city)
        }
    }

    @Test
    fun checkTexts() {
        onView(withId(R.id.name)).check(matches(withText("Chisinau")))
        onView(withId(R.id.county)).check(matches(withText("Moldova")))
        onView(withId(R.id.description)).check(matches(withText("Short description here")))
    }
}