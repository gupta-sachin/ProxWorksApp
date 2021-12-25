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
import com.github.mikephil.charting.components.AxisBase
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

    private var START_MILLIS: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.city_aqi_chart_fragment, container, false)

        val city = viewModel.eventItemClickedLiveData.value!!.city

        chart = root.findViewById(R.id.chart)
        initChartView(city)

        viewModel.getCityAQIsLiveData(city).observe(viewLifecycleOwner) {
            val aqiChartEntry = it[0]
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "onChanged $aqiChartEntry")
            }
            addEntry(aqiChartEntry)
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
        // xl.setAxisMaximum(4f); // no-auto-h-scrolling
        // xl.setEnabled(true); // default is true
        xl.setPosition(XAxis.XAxisPosition.BOTTOM)
        xl.setLabelRotationAngle(270f)

        xl.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String? {
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "getFormattedValue() called with: value = $value, axis = $axis")
                }
                return "Time $value"
            }
        })

        val leftAxis: YAxis = chart.getAxisLeft()
        //leftAxis.setTypeface(tfLight)
        leftAxis.setTextColor(Color.WHITE)
        leftAxis.setAxisMaximum(600f) // TODO - can it or visible range be made appropriately dynamic?
        leftAxis.setAxisMinimum(0f) // TODO - can it or visible range be made appropriately dynamic?
        leftAxis.setDrawGridLines(true)

        val rightAxis: YAxis = chart.getAxisRight()
        rightAxis.setEnabled(false)

    }

    private fun addEntry(pair: AqiChartEntry) {
        val data: LineData = chart.getData()

        if (START_MILLIS == -1L) {
            START_MILLIS = pair.time
        }
        val x = ((pair.time - START_MILLIS) / 1000).toFloat()
        data.addEntry(Entry(x, pair.aqi.toFloat()), 0)
        data.notifyDataChanged()

        // let the chart know it's data has changed
        chart.notifyDataSetChanged()

        // limit the number of visible entries
        chart.setVisibleXRangeMaximum(10f) // TODO - why not in initChartView?
        //chart.setVisibleYRangeMaximum(150f, YAxis.AxisDependency.LEFT);

        // move to the latest entry
        chart.moveViewToX(x)

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