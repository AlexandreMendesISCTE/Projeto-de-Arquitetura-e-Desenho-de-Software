import { create } from 'zustand'
import { POI } from '../services/api/poi.service'

interface LoadedArea {
  minLat: number
  maxLat: number
  minLng: number
  maxLng: number
  pois: POI[]
  timestamp: number
}

interface POIState {
  enabled: boolean
  loadedAreas: LoadedArea[]
  cachedPOIs: Map<string, POI> // Map by POI ID for quick lookup
  lastRequestTime: number
  toggle: () => void
  setEnabled: (enabled: boolean) => void
  addPOIs: (pois: POI[], bounds: { minLat: number; maxLat: number; minLng: number; maxLng: number }) => void
  getPOIsInBounds: (bounds: { minLat: number; maxLat: number; minLng: number; maxLng: number }) => POI[]
  isAreaLoaded: (bounds: { minLat: number; maxLat: number; minLng: number; maxLng: number }) => boolean
  clearCache: () => void
}

export const usePOIStore = create<POIState>((set, get) => ({
  enabled: false,
  loadedAreas: [],
  cachedPOIs: new Map(),
  lastRequestTime: 0,
  toggle: () => set((state) => ({ enabled: !state.enabled })),
  setEnabled: (enabled) => set({ enabled }),
  addPOIs: (pois, bounds) => {
    const state = get()
    const newPOIs = new Map(state.cachedPOIs)
    
    // Add new POIs to cache
    pois.forEach(poi => {
      newPOIs.set(poi.id, poi)
    })
    
    // Add to loaded areas
    const newArea: LoadedArea = {
      ...bounds,
      pois,
      timestamp: Date.now()
    }
    
    set({
      cachedPOIs: newPOIs,
      loadedAreas: [...state.loadedAreas, newArea],
      lastRequestTime: Date.now()
    })
  },
  getPOIsInBounds: (bounds) => {
    const state = get()
    const result: POI[] = []
    const seenIds = new Set<string>()
    
    // Get POIs from cached areas that overlap with requested bounds
    state.loadedAreas.forEach(area => {
      // Check if areas overlap
      const overlaps = !(
        area.maxLat < bounds.minLat ||
        area.minLat > bounds.maxLat ||
        area.maxLng < bounds.minLng ||
        area.minLng > bounds.maxLng
      )
      
      if (overlaps) {
        area.pois.forEach(poi => {
          // Check if POI is within requested bounds
          if (
            poi.lat >= bounds.minLat &&
            poi.lat <= bounds.maxLat &&
            poi.lng >= bounds.minLng &&
            poi.lng <= bounds.maxLng &&
            !seenIds.has(poi.id)
          ) {
            result.push(poi)
            seenIds.add(poi.id)
          }
        })
      }
    })
    
    return result
  },
  isAreaLoaded: (bounds) => {
    const state = get()
    // Check if any loaded area covers the requested bounds (with some tolerance)
    return state.loadedAreas.some(area => {
      // Check if loaded area covers the requested bounds
      return (
        area.minLat <= bounds.minLat &&
        area.maxLat >= bounds.maxLat &&
        area.minLng <= bounds.minLng &&
        area.maxLng >= bounds.maxLng
      )
    })
  },
  clearCache: () => {
    set({
      loadedAreas: [],
      cachedPOIs: new Map(),
      lastRequestTime: 0
    })
  }
}))

