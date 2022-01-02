package com.proximity.aqi.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.proximity.app.BuildConfig
import com.proximity.app.ProxWorksApp
import com.proximity.app.R
import com.proximity.aqi.data.AqiChartEntry
import com.proximity.aqi.vm.CityViewModel
import com.proximity.aqi.vm.CityViewModelFactory

private const val LOG_TAG = "CityAqiChartFragment"

class CityAqiChartFragment : Fragment() {

    private val viewModel: CityViewModel by activityViewModels {
        CityViewModelFactory((requireContext().applicationContext as ProxWorksApp).repository)
    }

    private lateinit var chart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.city_aqi_chart_fragment, container, false)

        val city = viewModel.eventItemClickedLiveData.value!!.city

        chart = root.findViewById(R.id.chart)
        initChartView(city)

        viewModel.getAQIs(city) // get AQIs available since app-start till now

        viewModel.aqisListLiveData.observe(viewLifecycleOwner) { list ->
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "aqisListLiveData#onChanged ${list.size}")
            }
            /** with current logic of [CityViewModel.getAQIs], emptyList() is sent first,
            and we neither need to show it, nor need to observe for new AQIs in this case */
            if (list.isEmpty()) {
                return@observe
            }
            addEntries(list) // show AQIs available since app-start till now

            // now observe for new AQIs
            viewModel.getLatestAqiAsLiveData(city).observe(viewLifecycleOwner) {
                addEntry(it)
            }
        }

        return root
    }

    fun initChartView(city: String) {
        chart.getDescription().setEnabled(false)

        // enable touch gestures
        chart.setTouchEnabled(true)

        // enable scaling and dragging
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true)

        // set an alternative background color
        chart.setBackgroundColor(Color.LTGRAY)

        // add empty data
        val data = LineData()
        data.setValueTextColor(Color.WHITE)

        data.addDataSet(createSet(city))

        chart.setData(data)

        // get the legend (only possible after setting data)
        val l: Legend = chart.getLegend()
        l.setForm(Legend.LegendForm.LINE)
        //l.setTypeface(tfLight)
        l.setTextColor(Color.WHITE)

        val xl: XAxis = chart.getXAxis()
        //xl.setTypeface(tfLight)
        xl.setTextColor(Color.WHITE)
        xl.setDrawGridLines(true)
        // xl.setAvoidFirstLastClipping(true); // adding some extra margin before first visible entry
        xl.setAxisMinimum(0f)
        xl.granularity = 30f // default 1f is better
        xl.isGranularityEnabled = true
        // xl.setAxisMaximum(4f); // no-auto-h-scrolling
        // xl.setEnabled(true); // default is true
        xl.setPosition(XAxis.XAxisPosition.BOTTOM)
        xl.setLabelRotationAngle(270f)

        xl.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "getFormattedValue() $value")
                }
                // TODO - why this is needed multiple times for many values?
                // is anything wrong with chart API or it's configuration?
                return viewModel.getTimeLabel(value)
            }
        }

        val leftAxis: YAxis = chart.getAxisLeft()
        //leftAxis.setTypeface(tfLight)
        leftAxis.setTextColor(Color.WHITE)
        leftAxis.setAxisMaximum(600f) // TODO - can this (or visible range) be made appropriately dynamic?
        leftAxis.setAxisMinimum(0f) // TODO - can this (or visible range) be made appropriately dynamic?
        leftAxis.setDrawGridLines(true)

        val rightAxis: YAxis = chart.getAxisRight()
        rightAxis.setEnabled(false)
    }

    private fun addEntry(aqiChartEntry: AqiChartEntry) {
        val data: LineData = chart.getData()
        data.addEntry(Entry(aqiChartEntry.secondsSinceFirstEntry, aqiChartEntry.aqi.toFloat()), 0)
        data.notifyDataChanged()

        // let the chart know it's data has changed
        chart.notifyDataSetChanged()

        // limit the number of visible entries
        chart.setVisibleXRangeMinimum(70f)
        chart.setVisibleXRangeMaximum(160f) // TODO - why not in initChartView?
        //chart.setVisibleYRangeMaximum(150f, YAxis.AxisDependency.LEFT);

        // move to the latest entry
        chart.moveViewToX(aqiChartEntry.secondsSinceFirstEntry)

        // this automatically refreshes the chart (calls invalidate())
        // chart.moveViewTo(data.getXValCount()-7, 55f,
        // AxisDependency.LEFT);

    }

    private fun addEntries(aqiChartEntries: List<AqiChartEntry>) {
        val data: LineData = chart.getData()

        val list = mutableListOf<Entry>()
        for (entry in aqiChartEntries) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "addEntries() ${entry.secondsSinceFirstEntry}")
            }
            list.add(Entry(entry.secondsSinceFirstEntry, entry.aqi.toFloat()))
        }
        (data.getDataSetByIndex(0) as LineDataSet).values = list
        data.notifyDataChanged()

        // let the chart know it's data has changed
        chart.notifyDataSetChanged()

        // limit the number of visible entries
        chart.setVisibleXRangeMinimum(70f)
        chart.setVisibleXRangeMaximum(160f) // TODO - why not in initChartView?
        //chart.setVisibleYRangeMaximum(150f, YAxis.AxisDependency.LEFT);

        // move to the latest entry
        chart.moveViewToX(aqiChartEntries.last().secondsSinceFirstEntry)

        // this automatically refreshes the chart (calls invalidate())
        // chart.moveViewTo(data.getXValCount()-7, 55f,
        // AxisDependency.LEFT);

    }

    private fun createSet(city: String): LineDataSet {
        val set = LineDataSet(null, city)
        set.setAxisDependency(YAxis.AxisDependency.LEFT)
        set.setColor(ColorTemplate.getHoloBlue())
        set.setCircleColor(Color.WHITE)
        set.setLineWidth(2f)
        set.setCircleRadius(4f)
        set.setFillAlpha(65)
        set.setFillColor(ColorTemplate.getHoloBlue())
        set.setHighLightColor(Color.rgb(244, 117, 117))
        set.setValueTextColor(Color.WHITE)
        set.setValueTextSize(9f)
        set.setDrawValues(false)
        return set
    }
}