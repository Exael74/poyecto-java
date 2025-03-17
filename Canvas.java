import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Canvas extends JPanel {
    private final MaxwellContainer container;
    
    // Referencias a figuras que pertenecen al canvas
    private Rectangle containerRect;
    private ArrayList<Circle> particleShapes = new ArrayList<>();
    private ArrayList<Triangle> demonTriangles = new ArrayList<>();
    
    // Variables para la paleta de colores
    private boolean showColorPalette = false;
    private Rectangle colorPaletteArea;
    private final int PALETTE_TILE_SIZE = 20;
    private final int PALETTE_COLS = 10;
    private final int PALETTE_ROWS = 5;
    private final int PALETTE_MARGIN = 5;
    private boolean selectingFastColor = true; // true = seleccionar color para partículas rápidas, false = lentas
    
    public Canvas(MaxwellContainer container) {
        this.container = container;
        setBackground(Color.WHITE);
        
        // Inicializar área de la paleta
        colorPaletteArea = new Rectangle(10, 10, 
                                       PALETTE_COLS * PALETTE_TILE_SIZE + PALETTE_MARGIN * 2, 
                                       PALETTE_ROWS * PALETTE_TILE_SIZE + PALETTE_MARGIN * 2);
        
        // Agregar listener para detectar clics en la paleta
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (showColorPalette) {
                    handleColorPaletteClick(e.getX(), e.getY());
                }
            }
        });
    }
    
    private void handleColorPaletteClick(int x, int y) {
        if (colorPaletteArea.contains(x, y)) {
            int relX = x - colorPaletteArea.getX() - PALETTE_MARGIN;
            int relY = y - colorPaletteArea.getY() - PALETTE_MARGIN;
            
            if (relX >= 0 && relY >= 0) {
                int col = relX / PALETTE_TILE_SIZE;
                int row = relY / PALETTE_TILE_SIZE;
                
                if (col < PALETTE_COLS && row < PALETTE_ROWS) {
                    int index = row * PALETTE_COLS + col;
                    if (index < 50) { // Aseguramos que no exceda el número de colores
                        Color selectedColor = getColorFromPalette(index);
                        
                        // Verificar que el color seleccionado sea diferente al otro tipo de partícula
                        boolean differentColor = true;
                        if (selectingFastColor) {
                            differentColor = !selectedColor.equals(container.getSlowParticleColor());
                        } else {
                            differentColor = !selectedColor.equals(container.getFastParticleColor());
                        }
                        
                        // Si son colores diferentes, aplicar el cambio
                        if (differentColor) {
                            if (selectingFastColor) {
                                container.setFastParticleColor(selectedColor);
                            } else {
                                container.setSlowParticleColor(selectedColor);
                            }
                            
                            // Ocultar paleta después de seleccionar
                            showColorPalette = false;
                            repaint();
                        } else {
                            // Mensaje de error si intentan seleccionar el mismo color
                            JOptionPane.showMessageDialog(container, 
                                "Fast and slow particles cannot have the same color.", 
                                "Invalid Color Selection", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        }
    }
    
    // Método para obtener un color específico de la paleta
    private Color getColorFromPalette(int index) {
        // Usamos el método estático de Particle para acceder a la paleta de colores
        return Particle.getColorFromPalette(index);
    }
    
    public void initShapes() {
        int leftMargin = container.getLeftMargin();
        int topMargin = container.getTopMargin();
        int containerWidth = container.getContainerWidth();
        int containerHeight = container.getContainerHeight();
        
        // Crear el rectángulo del contenedor
        containerRect = new Rectangle(leftMargin, topMargin, containerWidth, containerHeight);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Si no se ha inicializado el rectángulo del contenedor, hacerlo ahora
        if (containerRect == null) {
            initShapes();
        }
        
        int leftMargin = container.getLeftMargin();
        int topMargin = container.getTopMargin();
        int containerWidth = container.getContainerWidth();
        int containerHeight = container.getContainerHeight();
        double speedThreshold = container.getSpeedThreshold();
        
        // Dibujar el contenedor (rectángulo)
        g.setColor(Color.BLACK);
        containerRect.draw(g);
        
        // Dibujar línea divisoria
        int dividerX = leftMargin + containerWidth / 2;
        g.drawLine(dividerX, topMargin, dividerX, topMargin + containerHeight);
        
        // Dibujar agujeros negros
        ArrayList<Hole> blackHoles = container.getBlackHoles();
        for (Hole bh : blackHoles) {
            bh.draw(g);
        }
        
        // Dibujar demonios
        ArrayList<Demon> demons = container.getDemons();
        for (int i = 0; i < demons.size(); i++) {
            Demon demon = demons.get(i);
            demon.draw(g, i + 1, this);
        }
        
        // Dibujar moléculas en la cámara izquierda con sus colores propios
        ArrayList<Particle> leftChamber = container.getLeftChamber();
        for (Particle molecule : leftChamber) {
            molecule.draw(g);
        }
        
        // Dibujar moléculas en la cámara derecha con sus colores propios
        ArrayList<Particle> rightChamber = container.getRightChamber();
        for (Particle molecule : rightChamber) {
            molecule.draw(g);
        }
        
        // Mostrar mensaje de estado actual
        String statusMessage = container.getStatusMessage();
        if (!statusMessage.isEmpty()) {
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(statusMessage, leftMargin, topMargin - 10);
        }
        
        // Dibujar dimensiones del contenedor
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Container: " + containerWidth + "×" + containerHeight, 
                    leftMargin, topMargin + containerHeight + 15);
        
        // Dibujar contadores de moléculas y agujeros negros
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        
        int leftCount = leftChamber.size();
        int rightCount = rightChamber.size();
        int redCount = container.countRedMolecules();
        int blueCount = container.countBlueMolecules();
        
        g.drawString(String.format("Left: %d | Right: %d | Fast: %d | Slow: %d | Demons: %d | Black Holes: %d", 
                     leftCount, rightCount, redCount, blueCount, demons.size(), blackHoles.size()), 
                     leftMargin, topMargin + containerHeight + 35);
                     
        // Dibujar leyenda de colores de partículas
        drawParticleTypeLegend(g, leftMargin, topMargin + containerHeight + 55);
        
        // Dibujar paleta de colores si está visible
        if (showColorPalette) {
            drawColorPalette(g);
        }
    }
    
    // Método para dibujar la leyenda de tipos de partículas
    private void drawParticleTypeLegend(Graphics g, int x, int y) {
        Color fastColor = container.getFastParticleColor();
        Color slowColor = container.getSlowParticleColor();
        int boxSize = 12;
        
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Particle Types:", x, y);
        
        // Partículas rápidas
        g.setColor(fastColor);
        g.fillRect(x + 100, y - boxSize, boxSize, boxSize);
        g.setColor(Color.BLACK);
        g.drawRect(x + 100, y - boxSize, boxSize, boxSize);
        g.drawString("Fast", x + 100 + boxSize + 5, y);
        
        // Partículas lentas
        g.setColor(slowColor);
        g.fillRect(x + 180, y - boxSize, boxSize, boxSize);
        g.setColor(Color.BLACK);
        g.drawRect(x + 180, y - boxSize, boxSize, boxSize);
        g.drawString("Slow", x + 180 + boxSize + 5, y);
    }
    
    // Método para dibujar la paleta de 50 colores
    private void drawColorPalette(Graphics g) {
        // Dibujar fondo de la paleta
        g.setColor(new Color(240, 240, 240));
        g.fillRect(colorPaletteArea.getX(), colorPaletteArea.getY(), 
                 colorPaletteArea.getWidth(), colorPaletteArea.getHeight());
        g.setColor(Color.BLACK);
        g.drawRect(colorPaletteArea.getX(), colorPaletteArea.getY(), 
                  colorPaletteArea.getWidth(), colorPaletteArea.getHeight());
        
        // Dibujar título indicando qué tipo de partícula se está configurando
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String titleText = "Select color for " + (selectingFastColor ? "FAST" : "SLOW") + " particles";
        g.drawString(titleText, 
                   colorPaletteArea.getX() + 10, 
                   colorPaletteArea.getY() - 5);
        
        // Dibujar cada color
        for (int i = 0; i < 50; i++) {
            int row = i / PALETTE_COLS;
            int col = i % PALETTE_COLS;
            
            int x = colorPaletteArea.getX() + PALETTE_MARGIN + col * PALETTE_TILE_SIZE;
            int y = colorPaletteArea.getY() + PALETTE_MARGIN + row * PALETTE_TILE_SIZE;
            
            // Obtener el color de la paleta
            Color color = getColorFromPalette(i);
            
            // Dibujar el cuadrado de color
            g.setColor(color);
            g.fillRect(x, y, PALETTE_TILE_SIZE - 1, PALETTE_TILE_SIZE - 1);
            
            // Dibujar borde negro
            g.setColor(Color.BLACK);
            g.drawRect(x, y, PALETTE_TILE_SIZE - 1, PALETTE_TILE_SIZE - 1);
            
            // Destacar los colores actuales
            if (color.equals(container.getFastParticleColor())) {
                g.setColor(Color.WHITE);
                g.drawString("F", x + 6, y + 14);
            }
            
            if (color.equals(container.getSlowParticleColor())) {
                g.setColor(Color.WHITE);
                g.drawString("S", x + 6, y + 14);
            }
        }
    }
    
    // Método para actualizar dimensiones del contenedor cuando cambian
    public void updateContainerSize(int width, int height) {
        int leftMargin = container.getLeftMargin();
        int topMargin = container.getTopMargin();
        containerRect = new Rectangle(leftMargin, topMargin, width, height);
        repaint();
    }
    
    // Método para crear un triángulo para un demonio 
    public Triangle createDemonTriangle(int centerX, int posY, boolean isLeftSide) {
        int triangleSize = 20;
        int[] xPoints;
        int[] yPoints;
        
        if (isLeftSide) {
            // Triángulo izquierdo (apunta a la derecha)
            xPoints = new int[]{centerX - 15, centerX - 15, centerX - 5};
            yPoints = new int[]{posY - triangleSize, posY + triangleSize, posY};
        } else {
            // Triángulo derecho (apunta a la izquierda)
            xPoints = new int[]{centerX + 15, centerX + 15, centerX + 5};
            yPoints = new int[]{posY - triangleSize, posY + triangleSize, posY};
        }
        
        Triangle triangle = new Triangle(xPoints, yPoints);
        return triangle;
    }
    
    // Método para obtener el rectángulo del contenedor
    public Rectangle getContainerRect() {
        return containerRect;
    }
    
    // Método para convertir una partícula en un objeto Circle para dibujar
    public Circle particleToCircle(Particle p, Color color) {
        Circle circle = new Circle(p.getX() + p.getRadius(), p.getY() + p.getRadius(), p.getRadius());
        return circle;
    }
    
    // Métodos para la paleta de colores
    public void showColorPalette(boolean isFastColor) {
        showColorPalette = true;
        selectingFastColor = isFastColor;
        repaint();
    }
    
    public void hideColorPalette() {
        showColorPalette = false;
        repaint();
    }
    
    public boolean isColorPaletteVisible() {
        return showColorPalette;
    }
}