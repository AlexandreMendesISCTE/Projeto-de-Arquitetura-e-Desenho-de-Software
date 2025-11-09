import MapContainer from './map/MapContainer'
import LocationSearch from './search/LocationSearch'
import TransportModeSelector from './route/TransportModeSelector'
import RouteInfo from './route/RouteInfo'

const MapRouteExplorer = () => {
  return (
    <div className="relative w-full h-screen">
      <MapContainer />
      <LocationSearch />
      {/* Desktop transport mode selector */}
      <div className="hidden md:block">
        <TransportModeSelector />
      </div>
      <RouteInfo />
    </div>
  )
}

export default MapRouteExplorer

