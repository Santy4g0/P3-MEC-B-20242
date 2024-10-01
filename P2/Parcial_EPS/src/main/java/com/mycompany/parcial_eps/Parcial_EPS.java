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
    private JLabel lblEstadoCola, lblProximaAtencion, lblPacienteActual;

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

        // Panel izquierdo (Registro de pacientes y deslizador)
        JPanel panelIzquierdo = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Componentes del registro de pacientes
        gbc.gridx = 0; gbc.gridy = 0;
        panelIzquierdo.add(new JLabel("Registro de Pacientes"), gbc);

        gbc.gridy = 1;
        panelIzquierdo.add(new JLabel("Cédula:"), gbc);

        gbc.gridx = 1;
        txtCedula = new JTextField(15);
        panelIzquierdo.add(txtCedula, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panelIzquierdo.add(new JLabel("Categoría:"), gbc);

        gbc.gridx = 1;
        cmbCategoria = new JComboBox<>(new String[]{
                "Menor de 60 años", 
                "Adulto mayor", 
                "Persona con discapacidad"
        });
        panelIzquierdo.add(cmbCategoria, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panelIzquierdo.add(new JLabel("Servicio:"), gbc);

        gbc.gridx = 1;
        cmbServicio = new JComboBox<>(new String[]{
                "Consulta médica general",
                "Consulta médica especializada",
                "Prueba de laboratorio",
                "Imágenes diagnósticas"
        });
        panelIzquierdo.add(cmbServicio, gbc);

        gbc.gridy = 4;
        btnRegistrar = new JButton("Registrar Paciente");
        panelIzquierdo.add(btnRegistrar, gbc);

        // Deslizador de tiempo
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panelIzquierdo.add(new JLabel("Ajustar tiempo:"), gbc);

        gbc.gridy = 6;
        sliderTiempo = new JSlider(JSlider.HORIZONTAL, -1000, 1000, 0);
        sliderTiempo.setMajorTickSpacing(500);
        sliderTiempo.setPaintTicks(true);
        sliderTiempo.setPaintLabels(true);
        panelIzquierdo.add(sliderTiempo, gbc);

        add(panelIzquierdo, BorderLayout.WEST);

        // Panel derecho (Próxima atención y tablas de colas)
        JPanel panelDerecho = new JPanel();
        panelDerecho.setLayout(new BorderLayout());

        // Panel para la próxima atención
        JPanel panelTurnos = new JPanel();
        panelTurnos.setBorder(BorderFactory.createTitledBorder("Turnos"));
        lblProximaAtencion = new JLabel("Próxima atención: ");
        lblPacienteActual = new JLabel("Atendiendo a: ");
        panelTurnos.add(lblProximaAtencion);
        panelTurnos.add(lblPacienteActual);
        panelDerecho.add(panelTurnos, BorderLayout.NORTH);

        // Panel para las tablas de las colas
        JPanel panelTablas = new JPanel(new GridLayout(4, 1));
        modeloGeneral = new DefaultTableModel(new Object[]{"Cédula", "Condición", "Hora de llegada"}, 0);
        tblGeneral = new JTable(modeloGeneral);
        panelTablas.add(new JScrollPane(tblGeneral));

        modeloEspecializada = new DefaultTableModel(new Object[]{"Cédula", "Condición", "Hora de llegada"}, 0);
        tblEspecializada = new JTable(modeloEspecializada);
        panelTablas.add(new JScrollPane(tblEspecializada));

        modeloLaboratorio = new DefaultTableModel(new Object[]{"Cédula", "Condición", "Hora de llegada"}, 0);
        tblLaboratorio = new JTable(modeloLaboratorio);
        panelTablas.add(new JScrollPane(tblLaboratorio));

        modeloImagenes = new DefaultTableModel(new Object[]{"Cédula", "Condición", "Hora de llegada"}, 0);
        tblImagenes = new JTable(modeloImagenes);
        panelTablas.add(new JScrollPane(tblImagenes));

        panelDerecho.add(panelTablas, BorderLayout.CENTER);

        add(panelDerecho, BorderLayout.CENTER);

        // Estado de la cola
        lblEstadoCola = new JLabel("Pacientes en cola: 0");
        add(lblEstadoCola, BorderLayout.SOUTH);

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

        if (pacienteAtendido != null) {
            lblPacienteActual.setText("Atendiendo a: " + pacienteAtendido.getCedula() + 
                                      " - Servicio: " + pacienteAtendido.getServicio());
            lblProximaAtencion.setText("Próxima atención: " + (totalPacientesEnCola() > 0 ? 
                obtenerProximoPaciente().getCedula() : "Nadie en espera"));
        }

        actualizarEstadoCola();
    }

    // Obtener el próximo paciente sin eliminarlo de la cola
    private Paciente obtenerProximoPaciente() {
        if (!colaConsultaGeneral.isEmpty()) {
            return colaConsultaGeneral.peek();
        } else if (!colaConsultaEspecializada.isEmpty()) {
            return colaConsultaEspecializada.peek();
        } else if (!colaPruebaLaboratorio.isEmpty()) {
            return colaPruebaLaboratorio.peek();
        } else if (!colaImagenesDiagnosticas.isEmpty()) {
            return colaImagenesDiagnosticas.peek();
        }
        return null; // No hay pacientes en cola
    }

    // Simular la duración del servicio
    private void simularDuracionServicio(Paciente paciente, int duracionEnMinutos) {
        try {
            Thread.sleep(duracionEnMinutos * velocidadAtencion);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Actualizar el estado de la cola
    private void actualizarEstadoCola() {
        int totalPacientes = totalPacientesEnCola();
        lblEstadoCola.setText("Pacientes en cola: " + totalPacientes);
    }

    // Obtener el total de pacientes en todas las colas
    private int totalPacientesEnCola() {
        return colaConsultaGeneral.size() + colaConsultaEspecializada.size() +
                colaPruebaLaboratorio.size() + colaImagenesDiagnosticas.size();
    }

    // Clase para representar a un paciente
    private static class Paciente {
        private String cedula;
        private String categoria;
        private String servicio;
        private String horaLlegada;
        private long tiempoRegistro;

        public Paciente(String cedula, String categoria, String servicio, String horaLlegada, long tiempoRegistro) {
            this.cedula = cedula;
            this.categoria = categoria;
            this.servicio = servicio;
            this.horaLlegada = horaLlegada;
            this.tiempoRegistro = tiempoRegistro;
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

        public String getHoraLlegada() {
            return horaLlegada;
        }

        public long getTiempoRegistro() {
            return tiempoRegistro;
        }
    }

    public static void main(String[] args) {
        new Parcial_EPS();
    }
}
