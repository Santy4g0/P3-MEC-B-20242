/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.parcial_eps;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

/**Parcial_EPS
 *
 * @author Juan David Ruiz Gomez, Elkin Santiago Ruiz Rodriguez
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class Parcial_EPS extends JFrame {

    private JTextField txtCedula;
    private JComboBox<String> cmbCategoria;
    private JComboBox<String> cmbServicio;
    private JButton btnRegistrar;
    private JTable tblGeneral, tblEspecializada, tblLaboratorio, tblImagenes;
    private DefaultTableModel modeloGeneral, modeloEspecializada, modeloLaboratorio, modeloImagenes;
    private JSlider sliderTiempo;
    private JLabel lblEstadoCola, lblProximaAtencion;
    
    // Colas por categorías de servicio
    private Queue<Paciente> colaConsultaGeneral;
    private Queue<Paciente> colaConsultaEspecializada;
    private Queue<Paciente> colaPruebaLaboratorio;
    private Queue<Paciente> colaImagenesDiagnosticas;

    private Timer temporizador;
    private int velocidadAtencion = 1000; // 1 segundo = 1 minuto
    private int tiempoBase = 1000;

    public Parcial_EPS() {
        setTitle("Simulación Atención en EPS");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Inicializar las colas
        colaConsultaGeneral = new LinkedList<>();
        colaConsultaEspecializada = new LinkedList<>();
        colaPruebaLaboratorio = new LinkedList<>();
        colaImagenesDiagnosticas = new LinkedList<>();

        // Componentes
        JLabel lblCedula = new JLabel("Cédula:");
        lblCedula.setBounds(20, 20, 100, 30);
        add(lblCedula);

        txtCedula = new JTextField();
        txtCedula.setBounds(100, 20, 150, 30);
        add(txtCedula);

        JLabel lblCategoria = new JLabel("Categoría:");
        lblCategoria.setBounds(20, 60, 100, 30);
        add(lblCategoria);

        cmbCategoria = new JComboBox<>(new String[]{
                "Menor de 60 años", 
                "Adulto mayor", 
                "Persona con discapacidad"
        });
        cmbCategoria.setBounds(100, 60, 200, 30);
        add(cmbCategoria);

        JLabel lblServicio = new JLabel("Servicio:");
        lblServicio.setBounds(20, 100, 100, 30);
        add(lblServicio);

        // Servicios disponibles
        cmbServicio = new JComboBox<>(new String[]{
                "Consulta médica general",
                "Consulta médica especializada",
                "Prueba de laboratorio",
                "Imágenes diagnósticas"
        });
        cmbServicio.setBounds(100, 100, 200, 30);
        add(cmbServicio);

        btnRegistrar = new JButton("Registrar Paciente");
        btnRegistrar.setBounds(100, 140, 150, 30);
        add(btnRegistrar);

        // Tablas para cada servicio
        modeloGeneral = new DefaultTableModel(new Object[]{"Cédula", "Categoría", "Hora de llegada"}, 0);
        tblGeneral = new JTable(modeloGeneral);
        JScrollPane scrollGeneral = new JScrollPane(tblGeneral);
        scrollGeneral.setBounds(320, 20, 250, 100);
        add(scrollGeneral);

        modeloEspecializada = new DefaultTableModel(new Object[]{"Cédula", "Categoría", "Hora de llegada"}, 0);
        tblEspecializada = new JTable(modeloEspecializada);
        JScrollPane scrollEspecializada = new JScrollPane(tblEspecializada);
        scrollEspecializada.setBounds(320, 140, 250, 100);
        add(scrollEspecializada);

        modeloLaboratorio = new DefaultTableModel(new Object[]{"Cédula", "Categoría", "Hora de llegada"}, 0);
        tblLaboratorio = new JTable(modeloLaboratorio);
        JScrollPane scrollLaboratorio = new JScrollPane(tblLaboratorio);
        scrollLaboratorio.setBounds(320, 260, 250, 100);
        add(scrollLaboratorio);

        modeloImagenes = new DefaultTableModel(new Object[]{"Cédula", "Categoría", "Hora de llegada"}, 0);
        tblImagenes = new JTable(modeloImagenes);
        JScrollPane scrollImagenes = new JScrollPane(tblImagenes);
        scrollImagenes.setBounds(320, 380, 250, 100);
        add(scrollImagenes);

        // Control deslizante para ajustar el tiempo
        JLabel lblTiempo = new JLabel("Ajustar tiempo:");
        lblTiempo.setBounds(20, 200, 200, 30);
        add(lblTiempo);

        sliderTiempo = new JSlider(JSlider.HORIZONTAL, -1000, 1000, 0); // Ajuste para tiempo
        sliderTiempo.setBounds(20, 230, 200, 50);
        sliderTiempo.setMajorTickSpacing(500);
        sliderTiempo.setPaintTicks(true);
        sliderTiempo.setPaintLabels(true);
        add(sliderTiempo);

        // Estado de la cola
        lblEstadoCola = new JLabel("Pacientes en cola: 0");
        lblEstadoCola.setBounds(20, 300, 300, 30);
        add(lblEstadoCola);

        // Cuadro de próxima atención
        lblProximaAtencion = new JLabel("Próxima atención: ");
        lblProximaAtencion.setBounds(20, 340, 300, 50);
        add(lblProximaAtencion);

        // Listeners
        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarPaciente();
            }
        });

        sliderTiempo.addChangeListener(e -> {
            ajustarVelocidadAtencion(sliderTiempo.getValue());
        });

        // Temporizador para la simulación de atención
        temporizador = new Timer();
        temporizador.schedule(new TimerTask() {
            @Override
            public void run() {
                atenderPacientes();
            }
        }, 0, velocidadAtencion);

        setVisible(true);
    }

    // Ajustar la velocidad del temporizador según el deslizador
    private void ajustarVelocidadAtencion(int valor) {
        velocidadAtencion = tiempoBase + valor;
    }

    // Registrar paciente y agregarlo a la cola correspondiente
    private void registrarPaciente() {
        String cedula = txtCedula.getText();
        String categoria = (String) cmbCategoria.getSelectedItem();
        String servicio = (String) cmbServicio.getSelectedItem();
        String horaLlegada = new SimpleDateFormat("HH:mm:ss").format(new Date());

        // Validar cédula numérica
        if (!cedula.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "La cédula debe contener solo números.");
            return;
        }

        Paciente paciente = new Paciente(cedula, categoria, servicio, horaLlegada, System.currentTimeMillis());

        // Agregar paciente a la cola correspondiente
        switch (servicio) {
            case "Consulta médica general":
                colaConsultaGeneral.add(paciente);
                modeloGeneral.addRow(new Object[]{cedula, categoria, horaLlegada});
                break;
            case "Consulta médica especializada":
                colaConsultaEspecializada.add(paciente);
                modeloEspecializada.addRow(new Object[]{cedula, categoria, horaLlegada});
                break;
            case "Prueba de laboratorio":
                colaPruebaLaboratorio.add(paciente);
                modeloLaboratorio.addRow(new Object[]{cedula, categoria, horaLlegada});
                break;
            case "Imágenes diagnósticas":
                colaImagenesDiagnosticas.add(paciente);
                modeloImagenes.addRow(new Object[]{cedula, categoria, horaLlegada});
                break;
        }

        actualizarEstadoCola();
    }

    // Atender pacientes según las colas
    private void atenderPacientes() {
        Paciente pacienteAtendido = null;

        // Atender de la cola de "Consulta médica general"
        if (!colaConsultaGeneral.isEmpty()) {
            pacienteAtendido = colaConsultaGeneral.poll();
            modeloGeneral.removeRow(0);
            simularDuracionServicio(pacienteAtendido, 3); // 3 minutos para consulta general
        }
        // Atender de la cola de "Consulta médica especializada"
        else if (!colaConsultaEspecializada.isEmpty()) {
            pacienteAtendido = colaConsultaEspecializada.poll();
            modeloEspecializada.removeRow(0);
            simularDuracionServicio(pacienteAtendido, 5); // 5 minutos para consulta especializada
        }
        // Atender de la cola de "Prueba de laboratorio"
        else if (!colaPruebaLaboratorio.isEmpty()) {
            pacienteAtendido = colaPruebaLaboratorio.poll();
            modeloLaboratorio.removeRow(0);
            simularDuracionServicio(pacienteAtendido, 2); // 2 minutos para pruebas de laboratorio
        }
        // Atender de la cola de "Imágenes diagnósticas"
        else if (!colaImagenesDiagnosticas.isEmpty()) {
            pacienteAtendido = colaImagenesDiagnosticas.poll();
            modeloImagenes.removeRow(0);
            simularDuracionServicio(pacienteAtendido, 4); // 4 minutos para imágenes diagnósticas
        }

        actualizarEstadoCola();
    }

    // Simular la duración del servicio
    private void simularDuracionServicio(Paciente paciente, int duracionMinutos) {
        long tiempoEspera = (System.currentTimeMillis() - paciente.getHoraLlegada()) / 60000;
        lblProximaAtencion.setText("Próxima atención: Cédula " + paciente.getCedula() + ", Categoría " + paciente.getCategoria() + ", Servicio " + paciente.getServicio());
        JOptionPane.showMessageDialog(this, "Atendiendo a paciente: " + paciente.getCedula() + " con tiempo de espera de " + tiempoEspera + " minutos.\nDuración del servicio: " + duracionMinutos + " minutos.");
    }

    // Actualizar el estado de la cola en la interfaz
    private void actualizarEstadoCola() {
        int totalPacientes = colaConsultaGeneral.size() + colaConsultaEspecializada.size() + colaPruebaLaboratorio.size() + colaImagenesDiagnosticas.size();
        lblEstadoCola.setText("Pacientes en cola: " + totalPacientes);
    }

    // Clase para representar a un paciente
    class Paciente {
        private String cedula;
        private String categoria;
        private String servicio;
        private String horaLlegadaTexto;
        private long horaLlegada;

        public Paciente(String cedula, String categoria, String servicio, String horaLlegadaTexto, long horaLlegada) {
            this.cedula = cedula;
            this.categoria = categoria;
            this.servicio = servicio;
            this.horaLlegadaTexto = horaLlegadaTexto;
            this.horaLlegada = horaLlegada;
        }

        public String getCedula() {
            return cedula;
        }

        public String getCategoria() {
            return categoria;
        }

        public String getServicio() {
            return servicio;
        }

        public long getHoraLlegada() {
            return horaLlegada;
        }

        public String getHoraLlegadaTexto() {
            return horaLlegadaTexto;
        }
    }

    public static void main(String[] args) {
        new Parcial_EPS();
    }
}

