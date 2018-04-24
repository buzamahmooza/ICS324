import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Arrays;

/**
 * @author Faris Hijazi st201578750
 */
public class JDBC extends JFrame {
    private JTextField txtMcode = new JTextField(4);
    private JTextField txtResult = new JTextField(30);
    private JPanel resultsPanelContainer = new JPanel();
    private Statement s = null;

    private JDBC() {
        super("JDBC Example");
        setLayout(new FlowLayout());
        JLabel lblMcode = new JLabel("Mcode");
        add(lblMcode);
        add(txtMcode);
        JLabel lblResult = new JLabel("Result");
        add(lblResult);
        add(txtResult);
        add(resultsPanelContainer);
        JButton find = new JButton("Find");
        add(find);

        //               "jdbc:oracle:thin:@ics-db:1521:xe";
        String connStr = "jdbc:oracle:thin:@172.16.0.239:1521:xe";

        try {
            final Connection conn = DriverManager.getConnection(connStr, "ICS324", "ICS324");
            conn.setAutoCommit(false);

            s = conn.createStatement();
            ActionListener queryListener = (ActionEvent a) -> {
                // list the students in a given major entered in a text field
                String inputText = txtMcode.getText();
                String query = "SELECT SNAME, SN " +
                        "FROM MAJOR NATURAL JOIN STUDENT WHERE MCODE = '" + inputText + "'";
                try {
                    ResultSet rs = this.s.executeQuery(query);
                    DBTablePrinter.printTable(conn, "students in " + inputText);

                    resultsPanelContainer.removeAll();
                    resultsPanelContainer.add(new ResultsPanel(rs));
                } catch (SQLException e) {
                    txtResult.setText("Query Error");
                    System.exit(0);
                }
            };
            find.addActionListener(queryListener);
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
                        this.add(new JTextField(resultSet.getString(i))); //Print one element of a row
                    }
                    r++;//Move to the next line to print the next row.
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
