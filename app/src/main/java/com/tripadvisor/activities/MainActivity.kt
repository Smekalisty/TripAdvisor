package com.tripadvisor.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.tripadvisor.R
import com.tripadvisor.entities.City
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.net.UnknownHostException
import java.util.*

class MainActivity : AppCompatActivity() {
    private val disposables = CompositeDisposable()

    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    private var adapter: Adapter? = null

    private val keyDataSource = "keyDataSource"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refresh = findViewById(R.id.refresh)
        recyclerView = findViewById(R.id.recyclerView)

        refresh.isRefreshing = true
        refresh.setOnRefreshListener {
            requestDataSource()
        }

        adapter = Adapter().apply {
            val disposable = clickSubject.subscribe(this@MainActivity::onSelected, this@MainActivity::onError)
            disposables.add(disposable)
        }

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (savedInstanceState == null) {
            requestDataSource()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelableArrayList(keyDataSource, adapter?.dataSource)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        val dataSource =  savedInstanceState?.getParcelableArrayList<City>(keyDataSource)
        if (dataSource == null) {
            requestDataSource()
        } else {
            onSuccess(dataSource)
        }

        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    private fun requestDataSource() {
        val disposable = Single.fromCallable(this::readDataSource)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::onSuccess, this::onError)

        disposables.add(disposable)
    }

    private fun readDataSource() : ArrayList<City> {
        val array = arrayListOf<City>()

        assets.open("cities_custom.txt").use { stream ->
            stream.bufferedReader().use { bufferedReader ->
                while (true) {
                    val line = bufferedReader.readLine() ?: break

                    val results = line.split("\",")
                    if (results.size != 4) {
                        continue
                    }

                    var isValid = results.all { it.startsWith("\"") }
                    if (!isValid) {
                        continue
                    }

                    isValid = results[3].endsWith('"')
                    if (!isValid) {
                        continue
                    }

                    val name = results[0].removeRange(0, 1)
                    val county = results[1].removeRange(0, 1)

                    var url = results[2].removeRange(0, 1)
                    val index = url.indexOf(' ')
                    if (index != -1) {
                        url = url.substring(0, url.indexOf(' '))
                    }

                    var description = results[3].removeRange(0, 1)
                    description = description.removeRange(description.length - 1, description.length)

                    array.add(City(name, county, url, description))
                }
            }
        }

        return array
    }

    private fun onSuccess(cities: ArrayList<City>) {
        refresh.isRefreshing = false
        if (cities.isEmpty()) {
            showMessage(R.string.no_such_cities)
        } else {
            if (adapter == null) {
                showMessage(R.string.an_error_has_occurred)
            } else {
                adapter!!.dataSource = cities
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun onError(error: Throwable) {
        refresh.isRefreshing = false

        if (error is UnknownHostException) {
            showMessage(R.string.probably_no_connection)
        } else {
            val message = error.message ?: error.toString()
            showMessage(message)
        }
    }

    private fun showMessage(@StringRes id: Int) {
        Snackbar.make(recyclerView, id, Snackbar.LENGTH_SHORT).show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun onSelected(city: City) {
        if (!refresh.isRefreshing) {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.extraCity, city)
            startActivity(intent)
        }
    }

    private class Adapter : RecyclerView.Adapter<ViewHolder>() {
        val clickSubject = PublishSubject.create<City>()

        var dataSource = arrayListOf<City>()

        override fun getItemCount() = dataSource.size

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.layout_city, viewGroup, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val city = dataSource[position]

            val random = Random()
            val color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))

            val requestOptions = RequestOptions()
                .error(R.drawable.icon_default_image)
                .placeholder(ColorDrawable(color))
                .timeout(10 * 1000)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)

            Glide.with(viewHolder.itemView)
                .load(city.url)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.image)

            viewHolder.name.text = city.name
            viewHolder.county.text = city.county
            viewHolder.description.text = city.description

            viewHolder.itemView.setOnClickListener {
                clickSubject.onNext(city)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: AppCompatImageView = view.findViewById(R.id.image)
        var name: AppCompatTextView = view.findViewById(R.id.name)
        var county: AppCompatTextView = view.findViewById(R.id.county)
        var description: AppCompatTextView = view.findViewById(R.id.description)
    }
}