package pt.iscteiul.maprouteexplorer;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import pt.iscteiul.maprouteexplorer.ui.MainWindow;
import pt.iscteiul.maprouteexplorer.util.ConfigManager;

/**
 * Classe principal da aplicação Map Route Explorer.
 * 
 * Esta aplicação permite aos utilizadores explorar mapas baseados em dados do OpenStreetMap,
 * traçar rotas entre pontos de interesse e obter informações relevantes sobre o trajeto.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class Main {
    
    /** Logger para esta classe */
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    /**
     * Método principal que inicia a aplicação.
     * 
     * @param args argumentos da linha de comandos (não utilizados)
     */
    public static void main(String[] args) {
        logger.info("Map Route Explorer - Sistema Interativo de Rotas e Exploração de Locais");
        logger.info("Iniciando aplicação...");
        
        try {
            // Carregar configurações
            ConfigManager.loadConfig();
            logger.info("Configurações carregadas com sucesso");
            
            // Configurar look and feel do sistema
            setupLookAndFeel();
            
            // Inicializar interface gráfica na thread de eventos
            SwingUtilities.invokeLater(() -> {
                try {
                    MainWindow mainWindow = new MainWindow();
                    mainWindow.start();
                    logger.info("Interface gráfica inicializada com sucesso");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Erro ao inicializar interface gráfica", e);
                    System.err.println("Erro ao inicializar a aplicação: " + e.getMessage());
                    System.exit(1);
                }
            });
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro fatal ao inicializar a aplicação", e);
            System.err.println("Erro fatal: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Configura o look and feel do sistema para uma aparência nativa.
     */
    private static void setupLookAndFeel() {
        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName());
            logger.info("Look and feel configurado: " + 
                       javax.swing.UIManager.getLookAndFeel().getName());
        } catch (Exception e) {
            logger.warning("Não foi possível configurar o look and feel: " + e.getMessage());
            // Continuar com o look and feel padrão
        }
    }
}


