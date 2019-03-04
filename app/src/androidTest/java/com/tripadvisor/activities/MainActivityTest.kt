package com.tripadvisor.activities

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.tripadvisor.R
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    //Do not judge strictly. These are my first tests for android

    @get:Rule
    val activityTestRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    @Test
    fun recyclerViewTest() {
        val recyclerView = activityTestRule.activity.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = recyclerView.adapter
        Assert.assertNotNull(adapter)
        Assert.assertEquals(7, adapter!!.itemCount)

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(0)
        Assert.assertNotNull(viewHolder)

        Assert.assertTrue(viewHolder is MainActivity.ViewHolder)
        val mainViewHolder = viewHolder as MainActivity.ViewHolder
        Assert.assertEquals("Chisinau", mainViewHolder.name.text)
        Assert.assertEquals("Moldova", mainViewHolder.county.text)
        Assert.assertTrue(mainViewHolder.description.text.startsWith("Is the capital and largest city"))
    }

    @Test
    fun onCitySelectedTest() {
        Intents.init()
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition<MainActivity.ViewHolder>(0, click()))
        intended(hasComponent(DetailActivity::class.java.name))
        intended(hasExtraWithKey(DetailActivity.extraCity))
        Intents.release()
    }
}