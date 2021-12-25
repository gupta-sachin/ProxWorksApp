package com.proximity.aqi.view

import android.os.Bundle
import android.util.JsonReader
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.proximity.app.ProxWorksApp
import com.proximity.app.R
import com.proximity.aqi.data.City
import com.proximity.aqi.vm.CityViewModel
import com.proximity.aqi.vm.CityViewModelFactory
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.net.URI

class CitiesListFragment : Fragment() {

    private val viewModel: CityViewModel by activityViewModels {
        CityViewModelFactory((requireContext().applicationContext as ProxWorksApp).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.cities_list_fragment, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = CityListAdapter(viewModel)
        recyclerView.adapter = adapter

        viewModel.citiesLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return root
    }
}