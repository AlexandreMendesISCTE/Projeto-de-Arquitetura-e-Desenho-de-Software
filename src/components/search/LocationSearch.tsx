import { useState, useCallback, useEffect, useRef } from 'react'
import { Search, MapPin, Loader2, Navigation, Plus, X } from 'lucide-react'
import { useGeocoding } from '../../hooks/useGeocoding'
import { useGeolocation } from '../../hooks/useGeolocation'
import { useMapStore } from '../../store/map.store'
import { useRouteStore } from '../../store/route.store'
import { useSearchStore } from '../../store/search.store'
import { Location } from '../../types/route.types'

interface LocationSearchFieldProps {
  type: 'origin' | 'destination' | 'waypoint'
  placeholder: string
  currentLocation: Location | null
  onLocationSelect: (location: Location) => void
  onClearLocation?: () => void
}

const LocationSearchField = ({ placeholder, currentLocation, onLocationSelect, onClearLocation, type }: LocationSearchFieldProps) => {
  const [inputValue, setInputValue] = useState('')
  const [isExpanded, setIsExpanded] = useState(false)
  const { query, setQuery, results, setResults, setIsLoading, clear } = useSearchStore()
  const { setCenter, setZoom, setWaitingForInput } = useMapStore()
  const { location: currentGeoLocation, getCurrentLocation, isLoading: isGeolocating } = useGeolocation()
  const hasHandledGeoLocationRef = useRef(false)

  const { data: searchResults, isLoading: isSearching } = useGeocoding(query, query.length > 2 && isExpanded)

  const handleSearch = useCallback((value: string) => {
    setInputValue(value)
    if (value.length > 2) {
      setQuery(value)
    } else {
      clear()
    }
  }, [setQuery, clear])

  // Store onLocationSelect in a ref to avoid dependency issues
  const onLocationSelectRef = useRef(onLocationSelect)
  useEffect(() => {
    onLocationSelectRef.current = onLocationSelect
  }, [onLocationSelect])

  const handleSelectLocation = useCallback((location: Location, shouldPanMap: boolean = true) => {
    if (shouldPanMap) {
      setCenter([location.lat, location.lng])
      setZoom(15)
    }
    setInputValue('')
    setIsExpanded(false)
    clear()
    onLocationSelectRef.current(location)
  }, [setCenter, setZoom, clear])

  const handleUseCurrentLocation = () => {
    hasHandledGeoLocationRef.current = false
    getCurrentLocation()
  }

  const handleAddToMap = () => {
    // Clear the current location and set waiting state for map click
    if (onClearLocation) {
      onClearLocation()
    }
    setWaitingForInput(type)
    // Pan to current map center to show where user can click
    const currentCenter = useMapStore.getState().center
    setCenter(currentCenter)
    setZoom(15)
  }

  // Update results when search completes
  useEffect(() => {
    if (searchResults) {
      setResults(searchResults)
      setIsLoading(false)
    }
  }, [searchResults, setResults, setIsLoading])

  // Handle current location from geolocation - only once per geolocation
  useEffect(() => {
    if (currentGeoLocation && !hasHandledGeoLocationRef.current) {
      hasHandledGeoLocationRef.current = true
      // Use the ref to call the latest callback without causing re-renders
      if (currentGeoLocation) {
        setCenter([currentGeoLocation.lat, currentGeoLocation.lng])
        setZoom(15)
        setInputValue('')
        setIsExpanded(false)
        clear()
        onLocationSelectRef.current(currentGeoLocation)
      }
    }
  }, [currentGeoLocation, setCenter, setZoom, clear]) // Safe dependencies

  const displayResults = searchResults || results
  const displayName = currentLocation?.name || currentLocation?.address || placeholder

  return (
    <div className="relative w-full">
      <div className="flex gap-2">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
          <input
            type="text"
            value={isExpanded ? inputValue : displayName}
            onChange={(e) => {
              setIsExpanded(true)
              handleSearch(e.target.value)
            }}
            onFocus={() => setIsExpanded(true)}
            placeholder={placeholder}
            className="w-full pl-10 pr-4 py-1.5 md:py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm md:text-sm"
          />
        </div>
        <button
          onClick={handleUseCurrentLocation}
          disabled={isGeolocating}
          className="px-2.5 py-1.5 md:py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1 text-sm min-w-[40px] md:min-w-0"
          title="Usar localização atual"
        >
          {isGeolocating ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <MapPin className="w-4 h-4" />
          )}
        </button>
        {currentLocation && (
          <button
            onClick={handleAddToMap}
            className="px-2.5 py-1.5 md:py-2 bg-green-500 text-white rounded-md hover:bg-green-600 flex items-center gap-1 text-sm min-w-[40px] md:min-w-0"
            title="Limpar e adicionar pelo mapa"
          >
            <Navigation className="w-4 h-4" />
          </button>
        )}
        {!currentLocation && (
          <button
            onClick={handleAddToMap}
            className="px-2.5 py-1.5 md:py-2 bg-green-500 text-white rounded-md hover:bg-green-600 flex items-center gap-1 text-sm min-w-[40px] md:min-w-0"
            title="Adicionar pelo mapa"
          >
            <Navigation className="w-4 h-4" />
          </button>
        )}
      </div>

      {isExpanded && (isSearching || (displayResults && displayResults.length > 0)) && (
        <div className="absolute z-50 w-full mt-1 bg-white rounded-lg shadow-lg border border-gray-200 max-h-64 overflow-y-auto">
          {isSearching && (
            <div className="p-2 text-sm text-gray-500 flex items-center gap-2">
              <Loader2 className="w-4 h-4 animate-spin" />
              A pesquisar...
            </div>
          )}
          {displayResults && displayResults.length > 0 && displayResults.map((location, index) => (
            <button
              key={index}
              onClick={() => handleSelectLocation(location, true)}
              className="w-full text-left p-2 hover:bg-gray-100 rounded-md transition-colors"
            >
              <div className="font-medium text-sm">{location.name || 'Localização'}</div>
              {location.address && (
                <div className="text-xs text-gray-500">{location.address}</div>
              )}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

const LocationSearch = () => {
  const { origin, destination, waypoints, addWaypoint, removeWaypoint, setOrigin, setDestination, setWaypoints } = useRouteStore()
  const { waitingForInput } = useMapStore()

  return (
    <div data-location-search-container className="absolute top-2 left-2 right-2 md:top-4 md:left-4 md:right-auto md:w-96 z-[1000] bg-white rounded-lg shadow-lg p-1.5 md:p-4 space-y-1 md:space-y-3 max-h-[calc(100vh-120px)] md:max-h-none overflow-y-auto">
      <div>
        <label className="block text-xs font-medium text-gray-700 mb-0 md:mb-1">Origem</label>
        <LocationSearchField
          type="origin"
          placeholder="Pesquisar origem..."
          currentLocation={origin}
          onLocationSelect={setOrigin}
          onClearLocation={() => setOrigin(null)}
        />
      </div>
      
      {/* Waypoints Section */}
      {origin && (
        <div>
          <div className="flex items-center justify-between mb-0 md:mb-1">
            <label className="block text-xs font-medium text-gray-700">
              Paragens {waypoints.length > 0 && `(${waypoints.length}/5)`}
            </label>
            <button
              onClick={() => {
                // Add empty waypoint (will be set to null, not map center)
                addWaypoint({ lat: 0, lng: 0 })
                // Set waiting state so user can click map or search
                useMapStore.getState().setWaitingForInput('waypoint')
              }}
              disabled={waypoints.length >= 5}
              className="p-1 text-blue-600 hover:bg-blue-50 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-transparent"
              title={waypoints.length >= 5 ? "Máximo de 5 paragens" : "Adicionar paragem"}
            >
              <Plus className="w-4 h-4" />
            </button>
          </div>
          {waypoints.map((waypoint, index) => {
            // Show empty waypoint if waiting for input, otherwise skip empty ones
            const isEmpty = waypoint.lat === 0 && waypoint.lng === 0
            if (isEmpty && waitingForInput !== 'waypoint') {
              return null
            }
            
            return (
              <div key={index} className="flex gap-2 mb-2">
                <div className="flex-1">
                  <LocationSearchField
                    type="waypoint"
                    placeholder={`Paragem ${index + 1}...`}
                    currentLocation={isEmpty ? null : waypoint}
                    onLocationSelect={(loc) => {
                      const newWaypoints = [...waypoints]
                      newWaypoints[index] = loc
                      setWaypoints(newWaypoints)
                      // Clear waiting state when waypoint is set
                      useMapStore.getState().clearWaitingForInput()
                    }}
                    onClearLocation={() => {
                      // Clear this waypoint by setting it to empty
                      const newWaypoints = [...waypoints]
                      newWaypoints[index] = { lat: 0, lng: 0 } // Mark as empty
                      setWaypoints(newWaypoints)
                      useMapStore.getState().setWaitingForInput('waypoint')
                    }}
                  />
                </div>
                <button
                  onClick={() => removeWaypoint(index)}
                  className="p-2 text-red-600 hover:bg-red-50 rounded transition-colors"
                  title="Remover paragem"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            )
          })}
          
          {/* Show empty waypoint field when waiting for input but no empty waypoint exists */}
          {waitingForInput === 'waypoint' && waypoints.every(wp => wp.lat !== 0 || wp.lng !== 0) && (
            <div className="flex gap-2 mb-2">
              <div className="flex-1">
                <LocationSearchField
                  type="waypoint"
                  placeholder="Paragem..."
                  currentLocation={null}
                  onLocationSelect={(loc) => {
                    addWaypoint(loc)
                    useMapStore.getState().clearWaitingForInput()
                  }}
                  onClearLocation={() => {
                    useMapStore.getState().clearWaitingForInput()
                  }}
                />
              </div>
            </div>
          )}
        </div>
      )}

      <div data-destination-field>
        <label className="block text-xs font-medium text-gray-700 mb-0 md:mb-1">Destino</label>
        <LocationSearchField
          type="destination"
          placeholder="Pesquisar destino..."
          currentLocation={destination}
          onLocationSelect={setDestination}
          onClearLocation={() => setDestination(null)}
        />
      </div>
    </div>
  )
}

export default LocationSearch
