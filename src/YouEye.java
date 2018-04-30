import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

public class YouEye extends JFrame {

    private JPanel resultsPanelContainer = new JPanel();
    private JTextField txtStudentId,
            txtSemester,
            txtCourseNum;
    private JTextField txtSQL = new JTextField(15);

    Statement s = null;
    final int SIZE_FACTOR = 2;
    Font font = new Font("Calibri", Font.PLAIN, 10 * SIZE_FACTOR);

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                YouEye frame = new YouEye();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public YouEye() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new FlowLayout());
        setMinimumSize(new Dimension(300, 200));

        //Student ID text field
        txtStudentId = new JTextField();
        txtStudentId.setColumns(10);

        //Semester text field
        txtSemester = new JTextField();
        txtSemester.setColumns(10);

        //Course text field
        txtCourseNum = new JTextField();
        txtCourseNum.setColumns(10);

        JPanel fieldsPane = new JPanel();
        fieldsPane.setLayout(new GridLayout(3, 2));
        fieldsPane.add(txtSemester);
        fieldsPane.add(new JLabel("Semester"));
        fieldsPane.add(txtStudentId);
        fieldsPane.add(new JLabel("Student ID"));
        fieldsPane.add(txtCourseNum);
        fieldsPane.add(new JLabel("Course Code"));
        contentPane.add(fieldsPane);


        //Semester check box
        JCheckBox chckbx_TotalPoints = new JCheckBox("Total Points");
        chckbx_TotalPoints.setEnabled(false);
        //Course check box
        JCheckBox chckbx_LGrade = new JCheckBox("Letter Grade");
        chckbx_LGrade.setEnabled(false);


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

        // SQL textfield (only for debugging)
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

            validate();
            repaint();
        });


        // this will make sure that the textboxes are only enabled if all textFields are filled
        final DocumentListener textChangeListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateChckboxes();
            }
            public void removeUpdate(DocumentEvent e) {
                updateChckboxes();
            }
            public void insertUpdate(DocumentEvent e) {
                updateChckboxes();
            }

            private void updateChckboxes() {
                boolean allTextFieldsFilled = txtStudentId.getText().length() > 0 && txtSemester.getText().length() > 0 && txtCourseNum.getText().length() > 0;
                chckbx_LGrade.setEnabled(allTextFieldsFilled);
                chckbx_TotalPoints.setEnabled(allTextFieldsFilled);

                if (!allTextFieldsFilled) {
                    chckbx_LGrade.setSelected(false);
                    chckbx_TotalPoints.setSelected(false);
                }
            }
        };
        txtCourseNum.getDocument().addDocumentListener(textChangeListener);
        txtSemester.getDocument().addDocumentListener(textChangeListener);
        txtStudentId.getDocument().addDocumentListener(textChangeListener);

        JPanel searchOptionsPane = new JPanel();
        searchOptionsPane.setLayout(new GridLayout(0, 1));
        searchOptionsPane.add(chckbx_LGrade);
        searchOptionsPane.add(chckbx_TotalPoints);
        searchOptionsPane.add(btnSearch);

        contentPane.add(searchOptionsPane);
        contentPane.add(txtSQL);
        contentPane.add(resultsPanelContainer);


        final ActionListener submitListener = (ActionEvent a) -> {
            System.out.println("Submitted...");
            final boolean issStudentId = txtStudentId.getText().length() > 0,
                    issSemester = txtSemester.getText().length() > 0,
                    issCourse = txtCourseNum.getText().length() > 0;
            final String ATTR_STUDENT_ID = "S_ID",
                    ATTR_TERM = "TERM";

            String term = txtSemester.getText();
            String courseNum = txtCourseNum.getText();

            String query = "";
            if (!issStudentId && !issSemester && !issCourse) {
                return;
            }

            if (issStudentId && issSemester && issCourse) {
                if (chckbx_TotalPoints.isSelected()) {
                    //(8) 3 inputs ID and course and term output his total points{
                    //(9) 3 inputs ID course and term output letter grade
                    query = "SELECT sum ((POINTSEARNED/POINTS)*WEIGHT) AS overall_points" +
                            "FROM ENROLLED_IN NATURAL JOIN GRADE_DISTRIBUTION" +
                            "WHERE S_ID = " + txtStudentId.getText() + " AND COURSE_NUM = '" + courseNum + "' AND term = '" + term + "';";
                } else if (chckbx_LGrade.isSelected()) {
                    query = "SELECT  LGrade FROM (" +
                            "SELECT  LGrade " +
                            "    FROM GRADE_CUTOFFS" +
                            "    WHERE  GrdCutoff <= (" +
                            "        SELECT sum ((POINTSEARNED/POINTS)*WEIGHT) AS overall_points" +
                            "        FROM enrolled_in natural join GRADE_DISTRIBUTION" +
                            "        WHERE S_ID = '" + txtStudentId.getText() + "' AND course_num = '" + courseNum + "' AND term = '" + term + "'" +
                            "    )" +
                            "AND course_num = '" + courseNum + "'" +
                            "ORDER BY GrdCutoff DESC" +
                            ")" +
                            "where ROWNUM = 1;";
                } else {
                    // (7) 3 inputs (ID, course and term) output:	grades
                    final String condition = ATTR_STUDENT_ID + " = '" + txtStudentId.getText() +
                            "'AND " + ATTR_TERM + " = '" + txtSemester.getText() + "'" +
                            "AND " + ATTR_TERM + " = '" + txtCourseNum.getText() + "'";

                    query = "SELECT DISTINCT COURSE_NUM " +
                            "FROM ENROLLED_IN " +
                            "WHERE " + condition;
                }
            } else if (issStudentId && issSemester) {
                // Retrieve a student's details for all courses in a certain semester
                query = "SELECT DISTINCT COURSE_NUM FROM ENROLLED_IN " +
                        "WHERE " + ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "'AND " + ATTR_TERM + " = '" + txtSemester.getText() + "'";
            } else if (issStudentId && issCourse) {
                query = "SELECT DISTINCT " + ATTR_TERM + " FROM ENROLLED_IN " +
                        "WHERE " + ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "'AND COURSE_NUM = '" + txtCourseNum.getText() + "'";
            } else if (issSemester && issCourse) {
                // Retrieve all students' details for a certain course in a certain semester
                query = "SELECT DISTINCT " + ATTR_STUDENT_ID + " FROM ENROLLED_IN " +
                        "WHERE COURSE_NUM = '" + txtCourseNum.getText() + "'" + "AND " + ATTR_TERM + " = '" + txtSemester.getText() + "'";
            } else if (issStudentId) {
                // Retrieve all courses and their respective semesters given a student's ID

                String[] conditions = {
                        ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "'"
                };
                final String condition = String.join(", ", conditions);

                query = "SELECT DISTINCT COURSE_NUM, " + ATTR_TERM + " " +
                        "FROM ENROLLED_IN " +
                        "WHERE " + condition;
            } else {
                if (issSemester) {
                    // Retrieve multiple instances of multiple students corresponding to what courses they took on a certain semester

                    String selections = "COURSE_NUM" + (issCourse ? (", " + ATTR_TERM) : "");
                    ArrayList<String> conditions = new ArrayList<>(Arrays.asList(
                            ("COURSE_NUM = '" + txtCourseNum.getText() + "'"),
                            (ATTR_TERM + " = '" + txtSemester.getText() + "'")
                    ));

                    query = "SELECT " + String.join(", ", selections) +
                            " FROM COURSE " +
                            "WHERE " + String.join(", ", conditions);
                } else if (issCourse) {
                    // Retrieve students and the semester in which they took a certain course
                    String condition = "";
                    query = "SELECT " + ATTR_TERM +
                            " FROM COURSE " +
                            "WHERE " + condition;
                }
            }

            try {
                ResultSet r = s.executeQuery(query);
                updateResults(r);
                if (r.next()) {
                    /*list results*/
                    chckbx_LGrade.setText(r.getString(1));
                } else {
                    //display results
                    chckbx_LGrade.setText("Not Found");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                StringBuilder sb = new StringBuilder(e.toString());
                for (StackTraceElement ste : e.getStackTrace()) {
                    sb.append("\n\tat ");
                    sb.append(ste);
                }
                String trace = sb.toString();
                final String message = "SQL state:\t" + e.getSQLState() +
                        "\n" + trace;
                JOptionPane.showMessageDialog(
                        null,
                        message,
                        "Error Massage",
                        JOptionPane.ERROR_MESSAGE);
            }
            validate();
            repaint();
        };

        // Retrieve a student's details for a certain course on a certain semester
        // Retrieve a student's details for all courses in a certain semester
        // Retrieve all students' details for a certain course in a certain semester
        // Retrieve all courses and their respective semesters given a student's ID
        // Retrieve multiple instances of multiple students corresponding to what courses they took on a certain semester(?)
        // Retrieve students and	the semester in which they took a certain course
        /*list results*/
        //display results

        //h
        btnSearch.addActionListener(submitListener);

        txtCourseNum.addActionListener(submitListener);
        txtSemester.addActionListener(submitListener);
        txtStudentId.addActionListener(submitListener);
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

