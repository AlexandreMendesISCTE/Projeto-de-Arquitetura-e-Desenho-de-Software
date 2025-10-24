package pt.iscteiul.maprouteexplorer.ui;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.model.TransportMode;
import pt.iscteiul.maprouteexplorer.service.OSRMService;
import pt.iscteiul.maprouteexplorer.service.NominatimService;
import pt.iscteiul.maprouteexplorer.service.OkHttpClientService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Janela principal da aplicação Map Route Explorer.
 * 
 * Esta classe representa a interface gráfica principal da aplicação,
 * contendo o mapa interativo, controles para seleção de modo de transporte,
 * botões de ação e área de informações da rota.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class MainWindow extends JFrame implements PointSelectionListener {
    
    /** Painel do mapa */
    private MapPanel mapPanel;
    
    /** Campo de pesquisa de endereços */
    private JTextField searchField;
    
    /** Botão de pesquisa */
    private JButton searchButton;
    
    /** Combo box para seleção do modo de transporte */
    private JComboBox<TransportMode> transportModeCombo;
    
    /** Botão para calcular rota */
    private JButton calculateRouteButton;
    
    /** Botão para limpar seleção */
    private JButton clearButton;
    
    /** Área de texto para informações da rota */
    private JTextArea routeInfoArea;
    
    /** Serviço OSRM para cálculo de rotas */
    private OSRMService osrmService;
    
    /** Serviço Nominatim para geocodificação */
    private NominatimService nominatimService;
    
    /**
     * Construtor que inicializa a janela principal.
     */
    public MainWindow() {
        initializeServices();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Map Route Explorer - Sistema Interativo de Rotas");
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    /**
     * Inicializa os serviços necessários para a aplicação.
     */
    private void initializeServices() {
        OkHttpClientService httpClient = new OkHttpClientService();
        this.osrmService = new OSRMService(httpClient);
        this.nominatimService = new NominatimService(httpClient);
    }
    
    /**
     * Inicializa todos os componentes da interface.
     */
    private void initializeComponents() {
        // Painel do mapa
        mapPanel = new MapPanel();
        mapPanel.setPointSelectionListener(this);
        
        // Campo de pesquisa
        searchField = new JTextField(30);
        searchField.setToolTipText("Digite um endereço para pesquisar");
        
        // Botão de pesquisa
        searchButton = new JButton("Pesquisar");
        searchButton.setToolTipText("Pesquisar endereço no mapa");
        
        // Combo box para modo de transporte
        transportModeCombo = new JComboBox<>(TransportMode.values());
        transportModeCombo.setSelectedItem(TransportMode.DRIVING);
        transportModeCombo.setToolTipText("Selecione o modo de transporte");
        
        // Botão para calcular rota
        calculateRouteButton = new JButton("Calcular Rota");
        calculateRouteButton.setToolTipText("Calcular rota entre os pontos selecionados");
        calculateRouteButton.setEnabled(false);
        
        // Botão para limpar
        clearButton = new JButton("Limpar");
        clearButton.setToolTipText("Limpar seleção e rota atual");
        
        // Área de informações da rota
        routeInfoArea = new JTextArea(8, 30);
        routeInfoArea.setEditable(false);
        routeInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        routeInfoArea.setToolTipText("Informações sobre a rota calculada");
    }
    
    /**
     * Configura o layout da janela principal.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Painel superior com controles
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Painel central com mapa
        JScrollPane mapScrollPane = new JScrollPane(mapPanel);
        mapScrollPane.setPreferredSize(new Dimension(800, 600));
        add(mapScrollPane, BorderLayout.CENTER);
        
        // Painel lateral com informações
        JPanel sidePanel = createSidePanel();
        add(sidePanel, BorderLayout.EAST);
    }
    
    /**
     * Cria o painel superior com controles de pesquisa e modo de transporte.
     * 
     * @return painel superior
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Controles"));
        
        panel.add(new JLabel("Endereço:"));
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(Box.createHorizontalStrut(20));
        
        panel.add(new JLabel("Transporte:"));
        panel.add(transportModeCombo);
        panel.add(Box.createHorizontalStrut(20));
        
        panel.add(calculateRouteButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    /**
     * Cria o painel lateral com informações da rota.
     * 
     * @return painel lateral
     */
    private JPanel createSidePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Informações da Rota"));
        panel.setPreferredSize(new Dimension(300, 0));
        
        JScrollPane scrollPane = new JScrollPane(routeInfoArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Configura os event handlers para todos os componentes.
     */
    private void setupEventHandlers() {
        // Botão de pesquisa
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Enter no campo de pesquisa
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Botão de calcular rota
        calculateRouteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateRoute();
            }
        });
        
        // Botão de limpar
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });
    }
    
    /**
     * Realiza a pesquisa de endereço e centraliza o mapa.
     */
    private void performSearch() {
        String address = searchField.getText().trim();
        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite um endereço para pesquisar", 
                                        "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Location location = nominatimService.geocode(address);
            if (location != null) {
                mapPanel.setMapCenter(location);
                updateRouteInfo("Localização encontrada: " + location.getName());
            } else {
                JOptionPane.showMessageDialog(this, "Endereço não encontrado", 
                                            "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao pesquisar endereço: " + e.getMessage(), 
                                        "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Calcula a rota entre os pontos selecionados.
     */
    private void calculateRoute() {
        List<Location> selectedPoints = mapPanel.getSelectedPoints();
        
        if (selectedPoints.size() < 2) {
            JOptionPane.showMessageDialog(this, "Selecione pelo menos 2 pontos no mapa", 
                                        "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            TransportMode transportMode = (TransportMode) transportModeCombo.getSelectedItem();
            Route route = osrmService.calculateRoute(selectedPoints, transportMode);
            
            mapPanel.setRoute(route);
            displayRouteInfo(route);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao calcular rota: " + e.getMessage(), 
                                        "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Limpa todos os pontos selecionados e a rota atual.
     */
    private void clearAll() {
        mapPanel.clearSelectedPoints();
        mapPanel.clearRoute();
        calculateRouteButton.setEnabled(false);
        updateRouteInfo("Selecione pontos no mapa para calcular uma rota");
    }
    
    /**
     * Exibe as informações da rota calculada.
     * 
     * @param route rota calculada
     */
    private void displayRouteInfo(Route route) {
        StringBuilder info = new StringBuilder();
        info.append("ROTA CALCULADA\n");
        info.append("==============\n\n");
        info.append("Modo de transporte: ").append(route.getTransportMode().getDisplayName()).append("\n");
        info.append("Distância total: ").append(route.getFormattedDistance()).append("\n");
        info.append("Tempo estimado: ").append(route.getFormattedDuration()).append("\n");
        info.append("Número de pontos: ").append(route.getWaypointCount()).append("\n\n");
        
        if (!route.getInstructions().isEmpty()) {
            info.append("INSTRUÇÕES DE NAVEGAÇÃO\n");
            info.append("=======================\n");
            for (int i = 0; i < route.getInstructions().size(); i++) {
                info.append(String.format("%d. %s\n", i + 1, route.getInstructions().get(i)));
            }
        }
        
        updateRouteInfo(info.toString());
    }
    
    /**
     * Atualiza a área de informações da rota.
     * 
     * @param info informações a exibir
     */
    private void updateRouteInfo(String info) {
        routeInfoArea.setText(info);
        routeInfoArea.setCaretPosition(0);
    }
    
    /**
     * Chamado quando um ponto é selecionado no mapa.
     * 
     * @param location localização selecionada
     */
    @Override
    public void onPointSelected(Location location) {
        List<Location> selectedPoints = mapPanel.getSelectedPoints();
        calculateRouteButton.setEnabled(selectedPoints.size() >= 2);
        
        updateRouteInfo(String.format("Ponto selecionado: %.6f, %.6f\n" +
                                    "Total de pontos: %d\n\n" +
                                    "Clique em mais pontos ou use 'Calcular Rota'",
                                    location.getLatitude(), location.getLongitude(),
                                    selectedPoints.size()));
    }
    
    /**
     * Inicia a aplicação mostrando a janela principal.
     */
    public void start() {
        setVisible(true);
        updateRouteInfo("Bem-vindo ao Map Route Explorer!\n\n" +
                       "1. Clique no mapa para selecionar pontos\n" +
                       "2. Use 'Pesquisar' para encontrar endereços\n" +
                       "3. Selecione o modo de transporte\n" +
                       "4. Clique em 'Calcular Rota' para obter a rota");
    }
}


