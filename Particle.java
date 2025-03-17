import java.awt.*;

public class Particle extends Circle {
    // Array de exactamente 50 colores predefinidos
    private static final Color[] COLOR_PALETTE = {
        // Rojos
        new Color(255, 0, 0),      // Rojo
        new Color(220, 20, 60),    // Carmesí
        new Color(178, 34, 34),    // Rojo fuego
        new Color(255, 69, 0),     // Rojo-naranja
        new Color(139, 0, 0),      // Rojo oscuro
        
        // Naranjas
        new Color(255, 140, 0),    // Naranja oscuro
        new Color(255, 165, 0),    // Naranja
        new Color(255, 215, 0),    // Oro
        new Color(255, 192, 203),  // Rosa claro
        new Color(255, 20, 147),   // Rosa profundo
        
        // Amarillos
        new Color(255, 255, 0),    // Amarillo
        new Color(255, 255, 224),  // Amarillo claro
        new Color(240, 230, 140),  // Caqui
        new Color(189, 183, 107),  // Caqui oscuro
        new Color(238, 232, 170),  // Amarillo pálido
        
        // Verdes
        new Color(0, 128, 0),      // Verde
        new Color(34, 139, 34),    // Verde bosque
        new Color(154, 205, 50),   // Verde amarillento
        new Color(107, 142, 35),   // Verde oliva
        new Color(173, 255, 47),   // Verde césped
        
        // Verde-azulados
        new Color(0, 255, 127),    // Verde primavera
        new Color(60, 179, 113),   // Verde mar medio
        new Color(46, 139, 87),    // Verde mar
        new Color(32, 178, 170),   // Turquesa claro
        new Color(0, 139, 139),    // Cian oscuro
        
        // Azules
        new Color(0, 0, 255),      // Azul
        new Color(0, 0, 139),      // Azul oscuro
        new Color(0, 0, 205),      // Azul medio
        new Color(65, 105, 225),   // Azul real
        new Color(30, 144, 255),   // Azul dodger
        
        // Azul-púrpuras
        new Color(135, 206, 250),  // Azul cielo claro
        new Color(70, 130, 180),   // Azul acero
        new Color(100, 149, 237),  // Azul grisáceo
        new Color(0, 191, 255),    // Azul cielo profundo
        new Color(176, 196, 222),  // Azul acero claro
        
        // Púrpuras
        new Color(128, 0, 128),    // Púrpura
        new Color(186, 85, 211),   // Púrpura medio
        new Color(148, 0, 211),    // Violeta oscuro
        new Color(153, 50, 204),   // Orquídea oscuro
        new Color(138, 43, 226),   // Azul violeta
        
        // Marrones
        new Color(165, 42, 42),    // Marrón
        new Color(160, 82, 45),    // Siena
        new Color(210, 105, 30),   // Chocolate
        new Color(205, 133, 63),   // Perú
        new Color(139, 69, 19),    // Silla de montar
        
        // Grises
        new Color(128, 128, 128),  // Gris
        new Color(169, 169, 169),  // Gris oscuro
        new Color(192, 192, 192),  // Plata
        new Color(211, 211, 211),  // Gris claro
        new Color(220, 220, 220)   // Gainsboro (gris pálido)
    };

    private double velocityX, velocityY;
    private double speed;
    private Color color;     // Color de identificación individual (de la paleta)
    private Color baseColor; // Color base según velocidad (rápido/lento)
    private int colorId;     // ID del color (0-49)
    
    public Particle(double x, double y, int radius, double velocityX, double velocityY, double speed, int colorId) {
        super(x, y, radius);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.speed = speed;
        setColorId(colorId); // Esto asigna el colorId y el color correspondiente
        this.baseColor = null; // Se debe establecer externamente
    }
    
    // Actualizar posición basada en la velocidad
    public void move() {
        setX(getX() + velocityX);
        setY(getY() + velocityY);
    }
    
    // Invertir la velocidad en X (rebote horizontal)
    public void reverseXVelocity() {
        velocityX = -velocityX;
    }
    
    // Invertir la velocidad en Y (rebote vertical)
    public void reverseYVelocity() {
        velocityY = -velocityY;
    }
    
    // Dibujar la partícula con su color asignado
    @Override
    public void draw(Graphics g) {
        draw(g, null);
    }
    
    // Dibujar con un color personalizado o usar el color base configurado
    @Override
    public void draw(Graphics g, Color overrideColor) {
        Color drawColor = overrideColor;
        if (drawColor == null) {
            drawColor = baseColor != null ? baseColor : color;
        }
        
        g.setColor(drawColor);
        g.fillOval((int)(getX() - getRadius()), (int)(getY() - getRadius()), 
                  2 * getRadius(), 2 * getRadius());
        
        // Dibujar borde
        g.setColor(Color.BLACK);
        g.drawOval((int)(getX() - getRadius()), (int)(getY() - getRadius()), 
                  2 * getRadius(), 2 * getRadius());
    }
    
    // Métodos para obtener/establecer velocidades
    public double getVelocityX() {
        return velocityX;
    }
    
    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
        updateSpeed();
    }
    
    public double getVelocityY() {
        return velocityY;
    }
    
    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
        updateSpeed();
    }
    
    // Actualizar la velocidad total cuando cambian las componentes
    private void updateSpeed() {
        this.speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
    }
    
    // Establecer directamente una nueva velocidad
    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        updateSpeed();
    }
    
    // Obtener/establecer velocidad total
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        // Ajustar componentes de velocidad proporcionalmente
        double factor = speed / this.speed;
        this.velocityX *= factor;
        this.velocityY *= factor;
        this.speed = speed;
    }
    
    // Getters y setters para color e ID de color
    public Color getColor() {
        return color;
    }
    
    public int getColorId() {
        return colorId;
    }
    
    public void setColorId(int colorId) {
        // Asegurar que el colorId esté en el rango 0-49
        this.colorId = Math.abs(colorId) % 50;
        this.color = COLOR_PALETTE[this.colorId];
    }
    
    // Método estático para acceder a un color específico de la paleta
    public static Color getColorFromPalette(int index) {
        if (index < 0 || index >= COLOR_PALETTE.length) {
            index = 0;
        }
        return COLOR_PALETTE[index];
    }
    
    // Obtener el número total de colores en la paleta
    public static int getColorCount() {
        return COLOR_PALETTE.length;
    }
    
    // Getters y setters para color base
    public Color getBaseColor() {
        return baseColor;
    }
    
    public void setBaseColor(Color baseColor) {
        this.baseColor = baseColor;
    }
}