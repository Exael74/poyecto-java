import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Random;

public class MaxwellContainer extends JFrame {
    // Constantes
    private final int TOP_MARGIN = 50;
    private final int LEFT_MARGIN = 50;
    private final int BOTTOM_MARGIN = 50;
    private final int RIGHT_MARGIN = 50;
    private final double SPEED_THRESHOLD = 5.0;
    private final int BLACK_HOLE_RADIUS = 15;
    
    // Colores de partículas
    private Color fastParticleColor = Color.RED;
    private Color slowParticleColor = Color.BLUE;
    
    // Dimensiones del contenedor
    private int containerWidth = 700;
    private int containerHeight = 500;
    
    // Lists para almacenar objetos
    private final ArrayList<Particle> leftChamber = new ArrayList<>();
    private final ArrayList<Particle> rightChamber = new ArrayList<>();
    private final ArrayList<Demon> demons = new ArrayList<>();
    private final ArrayList<Hole> blackHoles = new ArrayList<>();
    
    // Componentes de UI
    private Canvas simulationPanel;
    private Timer timer;
    private JDialog statusWindow;
    private JTextField widthField;
    private JTextField heightField;
    
    // Estado de la última acción
    private boolean lastActionSuccessful = true;
    
    // Modo interactivo flags
    private boolean addRedParticleMode = false;
    private boolean addBlueParticleMode = false;
    private boolean removeParticleMode = false;
    private boolean addBlackHoleMode = false;
    private boolean removeBlackHoleMode = false;
    private boolean isSimulatorVisible = true;
    
    // Variables de estado
    private String statusMessage = "";
    private final Random random = new Random();
    
    public MaxwellContainer() {
        setTitle("Maxwell's Demon Simulator");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(containerWidth + LEFT_MARGIN + RIGHT_MARGIN, 
                containerHeight + TOP_MARGIN + BOTTOM_MARGIN + 130);
        setLayout(new BorderLayout());
        
        // Listener de ventana para manejar cierre
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        
        // Inicializar componentes UI
        setupUI();
        
        // Inicializar moléculas y añadir un demonio
        initializeMolecules();
        addDemon();
        
        // Configurar timer para la animación
        timer = new Timer(50, (ActionEvent e) -> {
            updateSimulation();
            if (isSimulatorVisible) {
                simulationPanel.repaint();
            }
        });
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Verifica si la última acción realizada fue exitosa
     * @return true si la última acción se completó correctamente, false en caso contrario
     */
    public boolean ok() {
        return lastActionSuccessful;
    }
    
    private void setupUI() {
        // Panel superior para controles de dimensiones y colores
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Panel para dimensiones
        JPanel dimensionPanel = new JPanel();
        JLabel widthLabel = new JLabel("Container Width:");
        widthField = new JTextField(String.valueOf(containerWidth), 5);
        JLabel heightLabel = new JLabel("Container Height:");
        heightField = new JTextField(String.valueOf(containerHeight), 5);
        JButton applyDimensionsButton = new JButton("Create Container");
        
        dimensionPanel.add(widthLabel);
        dimensionPanel.add(widthField);
        dimensionPanel.add(heightLabel);
        dimensionPanel.add(heightField);
        dimensionPanel.add(applyDimensionsButton);
        
        // Panel para selección de colores
        JPanel colorPanel = new JPanel();
        JButton fastColorButton = new JButton("Fast Particles Color");
        fastColorButton.setBackground(fastParticleColor);
        fastColorButton.setForeground(getContrastColor(fastParticleColor));
        
        JButton slowColorButton = new JButton("Slow Particles Color");
        slowColorButton.setBackground(slowParticleColor);
        slowColorButton.setForeground(getContrastColor(slowParticleColor));
        
        colorPanel.add(fastColorButton);
        colorPanel.add(slowColorButton);
        
        // Configurar color picker para partículas rápidas
        fastColorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(
                this, "Choose Fast Particles Color", fastParticleColor);
                
            if (selectedColor != null && !selectedColor.equals(slowParticleColor)) {
                fastParticleColor = selectedColor;
                fastColorButton.setBackground(fastParticleColor);
                fastColorButton.setForeground(getContrastColor(fastParticleColor));
                updateParticleColors();
                lastActionSuccessful = true;
                simulationPanel.repaint();
            } else if (selectedColor != null) {
                JOptionPane.showMessageDialog(this, 
                    "Fast and slow particles cannot have the same color.", 
                    "Invalid Color Selection", JOptionPane.WARNING_MESSAGE);
                lastActionSuccessful = false;
            }
        });
        
