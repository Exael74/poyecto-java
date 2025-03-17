import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Pruebas unitarias para la clase MaxwellContainer
 * Se ejecutan en modo invisible para no interrumpir el flujo visual
 */
public class MaxwellContainerC1Test {
    
    private MaxwellContainer container;
    private final CountDownLatch latch = new CountDownLatch(1);
    
    @BeforeEach
    public void setUp() throws Exception {
        // Iniciar el container en el hilo de EDT
        SwingUtilities.invokeAndWait(() -> {
            container = new MaxwellContainer();
            // Aseguramos que esté en modo invisible inmediatamente
            container.setVisible(false);
            latch.countDown();
        });
        
        // Esperar hasta que se cree el contenedor
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout en la creación del contenedor");
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (container != null) {
            SwingUtilities.invokeAndWait(() -> {
                container.dispose();
            });
        }
    }
    
    /**
     * PRUEBAS DE INICIALIZACIÓN (Requisito 1: Create)
     */
    @Test
    public void testDefaultContainerInitialization() {
        // QUÉ DEBERÍA HACER: Inicializarse con valores predeterminados
        assertNotNull(container, "El contenedor no debería ser nulo");
        assertEquals(700, container.getContainerWidth(), "Ancho predeterminado incorrecto");
        assertEquals(500, container.getContainerHeight(), "Altura predeterminada incorrecta");
        assertTrue(!container.getLeftChamber().isEmpty(), "La cámara izquierda debería tener partículas");
        assertTrue(!container.getRightChamber().isEmpty(), "La cámara derecha debería tener partículas");
        assertEquals(1, container.getDemons().size(), "Debería haber 1 demonio por defecto");
        assertEquals(0, container.getBlackHoles().size(), "No debería haber agujeros negros por defecto");
    }
    
