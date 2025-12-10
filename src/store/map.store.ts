// Global map viewport state (center/zoom) and click-waiting flags
import { create } from 'zustand'
import { MapState } from '../types/store.types'
import { MAP_DEFAULT_CENTER, MAP_DEFAULT_ZOOM } from '../constants/api.constants'

export const useMapStore = create<MapState>((set) => ({
  center: MAP_DEFAULT_CENTER,
  zoom: MAP_DEFAULT_ZOOM,
  selectedPoint: null,
  waitingForInput: null, // 'origin' | 'destination' | 'waypoint' | null
  setCenter: (center) => set({ center }),
  setZoom: (zoom) => set({ zoom }),
  setSelectedPoint: (point) => set({ selectedPoint: point }),
  setWaitingForInput: (type) => set({ waitingForInput: type }),
  clearWaitingForInput: () => set({ waitingForInput: null }),
}))
