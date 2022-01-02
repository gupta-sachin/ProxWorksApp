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


# AqiMainActivity initiates the calls for server connection/disconnection from onResume/onPause.
App connects to server as soon as app enters foreground and closes the connection as soon as user leaves the app. This is done to avoid fetching data, when user is not on the app.

Note- Above also means, that if app is not killed and user will come back to the app, there may be some long gaps between the graph entries.


# CitiesListFragment shows the list of cities with latest AQIs.


# CityAqiChartFragment shows the chart for selected city.


# All business logic is kept in CityViewModel
