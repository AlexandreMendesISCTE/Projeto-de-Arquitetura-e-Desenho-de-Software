import { Route, RotateCcw, Loader2, Map, MapPinned } from 'lucide-react'
import { useRouteStore } from '../../store/route.store'
import { usePOIStore } from '../../store/poi.store'
import { useRoute } from '../../hooks/useRoute'
import { useEffect } from 'react'
import { formatDistance, formatDuration } from '../../utils/route.utils'
import { openRouteInGoogleMaps } from '../../utils/google-maps.utils'

const RouteInfo = () => {
  const { origin, destination, route, transportMode, waypoints, setRoute, reset, setIsLoading } = useRouteStore()
  const { enabled: poiEnabled, toggle: togglePOIs } = usePOIStore()
  const { data: calculatedRoute, isLoading, error } = useRoute(origin, destination, transportMode, waypoints)

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

  return (
    <div className="absolute bottom-4 left-4 right-4 md:right-auto md:min-w-[300px] z-[1000] bg-white rounded-lg shadow-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <h3 className="font-semibold text-lg flex items-center gap-2">
          <Route className="w-5 h-5" />
          <span className="hidden sm:inline">Informações da Rota</span>
          <span className="sm:hidden">Rota</span>
        </h3>
        <div className="flex gap-2">
          {/* POI Toggle Button */}
          {route && (
            <button
              onClick={togglePOIs}
              className={`p-1 rounded transition-colors ${
                poiEnabled
                  ? 'bg-purple-100 text-purple-600'
                  : 'hover:bg-gray-100 text-gray-600'
              }`}
              title={poiEnabled ? 'Ocultar pontos de interesse' : 'Mostrar pontos de interesse'}
            >
              <MapPinned className="w-4 h-4" />
            </button>
          )}
          {route && (
            <button
              onClick={handleOpenInGoogleMaps}
              className="p-1 hover:bg-gray-100 rounded transition-colors"
              title="Abrir no Google Maps"
            >
              <Map className="w-4 h-4 text-blue-600" />
            </button>
          )}
          <button
            onClick={reset}
            className="p-1 hover:bg-gray-100 rounded transition-colors"
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
            <span className="font-semibold capitalize">{transportMode}</span>
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
        <div className="text-sm text-gray-600 mt-2">
          Clique no mapa para definir o destino
        </div>
      )}
    </div>
  )
}

export default RouteInfo

