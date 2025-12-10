import { describe, it, expect, beforeEach } from 'vitest'
import { usePOIStore } from '../store/poi.store'
import { POI } from '../services/api/poi.service'

const resetPOIStore = () => {
  usePOIStore.setState({
    enabled: false,
    loadedAreas: [],
    cachedPOIs: new Map(),
    lastRequestTime: 0,
  })
}

const sampleBounds = { minLat: 0, maxLat: 2, minLng: 0, maxLng: 2 }

describe('poi.store', () => {
  beforeEach(() => {
    resetPOIStore()
  })

  it('toggles enable flag', () => {
    expect(usePOIStore.getState().enabled).toBe(false)
    usePOIStore.getState().toggle()
    expect(usePOIStore.getState().enabled).toBe(true)
  })

  it('caches POIs and marks areas as loaded', () => {
    const pois: POI[] = [
      { id: '1', name: 'Cafe', type: 'node', lat: 1, lng: 1, category: 'cafe' },
      { id: '2', name: 'Fuel', type: 'node', lat: 1.5, lng: 1.5, category: 'fuel' },
    ]

    usePOIStore.getState().addPOIs(pois, sampleBounds)

    const state = usePOIStore.getState()
    expect(state.cachedPOIs.size).toBe(2)
    expect(state.loadedAreas).toHaveLength(1)
    expect(state.isAreaLoaded(sampleBounds)).toBe(true)
  })

  it('returns POIs in requested bounds without duplicates', () => {
    const pois: POI[] = [
      { id: '1', name: 'Cafe', type: 'node', lat: 1, lng: 1, category: 'cafe' },
      { id: '1', name: 'Cafe Duplicate', type: 'node', lat: 1, lng: 1, category: 'cafe' },
    ]

    usePOIStore.getState().addPOIs(pois, sampleBounds)
    const result = usePOIStore.getState().getPOIsInBounds(sampleBounds)

    expect(result).toHaveLength(1)
    expect(result[0].name).toBe('Cafe')
  })
})
