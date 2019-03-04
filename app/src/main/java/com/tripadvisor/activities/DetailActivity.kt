package com.tripadvisor.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.tripadvisor.R
import com.tripadvisor.entities.City

class DetailActivity : AppCompatActivity() {
    companion object {
        const val extraCity = "extraCity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val image = findViewById<AppCompatImageView>(R.id.image)
        val name = findViewById<AppCompatTextView>(R.id.name)
        val county = findViewById<AppCompatTextView>(R.id.county)
        val description = findViewById<AppCompatTextView>(R.id.description)

        val city = intent.getParcelableExtra<City>(extraCity)

        name.text = city.name
        county.text = city.county
        description.text = city.description

        Glide.with(this)
            .load(city.url)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(image)
    }
}