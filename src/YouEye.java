import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class YouEye extends JFrame {

    private JPanel resultsPanelContainer = new JPanel();
    private JTextField txtStudentId,
            txtSemester,
            txtCourseNum;
    private JTextField txtSQL = new JTextField(15);
    JFrame frame;

    Statement s = null;
    final int SIZE_FACTOR = 2;
    Font font = new Font("Calibri", Font.PLAIN, 10 * SIZE_FACTOR);

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                JFrame frame = new YouEye();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public YouEye() {
        frame = this;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new FlowLayout());
        setMinimumSize(new Dimension(300, 200));

        //Student ID text field
        txtStudentId = new JTextField();
        txtStudentId.setColumns(10);
        txtStudentId.setFont(font);

        //Semester text field
        txtSemester = new JTextField();
        txtSemester.setColumns(10);
        txtSemester.setFont(font);

        //Course text field
        txtCourseNum = new JTextField();
        txtCourseNum.setColumns(10);
        txtCourseNum.setFont(font);


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

        // these listeners will make sure that a maximum of only 1 checkbox can be ticked at a time.
        chckbx_LGrade.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && chckbx_TotalPoints.isSelected())
                chckbx_TotalPoints.setSelected(false);
        });
        chckbx_TotalPoints.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && chckbx_LGrade.isSelected())
                chckbx_LGrade.setSelected(false);
        });


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
            final String ATTR_COURSE_NUM = "COURSE_NUM",
                    ATTR_STUDENT_ID = "S_ID",
                    ATTR_TERM = "TERM";

            String query;
            if (!issStudentId && !issSemester && !issCourse) {
                return;
            }

            // if all 3 inputs
            if (issStudentId && issSemester && issCourse) {
                if (chckbx_TotalPoints.isSelected()) {
                    //(8) 3 inputs ID and course and term output his total points{
                    //(9) 3 inputs ID course and term output letter grade
                    query = "SELECT sum ((POINTSEARNED/POINTS)*WEIGHT) AS overall_points" +
                            "\nFROM ENROLLED_IN NATURAL JOIN GRADE_DISTRIBUTION" +
                            "\nWHERE " + ATTR_STUDENT_ID + " = " + txtStudentId.getText() + " " +
                            "\nAND " + ATTR_COURSE_NUM + " = '" + txtCourseNum.getText() + "'" +
                            "\nAND TERM = '" + txtSemester.getText() + "';";
                } else if (chckbx_LGrade.isSelected()) {
                    query = "SELECT  LGrade FROM (" +
                            "\nSELECT  LGrade " +
                            "\n    FROM GRADE_CUTOFFS" +
                            "\n    WHERE  GrdCutoff <= (" +
                            "\n        SELECT sum ((POINTSEARNED/POINTS)*WEIGHT) AS overall_points" +
                            "\n        FROM ENROLLED_IN NATURAL JOIN GRADE_DISTRIBUTION" +
                            "\n        WHERE " + ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "'" +
                            "\n        AND " + ATTR_COURSE_NUM + " = '" + txtCourseNum.getText() + "'" +
                            "\n        AND TERM = '" + txtSemester.getText() + "'" +
                            "\n    )" +
                            "\nAND " + ATTR_COURSE_NUM + " = '" + txtCourseNum.getText() + "'" +
                            "\nORDER BY GrdCutoff DESC" +
                            "\n)" +
                            "\nWHERE ROWNUM = 1;";
                } else {
                    // (7) 3 inputs (ID, course and term) output:	grades
                    final String condition =
                            ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "'" +
                                    " AND " + ATTR_TERM + " = '" + txtSemester.getText() + "'" +
                                    " AND " + ATTR_COURSE_NUM + " = '" + txtCourseNum.getText() + "'";

                    query = "SELECT DISTINCT " + ATTR_COURSE_NUM + " " +
                            "\nFROM ENROLLED_IN " +
                            "\nWHERE " + condition;
                }
            } else if (issStudentId && issSemester) {
                // Retrieve a student's details for all courses in a certain semester
                //-- (3) Input ID output: 					courses, terms
                query = "SELECT DISTINCT " + ATTR_COURSE_NUM + " " +
                        "\nFROM ENROLLED_IN " +
                        "\nWHERE " + ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "' AND " + ATTR_TERM + " = '" + txtSemester.getText() + "'";
            } else if (issStudentId && issCourse) {
                query = "SELECT DISTINCT " + ATTR_TERM + " " +
                        "\nFROM ENROLLED_IN " +
                        "\nWHERE " + ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "' AND " + ATTR_COURSE_NUM + " = '" + txtCourseNum.getText() + "'";
            } else if (issSemester && issCourse) {
                // Retrieve all students' details for a certain course in a certain semester
                query = "SELECT DISTINCT " + ATTR_STUDENT_ID + " " +
                        "\nFROM ENROLLED_IN " +
                        "\nWHERE " + ATTR_COURSE_NUM + " = '" + txtCourseNum.getText() + "'" + " AND " + ATTR_TERM + " = '" + txtSemester.getText() + "'";
            } else if (issStudentId) {
                // Retrieve all courses and their respective semesters given a student's ID

                String[] conditions = {
                        ATTR_STUDENT_ID + " = '" + txtStudentId.getText() + "'"
                };
                // working
                query = "SELECT DISTINCT " + ATTR_COURSE_NUM + ", " + ATTR_TERM + " " +
                        "\nFROM ENROLLED_IN " +
                        "\nWHERE " + String.join(" AND ", conditions);
            } else {
                // Retrieve multiple instances of multiple students corresponding to what courses they took on a certain semester

                String selections = ATTR_COURSE_NUM + (!issSemester ? (", " + ATTR_TERM) : "");

                ArrayList<String> conditions = new ArrayList<>();
                if (issCourse) conditions.add(ATTR_COURSE_NUM + " = '" + txtCourseNum.getText() + "'");
                if (issSemester) conditions.add(ATTR_TERM + " = '" + txtSemester.getText() + "'");

                query = "SELECT " + String.join(", ", selections) + " " +
                        "\nFROM COURSE " +
                        "\nWHERE " + String.join(", ", conditions);
            }

            System.out.println("issStudentId = " + issStudentId);
            System.out.println("issSemester = " + issSemester);
            System.out.println("issCourse = " + issCourse);

            System.out.println("query:\n" + query + "\n\n");
            try {
                ResultSet r = s.executeQuery(query);
                updateResults(r);
                if (r.next()) {
                    txtSQL.setText(r.getString(1));
                } else {
                    //display results
                    txtSQL.setText("Not Found");
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

        btnSearch.addActionListener(submitListener);

        txtCourseNum.addActionListener(submitListener);
        txtSemester.addActionListener(submitListener);
        txtStudentId.addActionListener(submitListener);

        for (Component c : contentPane.getComponents()) {
            c.setFont(font);
            c.addComponentListener(resizeListener);
        }
        this.addComponentListener(resizeListener);
        this.pack();
    }

    ComponentListener resizeListener = new ComponentListener() {
        public void componentHidden(ComponentEvent e) {
        }
        public void componentMoved(ComponentEvent e) {
        }
        public void componentResized(ComponentEvent e) {
            int width = frame.getWidth();
            for (Component component : frame.getContentPane().getComponents()) {
                component.setFont(new Font(font.getName(), font.getStyle(), width / 25));
            }
            // resizing the font as the size of the window changes
            frame.getContentPane().revalidate();
        }
        public void componentShown(ComponentEvent e) {
        }
    };

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

                while (resultSet.next()) {
                    //Print one row
                    for (int i = 1; i <= columnsNumber; i++) {
                        JTextComponent resultCell = new JTextField(resultSet.getString(i));
                        resultCell.setFont(font);
                        resultCell.setEditable(false);
                        this.add(resultCell); //Print one element of a row
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                e.printStackTrace();
            }
        }
    }
}

