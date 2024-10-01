/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.parcial_eps;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

/**
 *
 * @author Juan David Ruiz Gomez, Elkin Santiago Ruiz Rodriguez
 */

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla completa
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Inicializar las colas
        colaConsultaGeneral = new LinkedList<>();
        colaConsultaEspecializada = new LinkedList<>();
        colaPruebaLaboratorio = new LinkedList<>();
        colaImagenesDiagnosticas = new LinkedList<>();

        // Panel principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new GridBagLayout());
        add(panelPrincipal, BorderLayout.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Componentes
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.weighty = 0.1; // Espacio para los campos
        panelPrincipal.add(new JLabel("Registro de Pacientes"), gbc);

        gbc.weighty = 0.1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panelPrincipal.add(new JLabel("Cédula:"), gbc);

        gbc.gridx = 1;
        txtCedula = new JTextField(15);
        panelPrincipal.add(txtCedula, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panelPrincipal.add(new JLabel("Categoría:"), gbc);

        gbc.gridx = 1;
        cmbCategoria = new JComboBox<>(new String[]{
                "Menor de 60 años", 
                "Adulto mayor", 
                "Persona con discapacidad"
        });
        panelPrincipal.add(cmbCategoria, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panelPrincipal.add(new JLabel("Servicio:"), gbc);

        gbc.gridx = 1;
        cmbServicio = new JComboBox<>(new String[]{
                "Consulta médica general",
                "Consulta médica especializada",
                "Prueba de laboratorio",
                "Imágenes diagnósticas"
        });
        panelPrincipal.add(cmbServicio, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        btnRegistrar = new JButton("Registrar Paciente");
        panelPrincipal.add(btnRegistrar, gbc);

        // Tablas para cada servicio
        modeloGeneral = new DefaultTableModel(new Object[]{"Cédula", "Condición", "Hora de llegada"}, 0);
        tblGeneral = new JTable(modeloGeneral);
        JScrollPane scrollGeneral = new JScrollPane(tblGeneral);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.weighty = 0.2;
        panelPrincipal.add(scrollGeneral, gbc);

        modeloEspecializada = new DefaultTableModel(new Object[]{"Cédula", "Condición", "Hora de llegada"}, 0);
        tblEspecializada = new JTable(modeloEspecializada);
        JScrollPane scrollEspecializada = new JScrollPane(tblEspecializada);
        gbc.gridy = 6;
        panelPrincipal.add(scrollEspecializada, gbc);

        modeloLaboratorio = new DefaultTableModel(new Object[]{"Cédula", "Condición", "Hora de llegada"}, 0);
        tblLaboratorio = new JTable(modeloLaboratorio);
        JScrollPane scrollLaboratorio = new JScrollPane(tblLaboratorio);
        gbc.gridy = 7;
        panelPrincipal.add(scrollLaboratorio, gbc);

        modeloImagenes = new DefaultTableModel(new Object[]{"Cédula", "Condición", "Hora de llegada"}, 0);
        tblImagenes = new JTable(modeloImagenes);
        JScrollPane scrollImagenes = new JScrollPane(tblImagenes);
        gbc.gridy = 8;
        panelPrincipal.add(scrollImagenes, gbc);

        // Control deslizante para ajustar el tiempo
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1;
        panelPrincipal.add(new JLabel("Ajustar tiempo:"), gbc);

        gbc.gridx = 1;
        sliderTiempo = new JSlider(JSlider.HORIZONTAL, -1000, 1000, 0);
        sliderTiempo.setMajorTickSpacing(500);
        sliderTiempo.setPaintTicks(true);
        sliderTiempo.setPaintLabels(true);
        panelPrincipal.add(sliderTiempo, gbc);

        // Estado de la cola
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        lblEstadoCola = new JLabel("Pacientes en cola: 0");
        panelPrincipal.add(lblEstadoCola, gbc);

        // Cuadro de próxima atención
        gbc.gridy = 11; gbc.weighty = 0.2; // Mayor espacio para la próxima atención
        JPanel panelProximaAtencion = new JPanel();
        panelProximaAtencion.setBorder(BorderFactory.createTitledBorder("Próxima atención"));
        lblProximaAtencion = new JLabel("Próxima atención: ");
        panelProximaAtencion.add(lblProximaAtencion);
        panelProximaAtencion.setPreferredSize(new Dimension(400, 100));
        panelPrincipal.add(panelProximaAtencion, gbc);

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
        if (totalPacientesEnCola() < 10) {
            return; // No atender hasta que haya al menos 10 pacientes en cola
        }

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

    private int totalPacientesEnCola() {
        return colaConsultaGeneral.size() + colaConsultaEspecializada.size() + colaPruebaLaboratorio.size() + colaImagenesDiagnosticas.size();
    }

    // Simular la duración del servicio
    private void simularDuracionServicio(Paciente paciente, int duracionMinutos) {
        long tiempoEspera = (System.currentTimeMillis() - paciente.getHoraLlegada()) / 60000;
        lblProximaAtencion.setText("Próxima atención: Cédula " + paciente.getCedula() + ", Categoría " + paciente.getCategoria() + ", Servicio " + paciente.getServicio());
        JOptionPane.showMessageDialog(this, "Atendiendo a paciente: " + paciente.getCedula() + " con tiempo de espera de " + tiempoEspera + " minutos.\nDuración del servicio: " + duracionMinutos + " minutos.");
    }

    // Actualizar el estado de la cola en la interfaz
    private void actualizarEstadoCola() {
        int totalPacientes = totalPacientesEnCola();
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
        SwingUtilities.invokeLater(Parcial_EPS::new);
    }
}
