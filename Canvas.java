import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Canvas extends JPanel {
    private final MaxwellContainer container;
    
    // Referencias a figuras que pertenecen al canvas
    private Rectangle containerRect;
    private ArrayList<Circle> particleShapes = new ArrayList<>();
    private ArrayList<Triangle> demonTriangles = new ArrayList<>();
    
    public Canvas(MaxwellContainer container) {
        this.container = container;
        setBackground(Color.WHITE);
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
        
        // Se eliminó la llamada a drawColorLegend que mostraba los IDs de colores
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
    
    // Se eliminó el método drawColorLegend y generateColorFromId
    
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
}