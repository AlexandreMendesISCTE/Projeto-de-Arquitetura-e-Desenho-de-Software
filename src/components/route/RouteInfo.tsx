import { Route, RotateCcw, Loader2, Map, MapPinned, Car, Bike, Footprints } from 'lucide-react'
import { useRouteStore } from '../../store/route.store'
import { usePOIStore } from '../../store/poi.store'
import { useRoute } from '../../hooks/useRoute'
import { useEffect, useState } from 'react'
import { formatDistance, formatDuration } from '../../utils/route.utils'
import { openRouteInGoogleMaps } from '../../utils/google-maps.utils'
import { TransportMode } from '../../types/route.types'

const RouteInfo = () => {
  const {
    origin,
    destination,
    route,
    transportMode,
    waypoints,
    setRoute,
    reset,
    setIsLoading,
    setTransportMode,
  } = useRouteStore()
  const { enabled: poiEnabled, toggle: togglePOIs } = usePOIStore()
  const {
    data: calculatedRoute,
    isLoading,
    error,
  } = useRoute(origin, destination, transportMode, waypoints)
  const [isTransportMenuOpen, setIsTransportMenuOpen] = useState(false)

  useEffect(() => {
    if (calculatedRoute) {
      setRoute(calculatedRoute)
      setIsLoading(false)
    }
  }, [calculatedRoute, setRoute, setIsLoading])

  useEffect(() => {
    setIsLoading(isLoading)
  }, [isLoading, setIsLoading])

  const handleOpenInGoogleMaps = () => {
    if (route && origin && destination) {
      openRouteInGoogleMaps(origin, destination, transportMode, waypoints)
    }
  }

  if (!origin && !destination && !route) {
    return null
  }

  // Check if route info should be visible (when destination is set)
  const isVisible = !!(origin && destination) || !!route

  return (
    <div
      className={`absolute bottom-0 left-0 right-0 md:bottom-4 md:left-4 md:right-auto md:min-w-[300px] z-[1000] bg-white rounded-lg shadow-lg p-3 md:p-4 rounded-b-none md:rounded-b-lg transition-all duration-500 ease-out ${
        isVisible ? 'translate-y-0 opacity-100' : 'translate-y-full opacity-0'
      }`}
    >
      <div className="flex items-center justify-between mb-3">
        <h3 className="font-semibold text-lg flex items-center gap-2">
          <Route className="w-5 h-5" />
          <span className="hidden sm:inline">Informações da Rota</span>
          <span className="sm:hidden">Rota</span>
        </h3>
        <div className="flex gap-1.5 md:gap-2 items-center">
          {/* Mobile Transport Mode Selector - Icon only, to the left of POI */}
          <div className="md:hidden relative">
            <button
              onClick={() => setIsTransportMenuOpen(!isTransportMenuOpen)}
              className="flex items-center justify-center w-8 h-8 rounded transition-colors hover:bg-gray-100 text-gray-600"
              title={
                transportMode === TransportMode.DRIVING
                  ? 'Carro'
                  : transportMode === TransportMode.BICYCLING
                    ? 'Bicicleta'
                    : 'A pé'
              }
            >
              {transportMode === TransportMode.DRIVING && <Car className="w-4 h-4" />}
              {transportMode === TransportMode.BICYCLING && <Bike className="w-4 h-4" />}
              {transportMode === TransportMode.WALKING && <Footprints className="w-4 h-4" />}
            </button>

            {/* Slide-out menu from right to left - horizontal row */}
            {isTransportMenuOpen && (
              <>
                {/* Backdrop */}
                <div
                  className="fixed inset-0 bg-black bg-opacity-20 z-[9998]"
                  onClick={() => setIsTransportMenuOpen(false)}
                />
                {/* Menu - horizontal row sliding from right to left */}
                <div className="absolute right-full top-1/2 -translate-y-1/2 mr-2 bg-white rounded-lg shadow-xl p-1 flex flex-row gap-1.5 items-center z-[9999] animate-slide-in-right">
                  {[
                    { mode: TransportMode.DRIVING, icon: Car },
                    { mode: TransportMode.BICYCLING, icon: Bike },
                    { mode: TransportMode.WALKING, icon: Footprints },
                  ]
                    .filter((m) => m.mode !== transportMode)
                    .map(({ mode, icon: Icon }) => (
                      <button
                        key={mode}
                        onClick={() => {
                          setTransportMode(mode)
                          setIsTransportMenuOpen(false)
                        }}
                        className="flex items-center justify-center w-8 h-8 hover:bg-gray-100 rounded transition-colors"
                        title={
                          mode === TransportMode.DRIVING
                            ? 'Carro'
                            : mode === TransportMode.BICYCLING
                              ? 'Bicicleta'
                              : 'A pé'
                        }
                      >
                        <Icon className="w-4 h-4 text-gray-700" />
                      </button>
                    ))}
                </div>
              </>
            )}
          </div>

          {/* POI Toggle Button */}
          {route && (
            <button
              onClick={togglePOIs}
              className={`flex items-center justify-center w-8 h-8 rounded transition-colors ${
                poiEnabled ? 'bg-purple-100 text-purple-600' : 'hover:bg-gray-100 text-gray-600'
              }`}
              title={poiEnabled ? 'Ocultar pontos de interesse' : 'Mostrar pontos de interesse'}
            >
              <MapPinned className="w-4 h-4" />
            </button>
          )}
          {route && (
            <button
              onClick={handleOpenInGoogleMaps}
              className="flex items-center justify-center w-8 h-8 hover:bg-gray-100 rounded transition-colors"
              title="Abrir no Google Maps"
            >
              <Map className="w-4 h-4 text-blue-600" />
            </button>
          )}
          <button
            onClick={reset}
            className="flex items-center justify-center w-8 h-8 hover:bg-gray-100 rounded transition-colors"
            title="Limpar rota"
          >
            <RotateCcw className="w-4 h-4 text-gray-600" />
          </button>
        </div>
      </div>

      {isLoading && (
        <div className="flex items-center gap-2 text-gray-600">
          <Loader2 className="w-4 h-4 animate-spin" />
          <span className="text-sm">A calcular rota...</span>
        </div>
      )}

      {error && (
        <div className="text-red-600 text-sm mb-2">
          Erro ao calcular rota: {error instanceof Error ? error.message : 'Erro desconhecido'}
        </div>
      )}

      {route && (
        <div className="space-y-2">
          <div className="flex justify-between items-center">
            <span className="text-sm text-gray-600">Distância:</span>
            <span className="font-semibold">{formatDistance(route.totalDistance)}</span>
          </div>
          <div className="flex justify-between items-center">
            <span className="text-sm text-gray-600">Tempo estimado:</span>
            <span className="font-semibold">{formatDuration(route.totalDuration)}</span>
          </div>
          <div className="flex justify-between items-center">
            <span className="text-sm text-gray-600">Modo:</span>
            <span className="font-semibold">
              {transportMode === TransportMode.DRIVING
                ? 'Carro'
                : transportMode === TransportMode.BICYCLING
                  ? 'Bicicleta'
                  : 'A pé'}
            </span>
          </div>
          {waypoints.length > 0 && (
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Paragens:</span>
              <span className="font-semibold">{waypoints.length}</span>
            </div>
          )}
        </div>
      )}

      {origin && !destination && (
        <div className="text-sm text-gray-600 mt-2">Clique no mapa para definir o destino</div>
      )}
    </div>
  )
}

export default RouteInfo
