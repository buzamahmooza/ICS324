import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Arrays;

/**
 * @author Faris Hijazi st201578750
 */
public class JDBC extends JFrame {
    private JTextField txtMcode = new JTextField(10);
    private JTextField txtSQL = new JTextField(10);
    private JTextArea txtResult = new JTextArea(20, 30);
    JScrollPane scrollPane = new JScrollPane(txtResult);

    private JPanel resultsPanelContainer = new JPanel();

    private Statement s = null;
    final int SIZE_FACTOR = 2;
    Font font = new Font("Calibri", Font.PLAIN, 10 * SIZE_FACTOR);

//    To view all available tables:     SELECT table_name FROM user_tables


    private JDBC() {
        super("JDBC Example");
//        setLayout(new FlowLayout());
        setLayout(new GridLayout(0, 2));
//        JLabel lblMcode = new JLabel("Mcode");
//        add(lblMcode);
//        add(txtMcode);

        JLabel lblResult = new JLabel("Result");
        JPanel resP = new JPanel();
        resP.add(lblResult);
        resP.add(txtResult);
        add(resP);

        JLabel lblSQL = new JLabel("Raw SQL");
        JPanel sqlP = new JPanel();
        sqlP.add(lblSQL);
        sqlP.add(txtSQL);
        add(sqlP);

//        lblMcode.setFont(font);
        lblResult.setFont(font);
        lblSQL.setFont(font);


        txtSQL.setFont(font);
        txtMcode.setFont(font);
        txtResult.setFont(font);


//        add(new JButton("Find"));
        add(resultsPanelContainer);
        add(scrollPane);

        String connStr = "jdbc:oracle:thin:@172.16.0.239:1521:xe";
        //               "jdbc:oracle:thin:@ics-db:1521:xe";


        try {
            final String USER_NAME = "ICS324",
                    PWD = "ICS324";
            final Connection conn = DriverManager.getConnection(connStr, USER_NAME, PWD);
            conn.setAutoCommit(false);

            s = conn.createStatement();

            ActionListener inputListener = (ActionEvent a) -> {
                // list the students in a given major entered in a text field
                String inputText = txtMcode.getText();
                String query = "SELECT SNAME, SN " +
                        "FROM MAJOR NATURAL JOIN STUDENT WHERE MCODE = '" + inputText + "'";
                resultsPanelContainer.removeAll();
                try {
                    ResultSet rs = this.s.executeQuery(query);
//                    DBTablePrinter.printTable(conn, "students in " + inputText);

                    resultsPanelContainer.add(new ResultsPanel(rs));
                } catch (SQLException e) {
                    txtResult.setText("Query Error");
                    System.exit(0);
                }
            };
            ActionListener sqlListener = (ActionEvent a) -> {
                String inputQuery = txtSQL.getText();
                System.out.println("Executin query:\n" + inputQuery);
                resultsPanelContainer.removeAll();
                try {
                    ResultSet rs = this.s.executeQuery(inputQuery);

//                    try {
//                        DBTablePrinter.printTable(conn, "Query result " + inputQuery);
//                    } catch (NoClassDefFoundError e) {
//                        e.printStackTrace();
//                    }

                    resultsPanelContainer.add(new ResultsPanel(rs));

                    String output = "Result of " + inputQuery + ":" +
                            "\nrs.toString():\t" + rs.toString() +
                            "\nresultToString:\t" + resultSetToString(rs);
                    System.out.println(output);
                    txtResult.setText(txtResult.getText() + "\n" + output);


                } catch (SQLException e) {
                    txtResult.setText("Query Error");
                    e.printStackTrace();
//                    System.exit(0);
                }
                resultsPanelContainer.repaint();
            };


            txtMcode.addActionListener(inputListener);
            txtSQL.addActionListener(sqlListener);

        } catch (SQLException e) {
            System.err.print("Connection Error");
            e.printStackTrace();
            System.exit(0);
        }
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
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
            }
        }
    }

    public static String resultSetToString(ResultSet rs) {
        StringBuilder str = new StringBuilder();
        try {
            while (rs.next()) {
                str.append(rs.getString(1)); // or rs.getString("//your column name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            str.append("\n").append(Arrays.toString(e.getStackTrace())).append(e.getSQLState());
        }
        System.out.println(str);
        return str.toString();
    }

    public static String resultToString(ResultSet resultSet) {
        StringBuilder str = new StringBuilder();
        try {
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (resultSet.next()) {
                // Create string for one row
                for (int i = 1; i <= columnsNumber; i++) {
                    str.append(resultSet.getString(i)).append(" "); // Appends all columns (attributes) of a row (tuple). Same as: `str += result + ""`
                }
                str.append("\n"); // Move to the next line
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    public static void main(String[] args) {
        new JDBC();
    }
}
