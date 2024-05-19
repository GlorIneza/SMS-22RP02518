import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Student {
    private String name;
    private String regNumber;
    private int mathMarks;
    private int javaMarks;
    private int phpMarks;

    public Student(String name, String regNumber, int mathMarks, int javaMarks, int phpMarks) {
        this.name = name;
        this.regNumber = regNumber;
        this.mathMarks = mathMarks;
        this.javaMarks = javaMarks;
        this.phpMarks = phpMarks;
    }

    public String getName() {
        return name;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public int getMathMarks() {
        return mathMarks;
    }

    public int getJavaMarks() {
        return javaMarks;
    }

    public int getPhpMarks() {
        return phpMarks;
    }

    public double getAverageMarks() {
        return (mathMarks + javaMarks + phpMarks) / 3.0;
    }
}

class StudentManagement {
    private List<Student> students;
    private final String JDBC_URL = "jdbc:mariadb://localhost:3306/studentmanagementsystem";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    public StudentManagement() {
        students = new ArrayList<>();
        loadStudentsFromDatabase();
    }

    public void addStudent(Student student) {
        students.add(student);
        addStudentToDatabase(student);
    }

    public double calculateAverageMarks() {
        double total = 0;
        for (Student student : students) {
            total += student.getAverageMarks();
        }
        return students.size() > 0 ? total / students.size() : 0;
    }

    public Student getTopStudent() {
        if (students.isEmpty()) {
            return null;
        }
        Student topStudent = students.get(0);
        for (Student student : students) {
            if (student.getAverageMarks() > topStudent.getAverageMarks()) {
                topStudent = student;
            }
        }
        return topStudent;
    }

    public List<Student> getStudents() {
        return students;
    }

    private void loadStudentsFromDatabase() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {

            while (rs.next()) {
                String name = rs.getString("name");
                String regNumber = rs.getString("regNumber");
                int mathMarks = rs.getInt("mathMarks");
                int javaMarks = rs.getInt("javaMarks");
                int phpMarks = rs.getInt("phpMarks");

                Student student = new Student(name, regNumber, mathMarks, javaMarks, phpMarks);
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addStudentToDatabase(Student student) {
        String sql = "INSERT INTO students (name, regNumber, mathMarks, javaMarks, phpMarks) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getRegNumber());
            pstmt.setInt(3, student.getMathMarks());
            pstmt.setInt(4, student.getJavaMarks());
            pstmt.setInt(5, student.getPhpMarks());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class StudentManager extends JFrame {
    private StudentManagement studentManagement;

    private JTextField nameField;
    private JTextField regNumberField;
    private JTextField mathMarksField;
    private JTextField javaMarksField;
    private JTextField phpMarksField;

    private DefaultTableModel tableModel;

    public StudentManager() {
        studentManagement = new StudentManagement();

        setTitle("Student Management System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));

        panel.add(new JLabel("Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Reg Number:"));
        regNumberField = new JTextField();
        panel.add(regNumberField);

        panel.add(new JLabel("Math Marks:"));
        mathMarksField = new JTextField();
        panel.add(mathMarksField);

        panel.add(new JLabel("Java Marks:"));
        javaMarksField = new JTextField();
        panel.add(javaMarksField);

        panel.add(new JLabel("PHP Marks:"));
        phpMarksField = new JTextField();
        panel.add(phpMarksField);

        JButton addButton = new JButton("Add Student");
        addButton.setBackground(Color.GREEN);
        panel.add(addButton);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStudent();
            }
        });

        JButton exitButton = new JButton("Exit");
        exitButton.setBackground(Color.RED);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        panel.add(exitButton);

        getContentPane().add(panel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Name", "Reg Number", "Math", "Java", "PHP", "Average"}, 0);
        JTable table = new JTable(tableModel);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(1, 2));

        JButton averageButton = new JButton("Show Average Marks");
        statsPanel.add(averageButton);
        averageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAverageMarks();
            }
        });

        JButton topStudentButton = new JButton("Show Top Student");
        statsPanel.add(topStudentButton);
        topStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTopStudent();
            }
        });

        getContentPane().add(statsPanel, BorderLayout.SOUTH);

        // Load existing student data from the database and display it in the table
        List<Student> existingStudents = studentManagement.getStudents();
        for (Student student : existingStudents) {
            tableModel.addRow(new Object[]{student.getName(), student.getRegNumber(), student.getMathMarks(), student.getJavaMarks(), student.getPhpMarks(), student.getAverageMarks()});
        }
    }

    private void addStudent() {
        try {
            String name = nameField.getText().trim();
            String regNumber = regNumberField.getText().trim();
            int mathMarks = Integer.parseInt(mathMarksField.getText().trim());
            int javaMarks = Integer.parseInt(javaMarksField.getText().trim());
            int phpMarks = Integer.parseInt(phpMarksField.getText().trim());

            if (mathMarks < 0 || mathMarks > 100 || javaMarks < 0 || javaMarks > 100 || phpMarks < 0 || phpMarks > 100) {
                JOptionPane.showMessageDialog(this, "Marks must be between 0 and 100.");
                return;
            }

            Student student = new Student(name, regNumber, mathMarks, javaMarks, phpMarks);
            studentManagement.addStudent(student);

            tableModel.addRow(new Object[]{name, regNumber, mathMarks, javaMarks, phpMarks, student.getAverageMarks()});
            clearFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for marks.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nameField.setText("");
        regNumberField.setText("");
        mathMarksField.setText("");
        javaMarksField.setText("");
        phpMarksField.setText("");
    }

    private void showAverageMarks() {
        double average = studentManagement.calculateAverageMarks();
        JOptionPane.showMessageDialog(this, "Average marks of all students: " + average);
    }

    private void showTopStudent() {
        Student topStudent = studentManagement.getTopStudent();
        if (topStudent != null) {
            JOptionPane.showMessageDialog(this, "Top student is: " + topStudent.getName() + " with average marks of " + topStudent.getAverageMarks());
        } else {
            JOptionPane.showMessageDialog(this, "No students available.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new StudentManager().setVisible(true);
            }
        });
    }
}
