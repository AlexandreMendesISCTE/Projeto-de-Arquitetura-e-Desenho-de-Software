import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { useGeolocation } from '../hooks/useGeolocation'

// Use vi.hoisted() to create mocks before tests
const { mockGetCurrentPosition, mockNavigator } = vi.hoisted(() => {
  const getCurrentPosition = vi.fn()
  const geolocation = {
    getCurrentPosition,
  }
  return {
    mockGetCurrentPosition: getCurrentPosition,
    mockNavigator: {
      geolocation,
    },
  }
})

describe('geolocation.utils (useGeolocation hook)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Setup navigator.geolocation mock using vi.stubGlobal
    vi.stubGlobal('navigator', mockNavigator)
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  describe('getCurrentLocation', () => {
    it('gets current location successfully', async () => {
      const mockPosition = {
        coords: {
          latitude: 38.7223,
          longitude: -9.1393,
        },
      }

      mockGetCurrentPosition.mockImplementation((success: (pos: any) => void) => {
        success(mockPosition)
      })

      const { result } = renderHook(() => useGeolocation())

      result.current.getCurrentLocation()

      await waitFor(() => {
        expect(result.current.location).not.toBeNull()
      })

      expect(result.current.location).toEqual({
        lat: 38.7223,
        lng: -9.1393,
      })
      expect(result.current.isLoading).toBe(false)
      expect(result.current.error).toBeNull()
    })

    it('handles geolocation permission denied', async () => {
      const mockError = {
        code: 1, // PERMISSION_DENIED
        message: 'User denied Geolocation',
      }

      mockGetCurrentPosition.mockImplementation((_success: any, error: (err: any) => void) => {
        error(mockError)
      })

      const { result } = renderHook(() => useGeolocation())

      result.current.getCurrentLocation()

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.error).toBe('User denied Geolocation')
      expect(result.current.location).toBeNull()
    })

    it('handles geolocation timeout', async () => {
      const mockError = {
        code: 3, // TIMEOUT
        message: 'Geolocation timeout',
      }

      mockGetCurrentPosition.mockImplementation((_success: any, error: (err: any) => void) => {
        error(mockError)
      })

      const { result } = renderHook(() => useGeolocation())

      result.current.getCurrentLocation()

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.error).toBe('Geolocation timeout')
      expect(result.current.location).toBeNull()
    })

    it('handles geolocation position unavailable', async () => {
      const mockError = {
        code: 2, // POSITION_UNAVAILABLE
        message: 'Position unavailable',
      }

      mockGetCurrentPosition.mockImplementation((_success: any, error: (err: any) => void) => {
        error(mockError)
      })

      const { result } = renderHook(() => useGeolocation())

      result.current.getCurrentLocation()

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.error).toBe('Position unavailable')
      expect(result.current.location).toBeNull()
    })

    it('handles geolocation not supported', async () => {
      vi.unstubAllGlobals()
      vi.stubGlobal('navigator', {})

      const { result } = renderHook(() => useGeolocation())

      result.current.getCurrentLocation()

      await waitFor(() => {
        expect(result.current.error).toBe('Geolocation is not supported by your browser')
      })
      expect(result.current.location).toBeNull()
    })

    it('sets loading state during geolocation request', async () => {
      let resolvePosition: ((pos: GeolocationPosition) => void) | null = null

      mockGetCurrentPosition.mockImplementation((success: (pos: GeolocationPosition) => void) => {
        resolvePosition = success
      })

      const { result } = renderHook(() => useGeolocation())

      result.current.getCurrentLocation()

      await waitFor(() => {
        expect(result.current.isLoading).toBe(true)
      })

      // Resolve the position
      if (resolvePosition) {
        const mockPosition = {
          coords: {
            latitude: 38.7223,
            longitude: -9.1393,
            accuracy: 10,
            altitude: null,
            altitudeAccuracy: null,
            heading: null,
            speed: null,
          },
          timestamp: Date.now(),
        } as GeolocationPosition
        ;(resolvePosition as (pos: GeolocationPosition) => void)(mockPosition)
      }

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })
    })

    it('uses correct geolocation options', () => {
      mockGetCurrentPosition.mockImplementation((success: any, _error: any, options: any) => {
        expect(options).toMatchObject({
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 0,
        })
        success({
          coords: {
            latitude: 38.7223,
            longitude: -9.1393,
          },
        })
      })

      const { result } = renderHook(() => useGeolocation())

      result.current.getCurrentLocation()

      expect(mockGetCurrentPosition).toHaveBeenCalledWith(
        expect.any(Function),
        expect.any(Function),
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 0,
        }
      )
    })
  })
})
