import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'
import {
  sendMessageToN8N,
  checkN8NHealth,
  sendLocationToN8N,
  type N8NResponse,
} from '../services/api/n8n.service'

// Mock axios methods
const mockPost = vi.fn()
const mockOptions = vi.fn()
const mockIsAxiosError = vi.fn()

describe('n8n.service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Mock axios methods
    vi.spyOn(axios, 'post').mockImplementation(mockPost)
    vi.spyOn(axios, 'options').mockImplementation(mockOptions)
    vi.spyOn(axios, 'isAxiosError').mockImplementation(mockIsAxiosError)
    // Mock console methods to avoid noise in tests
    vi.spyOn(console, 'log').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
    vi.spyOn(console, 'warn').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('sendMessageToN8N', () => {
    const mockContext = {
      message: 'Quero ir de Lisboa ao Porto',
      currentRoute: {
        origin: null,
        destination: null,
        waypoints: [],
      },
      waitingForInput: null,
    }

    it('sends correct payload to n8n webhook and returns response', async () => {
      const mockResponse: N8NResponse = {
        message: 'Perfeito! Vou ajudá-lo a definir a rota de Lisboa ao Porto.',
        action: null,
      }

      mockPost.mockResolvedValueOnce({
        data: mockResponse,
        status: 200,
      })

      const result = await sendMessageToN8N(mockContext)

      expect(mockPost).toHaveBeenCalledTimes(1)
      const callArgs = mockPost.mock.calls[0]
      expect(callArgs[0]).toBe('/n8n/chat') // Default URL when env var not set
      expect(callArgs[1]).toMatchObject({
        message: mockContext.message,
        currentRoute: mockContext.currentRoute,
        waitingForInput: mockContext.waitingForInput,
        timestamp: expect.any(String),
      })
      expect(callArgs[2]).toMatchObject({
        timeout: 30000,
        headers: {
          'Content-Type': 'application/json',
        },
      })
      expect(result).toEqual(mockResponse)
    })

    it('includes userLocation and useCurrentLocationAsOrigin when provided', async () => {
      const contextWithLocation = {
        ...mockContext,
        userLocation: { name: 'Lisboa', lat: 38.7223, lng: -9.1393 },
        useCurrentLocationAsOrigin: true,
      }

      mockPost.mockResolvedValueOnce({
        data: { message: 'OK', action: null },
        status: 200,
      })

      await sendMessageToN8N(contextWithLocation)

      const payload = mockPost.mock.calls[0][1]
      expect(payload.userLocation).toEqual(contextWithLocation.userLocation)
      expect(payload.useCurrentLocationAsOrigin).toBe(true)
    })

    it('handles response with route action (set_route)', async () => {
      const mockResponse: N8NResponse = {
        message: 'Rota definida com sucesso!',
        action: 'set_route',
        origin: { name: 'Lisboa', lat: 38.7223, lng: -9.1393 },
        destination: { name: 'Porto', lat: 41.1579, lng: -8.6291 },
        waypoints: [{ name: 'Coimbra', lat: 40.2033, lng: -8.4103 }],
      }

      mockPost.mockResolvedValueOnce({
        data: mockResponse,
        status: 200,
      })

      const result = await sendMessageToN8N(mockContext)

      expect(result.action).toBe('set_route')
      expect(result.origin).toEqual(mockResponse.origin)
      expect(result.destination).toEqual(mockResponse.destination)
      expect(result.waypoints).toHaveLength(1)
    })

    it('returns fallback message when response has no message field', async () => {
      mockPost.mockResolvedValueOnce({
        data: { action: null }, // Missing message field
        status: 200,
      })

      const result = await sendMessageToN8N(mockContext)

      expect(result.message).toBe('Mensagem processada com sucesso.')
      expect(result.action).toBeNull()
    })

    it('handles timeout errors correctly', async () => {
      const timeoutError = {
        code: 'ECONNABORTED',
        isAxiosError: true,
      }

      mockPost.mockRejectedValueOnce(timeoutError)
      mockIsAxiosError.mockReturnValue(true)

      await expect(sendMessageToN8N(mockContext)).rejects.toThrow(
        'Timeout ao comunicar com o servidor. Por favor, tente novamente.'
      )
    })

    it('handles 404 errors correctly', async () => {
      const notFoundError = {
        response: { status: 404 },
        isAxiosError: true,
      }

      mockPost.mockRejectedValueOnce(notFoundError)
      mockIsAxiosError.mockReturnValue(true)

      await expect(sendMessageToN8N(mockContext)).rejects.toThrow(
        'Serviço de chat não encontrado. Verifique a configuração do n8n.'
      )
    })

    it('handles 500+ server errors correctly', async () => {
      const serverError = {
        response: { status: 500 },
        isAxiosError: true,
      }

      mockPost.mockRejectedValueOnce(serverError)
      mockIsAxiosError.mockReturnValue(true)

      await expect(sendMessageToN8N(mockContext)).rejects.toThrow(
        'Erro no servidor. Por favor, tente novamente mais tarde.'
      )
    })

    it('handles network errors correctly', async () => {
      const networkError = {
        request: {},
        isAxiosError: true,
      }

      mockPost.mockRejectedValueOnce(networkError)
      mockIsAxiosError.mockReturnValue(true)

      await expect(sendMessageToN8N(mockContext)).rejects.toThrow(
        'Não foi possível conectar ao servidor. Verifique a sua ligação.'
      )
    })

    it('handles generic errors correctly', async () => {
      const genericError = new Error('Unknown error')

      mockPost.mockRejectedValueOnce(genericError)
      mockIsAxiosError.mockReturnValue(false)

      await expect(sendMessageToN8N(mockContext)).rejects.toThrow(
        'Erro ao processar mensagem. Por favor, tente novamente.'
      )
    })
  })

  describe('checkN8NHealth', () => {
    it('returns true when webhook is accessible', async () => {
      mockOptions.mockResolvedValueOnce({
        status: 200,
      })

      const result = await checkN8NHealth()

      expect(result).toBe(true)
      expect(mockOptions).toHaveBeenCalledWith('/n8n/chat', {
        timeout: 5000,
      })
    })

    it('returns false when webhook is not accessible', async () => {
      mockOptions.mockRejectedValueOnce(new Error('Network error'))

      const result = await checkN8NHealth()

      expect(result).toBe(false)
    })

    it('returns false when webhook returns 500+ status', async () => {
      mockOptions.mockResolvedValueOnce({
        status: 503,
      })

      const result = await checkN8NHealth()

      expect(result).toBe(false)
    })
  })

  describe('sendLocationToN8N', () => {
    const mockLocation = { name: 'Lisboa', lat: 38.7223, lng: -9.1393 }

    it('sends location with set_origin action', async () => {
      mockPost.mockResolvedValueOnce({
        data: { message: 'Origem definida!', action: null },
        status: 200,
      })

      const result = await sendLocationToN8N(mockLocation, 'origin')

      expect(mockPost).toHaveBeenCalledWith(
        '/n8n/chat',
        expect.objectContaining({
          action: 'set_origin',
          location: mockLocation,
          timestamp: expect.any(String),
        }),
        expect.objectContaining({
          timeout: 30000,
          headers: {
            'Content-Type': 'application/json',
          },
        })
      )
      expect(result).toBe('Origem definida!')
    })

    it('sends location with set_destination action', async () => {
      mockPost.mockResolvedValueOnce({
        data: { message: 'Destino definido!', action: null },
        status: 200,
      })

      const result = await sendLocationToN8N(mockLocation, 'destination')

      const payload = mockPost.mock.calls[0][1]
      expect(payload.action).toBe('set_destination')
      expect(result).toBe('Destino definido!')
    })

    it('sends location with set_waypoint action', async () => {
      mockPost.mockResolvedValueOnce({
        data: { message: 'Paragem definida!', action: null },
        status: 200,
      })

      const result = await sendLocationToN8N(mockLocation, 'waypoint')

      const payload = mockPost.mock.calls[0][1]
      expect(payload.action).toBe('set_waypoint')
      expect(result).toBe('Paragem definida!')
    })

    it('returns default message when response has no message', async () => {
      mockPost.mockResolvedValueOnce({
        data: { action: null },
        status: 200,
      })

      const result = await sendLocationToN8N(mockLocation, 'origin')

      expect(result).toBe('Origem definida com sucesso!')
    })

    it('handles errors correctly', async () => {
      mockPost.mockRejectedValueOnce(new Error('Network error'))

      await expect(sendLocationToN8N(mockLocation, 'origin')).rejects.toThrow(
        'Erro ao processar localização.'
      )
    })
  })
})
