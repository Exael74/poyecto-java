import java.awt.*;

public class Circle {
    private double x, y;
    private int radius;
    
    public Circle(double x, double y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    
    public void draw(Graphics g, Color color) {
        Color originalColor = g.getColor();
        g.setColor(color);
        g.fillOval((int)x - radius, (int)y - radius, radius * 2, radius * 2);
        g.setColor(originalColor);
    }
    
    public void draw(Graphics g) {
        draw(g, Color.BLACK);
    }
    
    // Método para verificar si un punto está dentro del círculo
    public boolean contains(int pointX, int pointY) {
        double distance = Math.sqrt(Math.pow(pointX - x, 2) + Math.pow(pointY - y, 2));
        return distance <= radius;
    }
    
    // Getters y setters
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
    }
}