        // Configurar color picker para partículas lentas
        slowColorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(
                this, "Choose Slow Particles Color", slowParticleColor);
                
            if (selectedColor != null && !selectedColor.equals(fastParticleColor)) {
                slowParticleColor = selectedColor;
                slowColorButton.setBackground(slowParticleColor);
                slowColorButton.setForeground(getContrastColor(slowParticleColor));
                updateParticleColors();
                lastActionSuccessful = true;
                simulationPanel.repaint();
            } else if (selectedColor != null) {
                JOptionPane.showMessageDialog(this, 
                    "Fast and slow particles cannot have the same color.", 
                    "Invalid Color Selection", JOptionPane.WARNING_MESSAGE);
                lastActionSuccessful = false;
            }
        });
        
        // Organizar paneles superiores
        JPanel topControlPanel = new JPanel(new BorderLayout());
        topControlPanel.add(dimensionPanel, BorderLayout.NORTH);
        topControlPanel.add(colorPanel, BorderLayout.SOUTH);
        add(topControlPanel, BorderLayout.NORTH);
        
        // Crear panel de simulación e inicializar formas gráficas
        simulationPanel = new Canvas(this);
        simulationPanel.initShapes(); // Inicializa el rectángulo del contenedor y otras formas base
        add(simulationPanel, BorderLayout.CENTER);
        
        // Crear ventana de estado (inicialmente no visible)
        setupStatusWindow();
        
        // Crear panel de controles inferior
        setupControlPanel(applyDimensionsButton);
        
        // Añadir listener para el mouse en el panel de simulación
        setupMouseListener();
    }
    
    // Método auxiliar para obtener un color de texto contrastante
    private Color getContrastColor(Color bg) {
        int luminance = (int) (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue());
        return luminance > 128 ? Color.BLACK : Color.WHITE;
    }
    
    // Actualizar colores de todas las partículas existentes
    private void updateParticleColors() {
        try {
            for (Particle p : leftChamber) {
                p.setBaseColor(p.getSpeed() >= SPEED_THRESHOLD ? fastParticleColor : slowParticleColor);
            }
            for (Particle p : rightChamber) {
                p.setBaseColor(p.getSpeed() >= SPEED_THRESHOLD ? fastParticleColor : slowParticleColor);
            }
            lastActionSuccessful = true;
        } catch (Exception e) {
            lastActionSuccessful = false;
            setStatusMessage("Failed to update particle colors: " + e.getMessage());
        }
    }
    
    private void setupStatusWindow() {
        statusWindow = new JDialog(this, "Maxwell's Demon Simulator Status");
        statusWindow.setSize(400, 200);
        statusWindow.setLocationRelativeTo(null);
        JPanel statusPanel = new JPanel(new BorderLayout());
        JTextArea statusText = new JTextArea();
        statusText.setEditable(false);
        statusPanel.add(new JScrollPane(statusText), BorderLayout.CENTER);
        
        // Botones para la ventana de estado
        JPanel statusButtonPanel = new JPanel();
        JButton showSimButton = new JButton("Mostrar Simulador");
        JButton exitButton = new JButton("Terminar Simulador");
        statusButtonPanel.add(showSimButton);
        statusButtonPanel.add(exitButton);
        statusPanel.add(statusButtonPanel, BorderLayout.SOUTH);
        statusWindow.add(statusPanel);
        
        // Timer para actualizar la ventana de estado
        Timer statusTimer = new Timer(1000, e -> {
            if (!isSimulatorVisible) {
                int leftCount = leftChamber.size();
                int rightCount = rightChamber.size();
                int redCount = countRedMolecules();
                int blueCount = countBlueMolecules();
                
                statusText.setText(String.format(
                    "Simulador ejecutándose en segundo plano\n" +
                    "-----------------------------------\n" +
                    "Partículas Izquierda: %d\n" +
                    "Partículas Derecha: %d\n" +
                    "Partículas Rápidas: %d\n" +
                    "Partículas Lentas: %d\n" +
                    "Demonios: %d\n" +
                    "Agujeros Negros: %d\n",
                    leftCount, rightCount, redCount, blueCount,
                    demons.size(), blackHoles.size()
                ));
            }
        });
        statusTimer.start();
        
        showSimButton.addActionListener(e -> showSimulator());
        exitButton.addActionListener(e -> confirmExit());
    }
    
    private void setupControlPanel(JButton applyDimensionsButton) {
        JPanel controlPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        JButton startButton = new JButton("Start Simulation");
        JButton resetButton = new JButton("Reset");
        JButton addDemonButton = new JButton("Add Demon");
        JButton removeDemonButton = new JButton("Remove Demon");
        JButton addRedParticleButton = new JButton("Add Fast Particle");
        JButton addBlueParticleButton = new JButton("Add Slow Particle");
        JButton removeParticleButton = new JButton("Remove Particle");
        JButton addBlackHoleButton = new JButton("Add Black Hole");
        JButton removeBlackHoleButton = new JButton("Remove Black Hole");
        JButton toggleVisibilityButton = new JButton("Ocultar Simulador");
        JButton exitSimulatorButton = new JButton("Terminar Simulador");
        JButton cancelButton = new JButton("Cancel Action");
        
        // Primera fila de controles
        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(toggleVisibilityButton);
        controlPanel.add(exitSimulatorButton);
        
        // Segunda fila de controles
        controlPanel.add(addDemonButton);
        controlPanel.add(removeDemonButton);
        controlPanel.add(addRedParticleButton);
        controlPanel.add(addBlueParticleButton);
        
        // Tercera fila de controles
        controlPanel.add(removeParticleButton);
        controlPanel.add(addBlackHoleButton);
        controlPanel.add(removeBlackHoleButton);
        controlPanel.add(cancelButton);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // Configurar listener para botón de inicio/pausa
        startButton.addActionListener(e -> {
            try {
                if (timer.isRunning()) {
                    timer.stop();
                    startButton.setText("Start Simulation");
                } else {
                    timer.start();
                    startButton.setText("Pause Simulation");
                }
                lastActionSuccessful = true;
            } catch (Exception ex) {
                lastActionSuccessful = false;
                setStatusMessage("Failed to toggle simulation: " + ex.getMessage());
            }
        });
        
        // Configurar listener para botón de reset
        resetButton.addActionListener(e -> {
            try {
                timer.stop();
                startButton.setText("Start Simulation");
                initializeMolecules();
                demons.clear();
                blackHoles.clear();
                addDemon();
                simulationPanel.repaint();
                lastActionSuccessful = true;
                setStatusMessage("Simulation reset successfully");
            } catch (Exception ex) {
                lastActionSuccessful = false;
                setStatusMessage("Failed to reset simulation: " + ex.getMessage());
            }
        });
        
        // Configurar listener para el botón de aplicar dimensiones
        setupApplyDimensionsButton(applyDimensionsButton);
        
        // Configurar listener para toggle de visibilidad
        toggleVisibilityButton.addActionListener(e -> {
            try {
                if (isSimulatorVisible) {
                    hideSimulator();
                    toggleVisibilityButton.setText("Mostrar Simulador");
                } else {
                    showSimulator();
                    toggleVisibilityButton.setText("Ocultar Simulador");
                }
                lastActionSuccessful = true;
            } catch (Exception ex) {
                lastActionSuccessful = false;
                setStatusMessage("Failed to toggle visibility: " + ex.getMessage());
            }
        });
        
        // Configurar listener para botón de salida
        exitSimulatorButton.addActionListener(e -> confirmExit());
        
        // Configurar listener para añadir demonio
        addDemonButton.addActionListener(e -> {
            addDemon();
            simulationPanel.repaint();
        });
        
        // Configurar listener para eliminar demonio
        removeDemonButton.addActionListener(e -> {
            try {
                if (!demons.isEmpty()) {
                    demons.remove(demons.size() - 1);
                    simulationPanel.repaint();
                    lastActionSuccessful = true;
                    setStatusMessage("Demon removed");
                } else {
                    lastActionSuccessful = false;
                    setStatusMessage("No demons to remove");
                }
            } catch (Exception ex) {
                lastActionSuccessful = false;
                setStatusMessage("Failed to remove demon: " + ex.getMessage());
            }
        });
        
        // Configurar listeners para manipulación de partículas
        addRedParticleButton.addActionListener(e -> {
            resetInteractionModes();
            addRedParticleMode = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            setStatusMessage("Click where you want to add a fast particle");
            lastActionSuccessful = true;
        });
        
        addBlueParticleButton.addActionListener(e -> {
            resetInteractionModes();
            addBlueParticleMode = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            setStatusMessage("Click where you want to add a slow particle");
            lastActionSuccessful = true;
        });
        
        removeParticleButton.addActionListener(e -> {
            resetInteractionModes();
            removeParticleMode = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setStatusMessage("Click on a particle to remove it");
            lastActionSuccessful = true;
        });
        
        // Configurar listeners para manipulación de agujeros negros
        addBlackHoleButton.addActionListener(e -> {
            resetInteractionModes();
            addBlackHoleMode = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            setStatusMessage("Click where you want to add a black hole");
            lastActionSuccessful = true;
        });
        
        removeBlackHoleButton.addActionListener(e -> {
            resetInteractionModes();
            removeBlackHoleMode = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setStatusMessage("Click on a black hole to remove it");
            lastActionSuccessful = true;
        });
        
        cancelButton.addActionListener(e -> {
            resetInteractionModes();
            setCursor(Cursor.getDefaultCursor());
            setStatusMessage("");
            lastActionSuccessful = true;
        });
    }
    
    private void setupApplyDimensionsButton(JButton applyDimensionsButton) {
        applyDimensionsButton.addActionListener(e -> {
            try {
                // Obtener nuevas dimensiones desde los campos de texto
                int newWidth = Integer.parseInt(widthField.getText());
                int newHeight = Integer.parseInt(heightField.getText());
                
                // Validar dimensiones (asegurar que sean razonables)
                if (newWidth < 200) newWidth = 200;
                if (newHeight < 150) newHeight = 150;
                
                // Actualizar dimensiones del contenedor
                containerWidth = newWidth;
                containerHeight = newHeight;
                
                // Actualizar tamaño del rectángulo del contenedor en el canvas
                simulationPanel.updateContainerSize(containerWidth, containerHeight);
                
                // Actualizar tamaño del frame para acomodar el nuevo tamaño del contenedor
                setSize(containerWidth + LEFT_MARGIN + RIGHT_MARGIN, 
                       containerHeight + TOP_MARGIN + BOTTOM_MARGIN + 130);
                
                // Detener la simulación y reiniciar
                timer.stop();
                
                // Limpiar partículas y entidades existentes
                leftChamber.clear();
                rightChamber.clear();
                demons.clear();
                blackHoles.clear();
                
                // Reinicializar la simulación
                initializeMolecules();
                addDemon();
                
                // Actualizar la interfaz
                statusMessage = "Container size updated to " + containerWidth + "x" + containerHeight;
                lastActionSuccessful = true;
                simulationPanel.repaint();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, 
                    "Please enter valid numbers for width and height.", 
                    "Invalid Dimensions", 
                    JOptionPane.ERROR_MESSAGE);
                lastActionSuccessful = false;
            }
        });
    }
    
    private void setupMouseListener() {
        simulationPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                
                if (x >= LEFT_MARGIN && x <= LEFT_MARGIN + containerWidth &&
                    y >= TOP_MARGIN && y <= TOP_MARGIN + containerHeight) {
                    
                    if (addRedParticleMode) {
                        addParticle(x, y, true);
                        resetInteractionModes();
                    } else if (addBlueParticleMode) {
                        addParticle(x, y, false);
                        resetInteractionModes();
                    } else if (removeParticleMode) {
                        removeParticle(x, y);
                    } else if (addBlackHoleMode) {
                        addBlackHole(x, y);
                        resetInteractionModes();
                    } else if (removeBlackHoleMode) {
                        removeBlackHole(x, y);
                    }
                }
            }
        });
    }
    
    // Métodos de lógica de simulación
    private void updateSimulation() {
        updateMolecules(leftChamber, true);
        updateMolecules(rightChamber, false);
        
        for (Demon demon : demons) {
            demon.operateGate(leftChamber, rightChamber, LEFT_MARGIN, containerWidth, SPEED_THRESHOLD);
        }
        
        checkBlackHoleCollisions();
    }
    
    private void initializeMolecules() {
        try {
            leftChamber.clear();
            rightChamber.clear();
            
            for (int i = 0; i < 50; i++) {
                leftChamber.add(createRandomParticle(true));
                rightChamber.add(createRandomParticle(false));
            }
            lastActionSuccessful = true;
        } catch (Exception e) {
            lastActionSuccessful = false;
            setStatusMessage("Failed to initialize molecules: " + e.getMessage());
        }
    }
    
    private Particle createRandomParticle(boolean inLeftChamber) {
        double x, y;
        
        if (inLeftChamber) {
            x = LEFT_MARGIN + random.nextDouble() * (containerWidth / 2 - 20);
            y = TOP_MARGIN + random.nextDouble() * (containerHeight - 20);
        } else {
            x = LEFT_MARGIN + (containerWidth / 2) + random.nextDouble() * (containerWidth / 2 - 20);
            y = TOP_MARGIN + random.nextDouble() * (containerHeight - 20);
        }
        
        double velocityX = (random.nextDouble() - 0.5) * 10;
        double velocityY = (random.nextDouble() - 0.5) * 10;
        double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        
        // Generar un colorId aleatorio entre 0-49
        int colorId = random.nextInt(50);
        
        // Asignar color base según velocidad
        Color baseColor = (speed >= SPEED_THRESHOLD) ? fastParticleColor : slowParticleColor;
        
        Particle p = new Particle(x, y, 5, velocityX, velocityY, speed, colorId);
        p.setBaseColor(baseColor);
        return p;
    }
    
    private void updateMolecules(ArrayList<Particle> molecules, boolean isLeftChamber) {
        int dividerX = LEFT_MARGIN + containerWidth / 2;
        
        for (Particle molecule : molecules) {
            molecule.move();
            
            if (isLeftChamber) {
                if (molecule.getX() < LEFT_MARGIN) {
                    molecule.setX(LEFT_MARGIN);
                    molecule.reverseXVelocity();
                } else if (molecule.getX() > dividerX - 10) {
                    molecule.setX(dividerX - 10);
                    molecule.reverseXVelocity();
                }
            } else {
                if (molecule.getX() < dividerX) {
                    molecule.setX(dividerX);
                    molecule.reverseXVelocity();
                } else if (molecule.getX() > LEFT_MARGIN + containerWidth - 10) {
                    molecule.setX(LEFT_MARGIN + containerWidth - 10);
                    molecule.reverseXVelocity();
                }
            }
            
            if (molecule.getY() < TOP_MARGIN) {
                molecule.setY(TOP_MARGIN);
                molecule.reverseYVelocity();
            } else if (molecule.getY() > TOP_MARGIN + containerHeight - 10) {
                molecule.setY(TOP_MARGIN + containerHeight - 10);
                molecule.reverseYVelocity();
            }
        }
    }
    
    private void checkBlackHoleCollisions() {
        for (Hole bh : blackHoles) {
            if (bh.isFull()) continue; // Saltar agujeros negros llenos
            
            for (int i = leftChamber.size() - 1; i >= 0; i--) {
                Particle m = leftChamber.get(i);
                if (bh.canAbsorb(m)) {
                    if (bh.absorbParticle()) {
                        leftChamber.remove(i);
                    }
                }
            }
            
            for (int i = rightChamber.size() - 1; i >= 0; i--) {
                Particle m = rightChamber.get(i);
                if (bh.canAbsorb(m)) {
                    if (bh.absorbParticle()) {
                        rightChamber.remove(i);
                    }
                }
            }
        }
    }
    
    // Métodos de utilidad
    private void addDemon() {
        try {
            int minY = TOP_MARGIN + 50;
            int maxY = TOP_MARGIN + containerHeight - 50;
            
            if (maxY <= minY) {
                maxY = TOP_MARGIN + containerHeight - 20;
                minY = TOP_MARGIN + 20;
            }
            
            int randomY = minY + random.nextInt(Math.max(1, maxY - minY));
            demons.add(new Demon(LEFT_MARGIN + containerWidth / 2, randomY));
            lastActionSuccessful = true;
            setStatusMessage("Demon added successfully");
        } catch (Exception e) {
            lastActionSuccessful = false;
            setStatusMessage("Failed to add demon: " + e.getMessage());
        }
    }
    
    private void addBlackHole(int x, int y) {
        try {
            // Verificar si estamos dentro de los límites válidos
            if (x < LEFT_MARGIN || x > LEFT_MARGIN + containerWidth ||
                y < TOP_MARGIN || y > TOP_MARGIN + containerHeight) {
                lastActionSuccessful = false;
                setStatusMessage("Cannot add black hole outside container");
                return;
            }
            
            // Generar un límite aleatorio entre 5 y 15 partículas
            int absorptionLimit = 5 + random.nextInt(11);
            blackHoles.add(new Hole(x, y, BLACK_HOLE_RADIUS, absorptionLimit));
            setStatusMessage("Black hole added (capacity: " + absorptionLimit + ")");
            lastActionSuccessful = true;
            simulationPanel.repaint();
        } catch (Exception e) {
            lastActionSuccessful = false;
            setStatusMessage("Failed to add black hole: " + e.getMessage());
        }
    }
    
    private void removeBlackHole(int x, int y) {
        try {
            for (int i = 0; i < blackHoles.size(); i++) {
                Hole bh = blackHoles.get(i);
                if (bh.contains(x, y)) {
                    blackHoles.remove(i);
                    setStatusMessage("Black hole removed");
                    lastActionSuccessful = true;
                    simulationPanel.repaint();
                    return;
                }
            }
            setStatusMessage("No black hole found at that position");
            lastActionSuccessful = false;
        } catch (Exception e) {
            lastActionSuccessful = false;
            setStatusMessage("Failed to remove black hole: " + e.getMessage());
        }
    }
    
    private void addParticle(int x, int y, boolean isFast) {
        try {
            // Verificar si estamos dentro de los límites válidos
            if (x < LEFT_MARGIN || x > LEFT_MARGIN + containerWidth ||
                y < TOP_MARGIN || y > TOP_MARGIN + containerHeight) {
                lastActionSuccessful = false;
                setStatusMessage("Cannot add particle outside container");
                return;
            }
            
            double velocityX = (random.nextDouble() - 0.5) * 8;
            double velocityY = (random.nextDouble() - 0.5) * 8;
            double speed;
            
            if (isFast) {
                speed = SPEED_THRESHOLD + 2 + random.nextDouble() * 4;
            } else {
                speed = 1 + random.nextDouble() * (SPEED_THRESHOLD - 1.5);
            }
            
            // Generar un colorId aleatorio entre 0-49
            int colorId = random.nextInt(50);
            
            // Crear la partícula y establecer el color base según tipo
            Particle newParticle = new Particle(x, y, 5, velocityX, velocityY, speed, colorId);
            newParticle.setBaseColor(isFast ? fastParticleColor : slowParticleColor);
            
            int dividerX = LEFT_MARGIN + containerWidth / 2;
            if (x < dividerX) {
                leftChamber.add(newParticle);
            } else {
                rightChamber.add(newParticle);
            }
            
            lastActionSuccessful = true;
            simulationPanel.repaint();
        } catch (Exception e) {
            lastActionSuccessful = false;
            setStatusMessage("Failed to add particle: " + e.getMessage());
        }
    }
    
    private void removeParticle(int x, int y) {
        try {
            // Check left chamber
            for (int i = 0; i < leftChamber.size(); i++) {
                Particle m = leftChamber.get(i);
                if (isPointOnParticle(x, y, m)) {
                    leftChamber.remove(i);
                    setStatusMessage("Particle removed");
                    lastActionSuccessful = true;
                    simulationPanel.repaint();
                    return;
                }
            }
            
            // Check right chamber
            for (int i = 0; i < rightChamber.size(); i++) {
                Particle m = rightChamber.get(i);
                if (isPointOnParticle(x, y, m)) {
                    rightChamber.remove(i);
                    setStatusMessage("Particle removed");
                    lastActionSuccessful = true;
                    simulationPanel.repaint();
                    return;
                }
            }
            
            setStatusMessage("No particle found at that position");
            lastActionSuccessful = false;
        } catch (Exception e) {
            lastActionSuccessful = false;
            setStatusMessage("Failed to remove particle: " + e.getMessage());
        }
    }
    
        private boolean isPointOnParticle(int x, int y, Particle p) {
        // Utilizamos el método contains de Circle
        return p.contains(x, y);
    }
    
    private void resetInteractionModes() {
        addRedParticleMode = false;
        addBlueParticleMode = false;
        removeParticleMode = false;
        addBlackHoleMode = false;
        removeBlackHoleMode = false;
        setCursor(Cursor.getDefaultCursor());
        setStatusMessage("");
    }
    
    private void setStatusMessage(String message) {
        statusMessage = message;
        if (isSimulatorVisible) {
            simulationPanel.repaint();
        }
    }
    
    private void hideSimulator() {
        isSimulatorVisible = false;
        setVisible(false);
        statusWindow.setVisible(true);
    }
    
    private void showSimulator() {
        isSimulatorVisible = true;
        setVisible(true);
        statusWindow.setVisible(false);
        simulationPanel.repaint();
    }
    
    private void confirmExit() {
        int response = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de que desea terminar el simulador?",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (response == JOptionPane.YES_OPTION) {
            timer.stop();
            statusWindow.dispose();
            dispose();
            System.exit(0);
        }
    }
    
    // Métodos para contar y acceder a propiedades
    public int countRedMolecules() {
        int count = 0;
        for (Particle m : leftChamber) {
            if (m.getSpeed() >= SPEED_THRESHOLD) count++;
        }
        for (Particle m : rightChamber) {
            if (m.getSpeed() >= SPEED_THRESHOLD) count++;
        }
        return count;
    }
    
    public int countBlueMolecules() {
        int count = 0;
        for (Particle m : leftChamber) {
            if (m.getSpeed() < SPEED_THRESHOLD) count++;
        }
        for (Particle m : rightChamber) {
            if (m.getSpeed() < SPEED_THRESHOLD) count++;
        }
        return count;
    }
    
    // Getters para el Canvas y otras clases
    public ArrayList<Particle> getLeftChamber() { return leftChamber; }
    public ArrayList<Particle> getRightChamber() { return rightChamber; }
    public ArrayList<Demon> getDemons() { return demons; }
    public ArrayList<Hole> getBlackHoles() { return blackHoles; }
    public int getContainerWidth() { return containerWidth; }
    public int getContainerHeight() { return containerHeight; }
    public int getLeftMargin() { return LEFT_MARGIN; }
    public int getTopMargin() { return TOP_MARGIN; }
    public double getSpeedThreshold() { return SPEED_THRESHOLD; }
    public String getStatusMessage() { return statusMessage; }
    public Color getFastParticleColor() { return fastParticleColor; }
    public Color getSlowParticleColor() { return slowParticleColor; }
    
    // Método principal
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MaxwellContainer());
    }
}
