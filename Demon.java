import java.awt.*;
import java.util.ArrayList;

public class Demon {
    private int positionX;
    private int positionY;
    private final int triangleSize = 20;
    private Triangle leftTriangle;
    private Triangle rightTriangle;
    
    public Demon(int positionX, int positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }
    
    public void draw(Graphics g, int demonNumber, Canvas canvas) {
        int dividerX = positionX;
        int gateY = positionY;
        
        // Crear triángulos usando el método del canvas si no existen
        if (leftTriangle == null) {
            leftTriangle = canvas.createDemonTriangle(dividerX, gateY, true);
        }
        if (rightTriangle == null) {
            rightTriangle = canvas.createDemonTriangle(dividerX, gateY, false);
        }
        
        // Dibujar los triángulos rellenos
        leftTriangle.fill(g, new Color(255, 100, 100, 200));
        rightTriangle.fill(g, new Color(255, 100, 100, 200));
        
        // Dibujar el contorno de los triángulos
        g.setColor(Color.RED);
        leftTriangle.draw(g);
        rightTriangle.draw(g);
        
        // Dibujar una línea para representar la puerta/sensor
        g.setColor(Color.RED);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(dividerX, gateY - triangleSize, dividerX, gateY + triangleSize);
        g2.setStroke(new BasicStroke(1));
        
        // Dibujar el número del demonio para identificación
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("D" + demonNumber, dividerX - 20, gateY - triangleSize - 5);
    }
    
    public void operateGate(ArrayList<Particle> leftChamber, ArrayList<Particle> rightChamber, 
                           int leftMargin, int containerWidth, double speedThreshold) {
        // Definir el área de la puerta para este demonio
        double gateY1 = positionY - triangleSize;
        double gateY2 = positionY + triangleSize;
        int gatePosX = positionX;
        
        // Verificar moléculas cerca de la puerta en la cámara izquierda
        for (int i = leftChamber.size() - 1; i >= 0; i--) {
            Particle m = leftChamber.get(i);
            
            // Si la molécula está cerca de la puerta y moviéndose hacia la derecha
            if (m.getX() > gatePosX - 20 && m.getX() < gatePosX - 5 &&
                m.getY() > gateY1 && m.getY() < gateY2 && 
                m.getVelocityX() > 0) {
                
                // Solo permitir que las moléculas rápidas (rojas) pasen
                if (m.getSpeed() >= speedThreshold) {
                    // Mover molécula a la cámara derecha
                    m.setX(gatePosX + 5);
                    rightChamber.add(m);
                    leftChamber.remove(i);
                }
            }
        }
        
        // Verificar moléculas cerca de la puerta en la cámara derecha
        for (int i = rightChamber.size() - 1; i >= 0; i--) {
            Particle m = rightChamber.get(i);
            
            // Si la molécula está cerca de la puerta y moviéndose hacia la izquierda
            if (m.getX() > gatePosX + 5 && m.getX() < gatePosX + 20 &&
                m.getY() > gateY1 && m.getY() < gateY2 && 
                m.getVelocityX() < 0) {
                
                // Solo permitir que las moléculas lentas (azules) pasen
                if (m.getSpeed() < speedThreshold) {
                    // Mover molécula a la cámara izquierda
                    m.setX(gatePosX - 10);
                    leftChamber.add(m);
                    rightChamber.remove(i);
                }
            }
        }
    }
    
    // Getters y setters
    public int getPositionX() {
        return positionX;
    }
    
    public void setPositionX(int positionX) {
        this.positionX = positionX;
        // Actualizar posición de los triángulos si existen
        if (leftTriangle != null && rightTriangle != null) {
            // Actualización pendiente para triangles...
        }
    }
    
    public int getPositionY() {
        return positionY;
    }
    
    public void setPositionY(int positionY) {
        this.positionY = positionY;
        // Actualizar posición de los triángulos si existen
        if (leftTriangle != null && rightTriangle != null) {
            // Actualización pendiente para triangles...
        }
    }
    
    public int getTriangleSize() {
        return triangleSize;
    }
    
    public Triangle getLeftTriangle() {
        return leftTriangle;
    }
    
    public Triangle getRightTriangle() {
        return rightTriangle;
    }
}