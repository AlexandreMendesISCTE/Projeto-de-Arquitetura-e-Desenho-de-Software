import { describe, it, expect, beforeEach } from 'vitest'
import { useRouteStore } from '../store/route.store'
import { useMapStore } from '../store/map.store'
import { useSearchStore } from '../store/search.store'
import { TransportMode } from '../types/route.types'
import { MAP_DEFAULT_CENTER, MAP_DEFAULT_ZOOM } from '../constants/api.constants'

const resetRouteStore = () => {
  useRouteStore.setState({
    origin: null,
    destination: null,
    waypoints: [],
    route: null,
    transportMode: TransportMode.DRIVING,
    isLoading: false,
  })
}

const resetMapStore = () => {
  useMapStore.setState({
    center: MAP_DEFAULT_CENTER,
    zoom: MAP_DEFAULT_ZOOM,
    selectedPoint: null,
    waitingForInput: null,
  })
}

const resetSearchStore = () => {
  useSearchStore.setState({
    query: '',
    results: [],
    isLoading: false,
  })
}

describe('Zustand stores', () => {
  beforeEach(() => {
    resetRouteStore()
    resetMapStore()
    resetSearchStore()
  })

  describe('route.store', () => {
    it('clears route when origin or destination is set', () => {
      useRouteStore.setState({
        route: { waypoints: [], totalDistance: 1000, totalDuration: 600, transportMode: TransportMode.DRIVING, geometry: { type: 'LineString', coordinates: [] } },
      })

      useRouteStore.getState().setOrigin({ lat: 1, lng: 2 })
      expect(useRouteStore.getState().route).toBeNull()

      useRouteStore.getState().route = {
        waypoints: [],
        totalDistance: 1000,
        totalDuration: 600,
        transportMode: TransportMode.DRIVING,
        geometry: { type: 'LineString', coordinates: [] },
      }
      useRouteStore.getState().setDestination({ lat: 3, lng: 4 })
      expect(useRouteStore.getState().route).toBeNull()
    })

    it('clears route when transport mode changes with a route present', () => {
      useRouteStore.setState({
        origin: { lat: 1, lng: 2 },
        destination: { lat: 3, lng: 4 },
        route: {
          waypoints: [],
          totalDistance: 1000,
          totalDuration: 600,
          transportMode: TransportMode.DRIVING,
          geometry: { type: 'LineString', coordinates: [] },
        },
      })

      useRouteStore.getState().setTransportMode(TransportMode.WALKING)
      expect(useRouteStore.getState().route).toBeNull()
      expect(useRouteStore.getState().transportMode).toBe(TransportMode.WALKING)
    })

    it('adds and removes waypoints', () => {
      useRouteStore.getState().addWaypoint({ lat: 10, lng: 20 })
      useRouteStore.getState().addWaypoint({ lat: 11, lng: 21 })
      expect(useRouteStore.getState().waypoints).toHaveLength(2)

      useRouteStore.getState().removeWaypoint(0)
      expect(useRouteStore.getState().waypoints).toHaveLength(1)
      expect(useRouteStore.getState().waypoints[0]).toMatchObject({ lat: 11, lng: 21 })
    })
  })

  describe('map.store', () => {
    it('updates center/zoom and waitingForInput flags', () => {
      useMapStore.getState().setCenter([40, -8])
      useMapStore.getState().setZoom(10)
      expect(useMapStore.getState().center).toEqual([40, -8])
      expect(useMapStore.getState().zoom).toBe(10)

      useMapStore.getState().setWaitingForInput('origin')
      expect(useMapStore.getState().waitingForInput).toBe('origin')
      useMapStore.getState().clearWaitingForInput()
      expect(useMapStore.getState().waitingForInput).toBeNull()
    })
  })

  describe('search.store', () => {
    it('updates query, results, and loading state', () => {
      useSearchStore.getState().setQuery('lisbon')
      useSearchStore.getState().setResults([{ lat: 1, lng: 2, name: 'Lisbon' }])
      useSearchStore.getState().setIsLoading(true)

      expect(useSearchStore.getState().query).toBe('lisbon')
      expect(useSearchStore.getState().results[0].name).toBe('Lisbon')
      expect(useSearchStore.getState().isLoading).toBe(true)

      useSearchStore.getState().clear()
      expect(useSearchStore.getState().query).toBe('')
      expect(useSearchStore.getState().results).toHaveLength(0)
      expect(useSearchStore.getState().isLoading).toBe(false)
    })
  })
})

