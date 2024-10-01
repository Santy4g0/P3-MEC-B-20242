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
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class Parcial_EPS extends JFrame {
    
    private JTextField txtCedula;
    private JComboBox<String> cmbCategoria;
    private JComboBox<String> cmbServicio;
    private JButton btnRegistrar;
    private JTable tblPacientes;
    private DefaultTableModel modeloTabla;
    private JSlider sliderTiempo;
    private JLabel lblEstadoCola;
    private Queue<Paciente> colaPacientes;
    private Timer temporizador;
    private int velocidadAtencion = 1000; // 1 segundo = 1 minuto

    public Parcial_EPS() {
        setTitle("Simulación Atención en EPS");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Inicializar la cola de pacientes
        colaPacientes = new LinkedList<>();

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

        cmbCategoria = new JComboBox<>(new String[]{"Menor de 60 años", "Adulto mayor", "Persona con discapacidad"});
        cmbCategoria.setBounds(100, 60, 150, 30);
        add(cmbCategoria);

        JLabel lblServicio = new JLabel("Servicio:");
        lblServicio.setBounds(20, 100, 100, 30);
        add(lblServicio);

        cmbServicio = new JComboBox<>(new String[]{"Consulta médica general", "Consulta médica especializada", "Prueba de laboratorio", "Imágenes diagnósticas"});
        cmbServicio.setBounds(100, 100, 150, 30);
        add(cmbServicio);

        btnRegistrar = new JButton("Registrar Paciente");
        btnRegistrar.setBounds(100, 140, 150, 30);
        add(btnRegistrar);

        // Tabla para mostrar pacientes
        modeloTabla = new DefaultTableModel(new Object[]{"Cédula", "Categoría", "Servicio", "Hora de llegada"}, 0);
        tblPacientes = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tblPacientes);
        scrollPane.setBounds(270, 20, 300, 200);
        add(scrollPane);

        // Control deslizante para ajustar el tiempo
        JLabel lblTiempo = new JLabel("Ajustar tiempo (segundos):");
        lblTiempo.setBounds(20, 200, 200, 30);
        add(lblTiempo);

        sliderTiempo = new JSlider(500, 3000, 1000); // Ajuste de 0.5 segundos a 3 segundos
        sliderTiempo.setBounds(20, 230, 200, 50);
        sliderTiempo.setMajorTickSpacing(500);
        sliderTiempo.setPaintTicks(true);
        sliderTiempo.setPaintLabels(true);
        add(sliderTiempo);

        // Estado de la cola
        lblEstadoCola = new JLabel("Pacientes en cola: 0");
        lblEstadoCola.setBounds(20, 300, 200, 30);
        add(lblEstadoCola);

        // Listeners
        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarPaciente();
            }
        });

        sliderTiempo.addChangeListener(e -> {
            velocidadAtencion = sliderTiempo.getValue();
        });

        // Temporizador para la simulación de atención
        temporizador = new Timer();
        temporizador.schedule(new TimerTask() {
            @Override
            public void run() {
                if (colaPacientes.size() >= 10) {
                    atenderPaciente();
                }
            }
        }, 0, velocidadAtencion); // Se ajusta según el control deslizante

        setVisible(true);
    }

    private void registrarPaciente() {
        String cedula = txtCedula.getText();
        String categoria = (String) cmbCategoria.getSelectedItem();
        String servicio = (String) cmbServicio.getSelectedItem();
        String horaLlegada = new SimpleDateFormat("HH:mm:ss").format(new Date());

        // Validaciones
        if (cedula.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese la cédula del paciente.");
            return;
        }

        // Agregar paciente a la cola y a la tabla
        Paciente paciente = new Paciente(cedula, categoria, servicio, horaLlegada);
        colaPacientes.add(paciente);
        modeloTabla.addRow(new Object[]{cedula, categoria, servicio, horaLlegada});
        lblEstadoCola.setText("Pacientes en cola: " + colaPacientes.size());
    }

    private void atenderPaciente() {
        if (!colaPacientes.isEmpty()) {
            Paciente pacienteAtendido = colaPacientes.poll();
            modeloTabla.removeRow(0);
            lblEstadoCola.setText("Pacientes en cola: " + colaPacientes.size());
            JOptionPane.showMessageDialog(this, "Atendiendo a paciente con cédula: " + pacienteAtendido.getCedula());
        }
    }

    public static void main(String[] args) {
        new Parcial_EPS();
    }

    class Paciente {
        private String cedula;
        private String categoria;
        private String servicio;
        private String horaLlegada;

        public Paciente(String cedula, String categoria, String servicio, String horaLlegada) {
            this.cedula = cedula;
            this.categoria = categoria;
            this.servicio = servicio;
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

        public String getHoraLlegada() {
            return horaLlegada;
        }
    }
}
