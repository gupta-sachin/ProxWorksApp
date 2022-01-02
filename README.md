# ProxWorksApp
Exercise App

# Architecture
1. App has only one activity, with just a fragment container, and two fragments.
2. There is only a single ViewModel, and that is used by the activity and both fragments too, so fragments are also passing activity as the view-model-owner.
3. ViewModel is accessing server and database only via repository instance.
4. Repository is fetching data from server via remote-source and saving the data into a local-source (room database) via a DAO.
5. Following single soource of truth copncept, repository is also providing the data to ViewModel as per requests, only from the local-source via same DAO.
6. Dao is connected to an in-memory room-database, so stored data will be available only till the app process is not killed. 

Note- If data is to be kept even after app-process is killed, like to show historical data, database should be changed from in-memory to persistent, and some other time based limit should be considered and implemented to prevent too much size of database. 


# Logic
1. AqiMainActivity initiates the calls for server connection/disconnection from onResume/onPause. App connects to server as soon as app enters foreground and closes the connection as soon as user leaves the app. This is done to avoid fetching data, when user is not on the app.

Note- Above also means, that if app is not killed and user will come back to the app, there may be some long gaps between the graph entries.

2. CitiesListFragment shows the list of cities with latest AQIs with a RecylerView using CityListAdapter, which also uses DiffUtils for some rendering optimizations and also to identify (and change the AQI cell's bg-color)  when the AQI band is changed for any city. CitiesListFragment observe citiesLiveData of the CityViewModel.

3. The CityViewModel#citiesLiveData is backed by a room-generated-flow of a list, containing only the latest AQI data for each city. This flow also maps fields of 'City' to 'CityUi' as per the needs of the UI design, and also adds a new field for AQI-band specific color.

4. When any city is selected from the list, it adds an event to CityViewModel#eventItemClickedLiveData, which is being observed by AqiMainActivity, to show the CityAqiChartFragment.

5. CityAqiChartFragment shows the chart for selected city with MPAndroidChart Library. It first gets the list of AQIs for selected city via CityViewModel, which are already available in the databse since app-start, and after showing those, it start observing only the latest AQI for that selected city. Interval based filtering and setting the value of 'secondsSinceFirstEntry' for each AQI is also done in CityViewModel.

6. As per current logic, whenever a city is selected or re-selected, CityViewModel need to clear previous list. Even if just the Fragment's view is recreated due to any configuration change, and thus city is not changed, app will have to re-fetch the city-wise data from database and filter by interval. If we want to optimize it, we will need to have city-wise view-model-level cache, whihc will need a few changes in code.
        
7. Once the already available AQIs of selected city have been shown on chart, CityAqiChartFragment starts observing latest AQI for selected city. As per current logic, the first latest entry will always be dropped by interval-based-filtering logic, as this must already be shown while showing the list of already available AQIs. If city-wise view-model-level cache will be implemented, each new shown entry will need to be added to cached list(s).
