import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.sql.*;

public class YouEye extends JFrame {

    private JPanel resultsPanelContainer = new JPanel();
    private JTextField txtStudentId,
            txtSemester,
            txtCourseCode;
    private JTextField txtSQL = new JTextField(10);

    Statement s = null;
    final int SIZE_FACTOR = 2;
    Font font = new Font("Calibri", Font.PLAIN, 10 * SIZE_FACTOR);

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                YouEye frame = new YouEye();
                frame.setVisible(true);
                frame.setResizable(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public YouEye() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 679, 655);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        //Student ID text field
        txtStudentId = new JTextField();
//        txtStudentId.setText("Enter Student ID");
//        txtStudentId.setEnabled(false);
        txtStudentId.setBounds(21, 44, 186, 32);
        contentPane.add(txtStudentId);
        txtStudentId.setColumns(10);

        //Student ID's check box
        JCheckBox chckbxStudentId = new JCheckBox("Student ID");
        chckbxStudentId.setBounds(224, 44, 151, 33);
        chckbxStudentId.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                txtStudentId.setEnabled(true);
                txtStudentId.setText("");

            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                txtStudentId.setEnabled(false);
                txtStudentId.setText("Enter Student ID");

            }

            validate();
            repaint();
        });

        contentPane.add(chckbxStudentId);

        //Semester text field
        txtSemester = new JTextField();
//        txtSemester.setEnabled(false);
//        txtSemester.setText("Enter Semester");
        txtSemester.setBounds(21, 97, 186, 32);
        contentPane.add(txtSemester);
        txtSemester.setColumns(10);

        //Semester check box
        JCheckBox chckbxSemester = new JCheckBox("Semester");
        chckbxSemester.setBounds(224, 90, 179, 35);
        chckbxSemester.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                txtSemester.setEnabled(true);
                txtSemester.setText("");

            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                txtSemester.setEnabled(false);
                txtSemester.setText("Enter Semester");

            }

            validate();
            repaint();
        });
        contentPane.add(chckbxSemester);


        //Course text field
        txtCourseCode = new JTextField();
