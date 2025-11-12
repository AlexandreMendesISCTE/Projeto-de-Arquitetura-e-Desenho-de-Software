import { create } from 'zustand'
import { RouteState } from '../types/store.types'
import { TransportMode } from '../types/route.types'

export const useRouteStore = create<RouteState>((set) => ({
  origin: null,
  destination: null,
  waypoints: [],
  route: null,
  transportMode: TransportMode.DRIVING,
  isLoading: false,
  setOrigin: (location) => set({ 
    origin: location,
    route: null // Clear route when origin changes to force recalculation
  }),
  setDestination: (location) => set({ 
    destination: location,
    route: null // Clear route when destination changes to force recalculation
  }),
  setWaypoints: (waypoints) => set({ waypoints }),
  addWaypoint: (waypoint) => set((state) => ({ waypoints: [...state.waypoints, waypoint] })),
  removeWaypoint: (index) => set((state) => ({ 
    waypoints: state.waypoints.filter((_, i) => i !== index),
    route: null // Clear route when waypoints change
  })),
  setRoute: (route) => set({ route }),
  setTransportMode: (mode) => 
    set((state) => {
      // Clear route when transport mode changes to force recalculation
      if (state.route && state.origin && state.destination) {
        return { transportMode: mode, route: null }
      }
      return { transportMode: mode }
    }),
  setIsLoading: (loading) => set({ isLoading: loading }),
  reset: () =>
    set({
      origin: null,
      destination: null,
      waypoints: [],
      route: null,
      isLoading: false,
    }),
}))

