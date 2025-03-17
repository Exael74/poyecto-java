import java.awt.*;

public class Rectangle {
    private int x, y;
    private int width, height;
    
    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    // Método para dibujar solo el borde (contorno) del rectángulo
    public void draw(Graphics g) {
        g.drawRect(x, y, width, height);
    }
    
    // Método para dibujar un rectángulo relleno
    public void fill(Graphics g, Color color) {
        Color originalColor = g.getColor();
        g.setColor(color);
        g.fillRect(x, y, width, height);
        g.setColor(originalColor);
    }
    
    // Método para verificar si un punto está dentro del rectángulo
    public boolean contains(int pointX, int pointY) {
        return (pointX >= x && pointX <= x + width && 
                pointY >= y && pointY <= y + height);
    }
    
    // Getters y setters
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
}