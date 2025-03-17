import java.awt.*;
import java.awt.geom.Point2D;

public class Hole extends Circle {
    private int maxAbsorbed; // Máximo de partículas que puede absorber
    private int particlesAbsorbed; // Contador de partículas absorbidas
    private boolean isFull; // Indicador de capacidad máxima alcanzada
    
    public Hole(int x, int y, int radius, int maxAbsorbed) {
        super(x, y, radius);
        this.maxAbsorbed = maxAbsorbed > 0 ? maxAbsorbed : 10; // Valor por defecto: 10
        this.particlesAbsorbed = 0;
        this.isFull = false;
    }
    
    // Constructor alternativo con valor por defecto
    public Hole(int x, int y, int radius) {
        this(x, y, radius, 10);
    }
    
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        // Color base del agujero según su estado
        Color baseColor;
        if (isFull) {
            baseColor = new Color(150, 0, 0); // Rojo más oscuro cuando está lleno
        } else {
            baseColor = new Color(0, 0, 0); // Negro normal
        }
        
        // Crear un gradiente radial para efecto de agujero negro
        RadialGradientPaint paint = new RadialGradientPaint(
            new Point2D.Float((float)getX(), (float)getY()),
            getRadius(),
            new float[] {0.0f, 0.7f, 1.0f},
            new Color[] {
                baseColor,                      // Centro
                new Color(20, 20, 50),          // Azul oscuro
                new Color(50, 0, 50, 180)       // Morado con transparencia en el borde
            }
        );
        
        g2.setPaint(paint);
        g2.fillOval((int)getX() - getRadius(), (int)getY() - getRadius(), 
                   getRadius() * 2, getRadius() * 2);
        
        // Dibujar anillo/resplandor exterior
        if (isFull) {
            g2.setColor(new Color(200, 0, 0, 100)); // Resplandor rojo cuando está lleno
        } else {
            g2.setColor(new Color(100, 0, 100, 100)); // Resplandor normal
        }
        g2.setStroke(new BasicStroke(2));
        g2.drawOval((int)getX() - getRadius() - 2, (int)getY() - getRadius() - 2, 
                    (getRadius() + 2) * 2, (getRadius() + 2) * 2);
        
        // Mostrar contador de absorción
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        String countText = particlesAbsorbed + "/" + maxAbsorbed;
        int textWidth = g2.getFontMetrics().stringWidth(countText);
        g2.drawString(countText, (int)getX() - textWidth/2, (int)getY() + 4);
    }
    
    // Verificar si una partícula está dentro del radio de absorción
    // y si el agujero puede absorber más
    public boolean canAbsorb(Particle p) {
        if (isFull) return false;
        
        double distance = Math.sqrt(
            Math.pow((p.getX() + p.getRadius()) - getX(), 2) + 
            Math.pow((p.getY() + p.getRadius()) - getY(), 2)
        );
        
        return distance <= getRadius() + p.getRadius();
    }
    
    // Método para absorber una partícula
    public boolean absorbParticle() {
        if (particlesAbsorbed >= maxAbsorbed) {
            isFull = true;
            return false;
        }
        
        particlesAbsorbed++;
        if (particlesAbsorbed >= maxAbsorbed) {
            isFull = true;
        }
        
        return true;
    }
    
    // Getters y setters
    public boolean isFull() {
        return isFull;
    }
    
    public int getMaxAbsorbed() {
        return maxAbsorbed;
    }
    
    public void setMaxAbsorbed(int maxAbsorbed) {
        this.maxAbsorbed = maxAbsorbed;
        this.isFull = (particlesAbsorbed >= maxAbsorbed);
    }
    
    public int getParticlesAbsorbed() {
        return particlesAbsorbed;
    }
    
    public void resetAbsorptionCount() {
        this.particlesAbsorbed = 0;
        this.isFull = false;
    }
}