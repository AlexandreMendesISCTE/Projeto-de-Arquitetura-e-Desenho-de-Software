/**
 * Serviço de integração com n8n Workflow
 *
 * Este módulo gere a comunicação com o workflow n8n hospedado no servidor.
 * Envia mensagens do utilizador e contexto da aplicação para processamento.
 *
 * Endpoint esperado no n8n:
 * - POST ${VITE_N8N_WEBHOOK_URL} (configurado via variável de ambiente)
 * O workflow n8n deve processar:
 * - Mensagens do utilizador
 * - Contexto da rota atual
 * - Definição de localizações (origem, destino, paragens)
 */

import axios from 'axios'

/**
 * URL base do n8n workflow
 * Configurável via variável de ambiente
 */
const N8N_WEBHOOK_URL = import.meta.env.VITE_N8N_WEBHOOK_URL || '/n8n/chat'

/**
 * Timeout para requisições ao n8n (30 segundos)
 */
const N8N_TIMEOUT = 30000

/**
 * Interface para o contexto da rota atual
 */
export interface RouteContext {
  origin: {
    name: string
    lat: number
    lng: number
  } | null
  destination: {
    name: string
    lat: number
    lng: number
  } | null
  waypoints: Array<{
    name: string
    lat: number
    lng: number
  }>
}

/**
 * Interface para o payload enviado ao n8n
 */
interface N8NPayload {
  message: string
  currentRoute: RouteContext
  waitingForInput: 'origin' | 'destination' | 'waypoint' | null
  timestamp: string
  userLocation?: {
    name: string
    lat: number
    lng: number
  } | null
  useCurrentLocationAsOrigin?: boolean
}

/**
 * Interface para a resposta do n8n
 */
export interface N8NResponse {
  message: string
  action?: 'set_origin' | 'set_destination' | 'add_waypoint' | 'add_waypoints' | 'set_route' | 'clear_route' | null
  location?: {
    name: string
    lat: number
    lng: number
  }
  origin?: {
    name: string
    lat: number
    lng: number
  }
  destination?: {
    name: string
    lat: number
    lng: number
  }
  waypoints?: Array<{
    name: string
    lat: number
    lng: number
  }>
  suggestions?: Array<{
    name: string
    lat: number
    lng: number
  }>
}

/**
 * Envia mensagem do utilizador para o workflow n8n
 *
 * @param context - Contexto atual da aplicação incluindo mensagem e estado da rota
 * @returns Promise<N8NResponse> - Resposta completa do n8n
 * @throws Error - Se ocorrer erro na comunicação com n8n
 *
 * @example
 * ```typescript
 * const response = await sendMessageToN8N({
 *   message: "Quero ir de Lisboa ao Porto",
 *   currentRoute: { origin: null, destination: null, waypoints: [] },
 *   waitingForInput: null
 * })
 * ```
 */
export const sendMessageToN8N = async (context: {
  message: string
  currentRoute: RouteContext
  waitingForInput: 'origin' | 'destination' | 'waypoint' | null
  userLocation?: {
    name: string
    lat: number
    lng: number
  } | null
  useCurrentLocationAsOrigin?: boolean
}): Promise<N8NResponse> => {
  try {
    // Preparar payload para o n8n
    const payload: N8NPayload = {
      message: context.message,
      currentRoute: context.currentRoute,
      waitingForInput: context.waitingForInput,
      timestamp: new Date().toISOString(),
      userLocation: context.userLocation,
      useCurrentLocationAsOrigin: context.useCurrentLocationAsOrigin,
    }

    console.log('Enviando mensagem para n8n:', payload)

    // Fazer requisição POST para o webhook do n8n
    const response = await axios.post<N8NResponse>(N8N_WEBHOOK_URL, payload, {
      timeout: N8N_TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    console.log('Resposta do n8n:', response.data)

    // Verificar se a resposta contém mensagem
    if (response.data && response.data.message) {
      return response.data
    }

    // Fallback se a resposta não tiver o formato esperado
    return {
      message: 'Mensagem processada com sucesso.',
      action: null,
    }
  } catch (error) {
    console.error('Erro ao comunicar com n8n:', error)

    // Tratamento específico de erros
    if (axios.isAxiosError(error)) {
      if (error.code === 'ECONNABORTED') {
        throw new Error('Timeout ao comunicar com o servidor. Por favor, tente novamente.')
      }

      if (error.response) {
        // Erro de resposta do servidor
        const status = error.response.status
        if (status === 404) {
          throw new Error('Serviço de chat não encontrado. Verifique a configuração do n8n.')
        } else if (status >= 500) {
          throw new Error('Erro no servidor. Por favor, tente novamente mais tarde.')
        }
      } else if (error.request) {
        // Erro de rede
        throw new Error('Não foi possível conectar ao servidor. Verifique a sua ligação.')
      }
    }

    // Erro genérico
    throw new Error('Erro ao processar mensagem. Por favor, tente novamente.')
  }
}

/**
 * Valida se o webhook do n8n está acessível
 *
 * @returns Promise<boolean> - true se o webhook estiver disponível
 */
export const checkN8NHealth = async (): Promise<boolean> => {
  try {
    // Tentar fazer uma requisição simples ao webhook
    // O n8n pode ter um endpoint de health check ou podemos usar OPTIONS
    const response = await axios.options(N8N_WEBHOOK_URL, {
      timeout: 5000,
    })

    return response.status < 500
  } catch (error) {
    console.warn('n8n webhook não está acessível:', error)
    return false
  }
}

/**
 * Envia localização selecionada pelo utilizador para o n8n
 * Útil quando o utilizador clica no mapa após o bot solicitar uma localização
 *
 * @param location - Localização selecionada
 * @param type - Tipo de localização (origin, destination, waypoint)
 * @returns Promise<string> - Confirmação do bot
 */
export const sendLocationToN8N = async (
  location: { name: string; lat: number; lng: number },
  type: 'origin' | 'destination' | 'waypoint'
): Promise<string> => {
  try {
    const payload = {
      action: `set_${type}`,
      location: location,
      timestamp: new Date().toISOString(),
    }

    const response = await axios.post<N8NResponse>(N8N_WEBHOOK_URL, payload, {
      timeout: N8N_TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    return (
      response.data?.message ||
      `${type === 'origin' ? 'Origem' : type === 'destination' ? 'Destino' : 'Paragem'} definida com sucesso!`
    )
  } catch (error) {
    console.error('Erro ao enviar localização para n8n:', error)
    throw new Error('Erro ao processar localização.')
  }
}