    /**
     * PRUEBAS DE GESTIÓN DE DEMONIOS (Requisito 2: Add/Delete Demon)
     */
    @Test
    public void testAddDemon() {
        // QUÉ DEBERÍA HACER: Añadir un demonio a la lista
        int initialCount = container.getDemons().size();
        
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 3; i++) {
                // Usamos el método interno addDemon mediante reflexión
                try {
                    java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod("addDemon");
                    method.setAccessible(true);
                    method.invoke(container);
                } catch (Exception e) {
                    fail("No se pudo invocar el método addDemon: " + e.getMessage());
                }
            }
        });
        
        // Esperar para que la UI se actualice
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertEquals(initialCount + 3, container.getDemons().size(), 
                   "Deberían haberse añadido 3 demonios");
    }
    
    @Test
    public void testRemoveDemon() {
        // QUÉ DEBERÍA HACER: Eliminar un demonio de la lista
        // Primero aseguramos que haya demonios para eliminar
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method addMethod = MaxwellContainer.class.getDeclaredMethod("addDemon");
                addMethod.setAccessible(true);
                addMethod.invoke(container);
                addMethod.invoke(container);
            } catch (Exception e) {
                fail("No se pudo invocar el método addDemon: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Ahora deberíamos tener al menos 3 demonios
        int initialCount = container.getDemons().size();
        assertTrue(initialCount >= 3, "Debería haber al menos 3 demonios para esta prueba");
        
        // Eliminar un demonio
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method removeMethod = MaxwellContainer.class.getDeclaredMethod("removeDemon");
                removeMethod.setAccessible(true);
                removeMethod.invoke(container);
            } catch (Exception e) {
                fail("No se pudo invocar el método removeDemon: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertEquals(initialCount - 1, container.getDemons().size(), 
                   "Debería haberse eliminado 1 demonio");
    }
    
    @Test
    public void testRemoveDemonWhenEmpty() {
        // QUÉ NO DEBERÍA HACER: No debería causar error al intentar eliminar cuando no hay demonios
        
        // Primero eliminamos todos los demonios
        SwingUtilities.invokeLater(() -> {
            while (!container.getDemons().isEmpty()) {
                try {
                    java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod("removeDemon");
                    method.setAccessible(true);
                    method.invoke(container);
                } catch (Exception e) {
                    fail("Error al eliminar demonios: " + e.getMessage());
                }
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificamos que no haya demonios
        assertEquals(0, container.getDemons().size(), "No deberían quedar demonios");
        
        // Intentamos eliminar otro demonio, no debería causar error
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod("removeDemon");
                method.setAccessible(true);
                method.invoke(container);
            } catch (Exception e) {
                fail("Se produjo una excepción al intentar eliminar de una lista vacía: " + e.getMessage());
            }
        });
        
        // Verificamos que sigue habiendo 0 demonios
        assertEquals(0, container.getDemons().size(), "Debería seguir habiendo 0 demonios");
    }
    
    /**
     * PRUEBAS DE GESTIÓN DE PARTÍCULAS (Requisito 3: Add/Delete Particle)
     */
    @Test
    public void testAddParticle() {
        // QUÉ DEBERÍA HACER: Añadir partículas correctamente a las cámaras
        int initialLeftCount = container.getLeftChamber().size();
        int initialRightCount = container.getRightChamber().size();
        
        // Añadir una partícula a la izquierda
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod(
                    "addParticle", int.class, int.class, boolean.class);
                method.setAccessible(true);
                // Posición izquierda (dentro del contenedor)
                int leftX = container.getLeftMargin() + 50;
                int y = container.getTopMargin() + 50;
                method.invoke(container, leftX, y, true); // partícula rápida
            } catch (Exception e) {
                fail("No se pudo invocar addParticle para la izquierda: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Añadir una partícula a la derecha
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod(
                    "addParticle", int.class, int.class, boolean.class);
                method.setAccessible(true);
                // Posición derecha (dentro del contenedor)
                int rightX = container.getLeftMargin() + container.getContainerWidth() - 50;
                int y = container.getTopMargin() + 50;
                method.invoke(container, rightX, y, false); // partícula lenta
            } catch (Exception e) {
                fail("No se pudo invocar addParticle para la derecha: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que se añadieron las partículas
        assertEquals(initialLeftCount + 1, container.getLeftChamber().size(), 
                   "Debería haberse añadido una partícula a la cámara izquierda");
        assertEquals(initialRightCount + 1, container.getRightChamber().size(), 
                   "Debería haberse añadido una partícula a la cámara derecha");
    }
    
    @Test
    public void testAddParticleOutsideBounds() {
        // QUÉ NO DEBERÍA HACER: No debería añadir partículas fuera de los límites
        int initialLeftCount = container.getLeftChamber().size();
        int initialRightCount = container.getRightChamber().size();
        
        // Intentar añadir fuera del contenedor
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod(
                    "addParticle", int.class, int.class, boolean.class);
                method.setAccessible(true);
                // Posición fuera del contenedor
                int outsideX = 10; // Demasiado a la izquierda
                int y = container.getTopMargin() + 50;
                method.invoke(container, outsideX, y, true);
            } catch (Exception e) {
                fail("No se pudo invocar addParticle: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que no se añadieron partículas
        assertEquals(initialLeftCount, container.getLeftChamber().size(), 
                   "No debería haberse añadido una partícula fuera de límites");
        assertEquals(initialRightCount, container.getRightChamber().size(), 
                   "No debería haberse añadido una partícula fuera de límites");
    }
    
    @Test
    public void testRemoveParticle() {
        // QUÉ DEBERÍA HACER: Eliminar una partícula cuando se hace clic en ella
        
        // Primero, aseguremos que hay partículas
        assertTrue(container.getLeftChamber().size() > 0, "Debe haber partículas para esta prueba");
        
        // Obtener la primera partícula y su posición
        Particle firstParticle = container.getLeftChamber().get(0);
        int particleX = (int)firstParticle.getX();
        int particleY = (int)firstParticle.getY();
        int initialCount = container.getLeftChamber().size();
        
        // Simular clic en la partícula para eliminarla
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod(
                    "removeParticle", int.class, int.class);
                method.setAccessible(true);
                method.invoke(container, particleX, particleY);
            } catch (Exception e) {
                fail("No se pudo invocar removeParticle: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que se eliminó una partícula
        assertEquals(initialCount - 1, container.getLeftChamber().size(),
                   "Debería haberse eliminado una partícula");
    }
    
    /**
     * PRUEBAS DE AGUJEROS NEGROS (Requisito 4: Add Hole)
     */
    @Test
    public void testAddBlackHole() {
        // QUÉ DEBERÍA HACER: Añadir un agujero negro correctamente
        int initialCount = container.getBlackHoles().size();
        
        // Añadir un agujero negro
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod(
                    "addBlackHole", int.class, int.class);
                method.setAccessible(true);
                int x = container.getLeftMargin() + 100;
                int y = container.getTopMargin() + 100;
                method.invoke(container, x, y);
            } catch (Exception e) {
                fail("No se pudo invocar addBlackHole: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que se añadió el agujero negro
        assertEquals(initialCount + 1, container.getBlackHoles().size(),
                   "Debería haberse añadido un agujero negro");
    }
    
    @Test
    public void testRemoveBlackHole() {
        // QUÉ DEBERÍA HACER: Eliminar un agujero negro cuando se hace clic en él
        
        // Primero asegurar que hay un agujero negro
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod(
                    "addBlackHole", int.class, int.class);
                method.setAccessible(true);
                int x = container.getLeftMargin() + 150;
                int y = container.getTopMargin() + 150;
                method.invoke(container, x, y);
            } catch (Exception e) {
                fail("No se pudo invocar addBlackHole: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int initialCount = container.getBlackHoles().size();
        assertTrue(initialCount > 0, "Debe haber agujeros negros para esta prueba");
        
        // Obtener el último agujero negro añadido y su posición
        Hole blackHole = container.getBlackHoles().get(initialCount - 1);
        int bhX = (int)blackHole.getX();
        int bhY = (int)blackHole.getY();
        
        // Simular clic en el agujero negro para eliminarlo
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod(
                    "removeBlackHole", int.class, int.class);
                method.setAccessible(true);
                method.invoke(container, bhX, bhY);
            } catch (Exception e) {
                fail("No se pudo invocar removeBlackHole: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que se eliminó el agujero negro
        assertEquals(initialCount - 1, container.getBlackHoles().size(),
                   "Debería haberse eliminado un agujero negro");
    }
    
    /**
     * PRUEBAS DE SIMULACIÓN (Requisito 5: Start)
     */
    @Test
    public void testSimulationStep() {
        // QUÉ DEBERÍA HACER: La simulación debe avanzar correctamente
        
        // Tomar las posiciones iniciales de algunas partículas
        ArrayList<Double> initialXPositions = new ArrayList<>();
        ArrayList<Double> initialYPositions = new ArrayList<>();
        
        for (int i = 0; i < Math.min(5, container.getLeftChamber().size()); i++) {
            Particle p = container.getLeftChamber().get(i);
            initialXPositions.add(p.getX());
            initialYPositions.add(p.getY());
        }
        
        // Ejecutar un paso de simulación
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod("updateSimulation");
                method.setAccessible(true);
                method.invoke(container);
            } catch (Exception e) {
                fail("No se pudo invocar updateSimulation: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que las posiciones han cambiado
        boolean positionsChanged = false;
        for (int i = 0; i < initialXPositions.size(); i++) {
            Particle p = container.getLeftChamber().get(i);
            if (Math.abs(p.getX() - initialXPositions.get(i)) > 0.001 || 
                Math.abs(p.getY() - initialYPositions.get(i)) > 0.001) {
                positionsChanged = true;
                break;
            }
        }
        
        assertTrue(positionsChanged, "Las posiciones de las partículas deberían cambiar tras un paso de simulación");
    }
    
    /**
     * PRUEBAS DE COLORES DE PARTÍCULAS
     */
    @Test
    public void testSetParticleColors() {
        // QUÉ DEBERÍA HACER: Permitir cambiar los colores de partículas
        Color initialFastColor = container.getFastParticleColor();
        Color initialSlowColor = container.getSlowParticleColor();
        
        // Elegir colores diferentes a los actuales
        Color newFastColor = new Color(0, 128, 0); // Verde
        if (newFastColor.equals(initialFastColor) || newFastColor.equals(initialSlowColor)) {
            newFastColor = new Color(128, 0, 128); // Púrpura
        }
        
        // Cambiar color de partículas rápidas
        final Color fastColorFinal = newFastColor;
        SwingUtilities.invokeLater(() -> {
            try {
                container.setFastParticleColor(fastColorFinal);
            } catch (Exception e) {
                fail("Error al cambiar color de partículas rápidas: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que el color cambió
        assertEquals(newFastColor, container.getFastParticleColor(),
                   "El color de las partículas rápidas debería haber cambiado");
    }
    
    @Test
    public void testTrySetSameColorForBothTypes() {
        // QUÉ NO DEBERÍA HACER: No permitir que ambos tipos tengan el mismo color
        Color initialFastColor = container.getFastParticleColor();
        Color initialSlowColor = container.getSlowParticleColor();
        
        // Intentar establecer el color de partículas rápidas igual al de lentas
        SwingUtilities.invokeLater(() -> {
            try {
                container.setFastParticleColor(initialSlowColor);
            } catch (Exception e) {
                fail("Error inesperado: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que el color no cambió
        assertEquals(initialFastColor, container.getFastParticleColor(),
                   "El color no debería cambiarse cuando se intenta usar el mismo color para ambos tipos");
    }
    
    /**
     * PRUEBAS DE VISIBILIDAD (Requisito 8: Make Visible/Invisible)
     */
    @Test
    public void testHideAndShowSimulator() {
        // QUÉ DEBERÍA HACER: Ocultar y mostrar el simulador correctamente
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Primero aseguramos que está visible
                java.lang.reflect.Method showMethod = MaxwellContainer.class.getDeclaredMethod("showSimulator");
                showMethod.setAccessible(true);
                showMethod.invoke(container);
                
                // Luego lo ocultamos
                java.lang.reflect.Method hideMethod = MaxwellContainer.class.getDeclaredMethod("hideSimulator");
                hideMethod.setAccessible(true);
                hideMethod.invoke(container);
            } catch (Exception e) {
                fail("Error al ocultar el simulador: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que el simulador está oculto
        assertFalse(container.isVisible(), "El simulador debería estar oculto");
        
        // Volver a mostrar el simulador
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Method showMethod = MaxwellContainer.class.getDeclaredMethod("showSimulator");
                showMethod.setAccessible(true);
                showMethod.invoke(container);
            } catch (Exception e) {
                fail("Error al mostrar el simulador: " + e.getMessage());
            }
        });
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que el simulador está visible
        assertTrue(container.isVisible(), "El simulador debería estar visible");
    }
    
    /**
     * PRUEBAS DE FINALIZACIÓN (Requisito 9: Finish)
     * (No podemos probar directamente el cierre completo de la aplicación,
     * pero podemos verificar que el método existe)
     */
    @Test
    public void testConfirmExitMethodExists() {
        // Verificar que existe el método confirmExit
        try {
            java.lang.reflect.Method method = MaxwellContainer.class.getDeclaredMethod("confirmExit");
            assertNotNull(method, "El método confirmExit debería existir");
        } catch (NoSuchMethodException e) {
            fail("No se encontró el método confirmExit: " + e.getMessage());
        }
    }
}