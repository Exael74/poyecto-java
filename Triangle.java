import java.awt.*;

public class Triangle {
    // Puntos del triángulo
    private int[] xPoints;
    private int[] yPoints;
    
    // Constructor con tres puntos (x,y)
    public Triangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        xPoints = new int[]{x1, x2, x3};
        yPoints = new int[]{y1, y2, y3};
    }
    
    // Constructor con arreglos de puntos
    public Triangle(int[] xPoints, int[] yPoints) {
        if (xPoints.length != 3 || yPoints.length != 3) {
            throw new IllegalArgumentException("Los arreglos deben contener exactamente 3 puntos");
        }
        this.xPoints = xPoints.clone();
        this.yPoints = yPoints.clone();
    }
    
    // Método para dibujar el contorno del triángulo
    public void draw(Graphics g) {
        g.drawPolygon(xPoints, yPoints, 3);
    }
    
    // Método para dibujar un triángulo relleno
    public void fill(Graphics g, Color color) {
        Color originalColor = g.getColor();
        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 3);
        g.setColor(originalColor);
    }
    
    // Método para verificar si un punto está dentro del triángulo
    public boolean contains(int x, int y) {
        // Implementación del algoritmo de punto en triángulo
        // Usando el método de áreas baricéntricas
        int x1 = xPoints[0], y1 = yPoints[0];
        int x2 = xPoints[1], y2 = yPoints[1];
        int x3 = xPoints[2], y3 = yPoints[2];
        
        // Calcular áreas
        double ABC = Math.abs((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2.0);
        double PBC = Math.abs((x * (y2 - y3) + x2 * (y3 - y) + x3 * (y - y2)) / 2.0);
        double PAC = Math.abs((x1 * (y - y3) + x * (y3 - y1) + x3 * (y1 - y)) / 2.0);
        double PAB = Math.abs((x1 * (y2 - y) + x2 * (y - y1) + x * (y1 - y2)) / 2.0);
        
        // Si la suma de PBC, PAC y PAB es igual a ABC, entonces el punto está dentro
        return Math.abs(PBC + PAC + PAB - ABC) < 0.1;  // Usar un pequeño epsilon para manejar errores de punto flotante
    }
    
    // Mover todo el triángulo
    public void translate(int dx, int dy) {
        for (int i = 0; i < 3; i++) {
            xPoints[i] += dx;
            yPoints[i] += dy;
        }
    }
    
    // Getters y setters
    public int[] getXPoints() {
        return xPoints.clone();
    }
    
    public int[] getYPoints() {
        return yPoints.clone();
    }
    
    public void setXPoints(int[] xPoints) {
        if (xPoints.length != 3) {
            throw new IllegalArgumentException("El arreglo debe contener exactamente 3 puntos");
        }
        this.xPoints = xPoints.clone();
    }
    
    public void setYPoints(int[] yPoints) {
        if (yPoints.length != 3) {
            throw new IllegalArgumentException("El arreglo debe contener exactamente 3 puntos");
        }
        this.yPoints = yPoints.clone();
    }
    
    // Obtener punto específico (0, 1, o 2)
    public int getX(int index) {
        if (index < 0 || index > 2) {
            throw new IllegalArgumentException("Índice inválido. Debe ser 0, 1 o 2");
        }
        return xPoints[index];
    }
    
    public int getY(int index) {
        if (index < 0 || index > 2) {
            throw new IllegalArgumentException("Índice inválido. Debe ser 0, 1 o 2");
        }
        return yPoints[index];
    }
    
    // Establecer punto específico (0, 1, o 2)
    public void setPoint(int index, int x, int y) {
        if (index < 0 || index > 2) {
            throw new IllegalArgumentException("Índice inválido. Debe ser 0, 1 o 2");
        }
        xPoints[index] = x;
        yPoints[index] = y;
    }
}