//        txtCourseCode.setText("Enter Course Code");
//        txtCourseCode.setEnabled(false);
        txtCourseCode.setBounds(21, 139, 186, 32);
        contentPane.add(txtCourseCode);
        txtCourseCode.setColumns(10);

        //Course check box
        JCheckBox chckbxCourse = new JCheckBox("Course");
        chckbxCourse.setBounds(224, 138, 179, 35);
        chckbxCourse.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                txtCourseCode.setEnabled(true);
                txtCourseCode.setText("");

            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                txtCourseCode.setEnabled(false);
                txtCourseCode.setText("Enter Course Code");

            }

            validate();
            repaint();
        });
        contentPane.add(chckbxCourse);


        //The main man, the big B
        JButton btnSearch = new JButton("Search");

        String connStr = "jdbc:oracle:thin:@172.16.0.239:1521:xe";
        //        "jdbc:oracle:thin:@ics-db:1521:xe";


        try {
            final String USER_NAME = "ICS324",
                    PWD = "ICS324";
            final Connection conn = DriverManager.getConnection(connStr, USER_NAME, PWD);
            conn.setAutoCommit(false);
            s = conn.createStatement();

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.print("Connection Error");
            e.printStackTrace();
            System.exit(0);
        }

        // sql textfield (only for debugging)
        txtSQL.addActionListener((ActionEvent a) -> {
            String inputQuery = txtSQL.getText();
            System.out.println("Executing query:\n" + inputQuery);
            try {
                ResultSet rs = this.s.executeQuery(inputQuery);

                updateResults(rs);

                String output = "Result of " + inputQuery + ":" +
                        "\nrs.toString():\t" + rs.toString() +
                        "\nresultToString:\t" + JDBC.resultSetToString(rs);
                System.out.println(output);


            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });

        final ActionListener submitListener = (ActionEvent a) -> {
            System.out.println("Submitted...");
            final boolean issStudentId = txtStudentId.getText().length() > 0,
                    issSemester = txtSemester.getText().length() > 0,
                    issCourse = txtCourseCode.getText().length() > 0;

            final String ATTR_STUDENT_ID = "S_ID";
            String query = "";
            if (issStudentId && issSemester && issCourse) { // Retrieve
                // a student's details for a certain course on a certain semester
                query = "SELECT DISTINCT COURSE_NUM FROM ENROLLED_IN " +
                        "WHERE " + ATTR_STUDENT_ID + " = '" + txtStudentId.getText() +
                        "'AND TERM = '" + txtSemester.getText() + "'" +
                         "AND TERM = '" + txtCourseCode.getText() + "'";
            } else {
                if (issStudentId && issSemester) { // Retrieve a student's details for all courses in a certain semester
                    query = "SELECT DISTINCT COURSE_NUM FROM ENROLLED_IN " +
                            "WHERE " + ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "'AND TERM = '" + txtSemester.getText() + "'";
                } else if (issStudentId && issCourse) {
                    query = "SELECT DISTINCT TERM FROM ENROLLED_IN " +
                            "WHERE " + ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "'AND COURSE_NUM = '" + txtCourseCode.getText() + "'";
                } else if (issSemester && issCourse) { // Retrieve all students' details for a certain course in a certain semester
                    query = "SELECT DISTINCT " + ATTR_STUDENT_ID + " FROM ENROLLED_IN " +
                            "WHERE COURSE_NUM = '" + txtCourseCode.getText() + "'" + "AND TERM = '" + txtSemester.getText() + "'";
                } else if (issStudentId) { // Retrieve all courses and their respective semesters given a student's ID
                    query = "SELECT DISTINCT COURSE_NUM, TERM " +
                            "FROM ENROLLED_IN " +
                            "WHERE " + ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "'";
                } else if (issSemester) { // Retrieve multiple instances of multiple students corresponding to what courses they took on a certain semester(?)
                    query = "SELECT Course_num FROM Course " +
                            "WHERE TERM = '" + txtSemester.getText() + "'";
                } else if (issCourse) {// Retrieve students and	the semester in which they took a certain course
                    query = "SELECT TERM FROM COURSE " +
                            "WHERE COURSE_NUM = '" + txtCourseCode.getText() + "'";
                } else query = "";

                try {
                    ResultSet r = s.executeQuery(query);
                    updateResults(r);
                    if (r.next()) {
                        /*list results*/
                        chckbxCourse.setText(r.getString(1));
                    } else {
                        //display results
                        chckbxCourse.setText("Not Found");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    chckbxCourse.setText("Query Error");
                }
            }
        };
        //h
        btnSearch.addActionListener(submitListener);
        txtCourseCode.addActionListener(submitListener);
        txtSemester.addActionListener(submitListener);
        txtStudentId.addActionListener(submitListener);

        btnSearch.setBounds(140, 196, 141, 35);
        contentPane.add(btnSearch);

        contentPane.add(txtSQL);
        txtSQL.setBounds(21, 263, 238, 35);

        contentPane.add(resultsPanelContainer);
        resultsPanelContainer.setBounds(300, 179, 332, 384);
    }

    private void updateResults(ResultSet rs) {
        resultsPanelContainer.removeAll();
        resultsPanelContainer.add(new ResultsPanel(rs));
        resultsPanelContainer.repaint();
    }

    class ResultsPanel extends JPanel {
        ResultSet resultSet;
        ResultSetMetaData rsmd;
        int columnsNumber;


        public ResultsPanel(ResultSet resultSet) {
            this.resultSet = resultSet;
            try {
                rsmd = resultSet.getMetaData();
                columnsNumber = rsmd.getColumnCount();
                this.setLayout(new GridLayout(0, columnsNumber));

                int r = 0;
                while (resultSet.next()) {
                    //Print one row
                    for (int i = 1; i <= columnsNumber; i++) {
                        JTextComponent resultCell = new JTextField(resultSet.getString(i));
                        resultCell.setFont(font);
                        resultCell.setEditable(false);
                        this.add(resultCell); //Print one element of a row
                    }
                    r++;//Move to the next line to print the next row.
                }
            } catch (SQLException e) {
                e.printStackTrace();
                e.printStackTrace();
            }
        }
    }

}

