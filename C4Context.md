```mermaid
C4Context
    title Diagrama de Contexto - Map Route Explorer

    Person(user, "Utilizador", "Pessoa que quer planear rotas e explorar mapas")
    Person(mobile_user, "Utilizador Mobile", "Acede via smartphone ou tablet")
    
    System(mre, "Map Route Explorer", "Aplicacao web para planeamento de rotas com multiplos modos de transporte")
    
    System_Ext(osm, "OpenStreetMap", "Fornece tiles de mapa e dados geograficos")
    System_Ext(nominatim, "Nominatim API", "Servico de geocodificacao e pesquisa de enderecos")
    System_Ext(osrm, "OSRM API", "Calculo de rotas para carro bicicleta e a pe")
    System_Ext(google_routes, "Google Routes API", "Rotas de transporte publico com horarios")
    System_Ext(n8n, "n8n Webhook", "Chatbot assistente para ajuda com rotas")
    System_Ext(overpass, "Overpass API", "Consulta de POIs do OpenStreetMap")
    
    Rel(user, mre, "Utiliza", "HTTPS")
    Rel(mobile_user, mre, "Utiliza", "HTTPS")
    
    Rel(mre, osm, "Carrega tiles", "HTTPS")
    Rel(mre, nominatim, "Pesquisa enderecos", "HTTPS")
    Rel(mre, osrm, "Calcula rotas", "HTTPS")
    Rel(mre, google_routes, "Rotas transit", "HTTPS")
    Rel(mre, n8n, "Envia mensagens", "HTTPS")
    Rel(mre, overpass, "Consulta POIs", "HTTPS")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```