import java.sql.*;

public class App {
    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/alpha", "root",
                    "naveen123");

            // Create a statement object
            Statement statement = connection.createStatement();

            // Show unique cities
            showUniqueCities(statement);
            PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT name FROM resttable WHERE city = ?");

            // Prompt user to select a city
            String cityName = selectCity(statement);
            if (cityName != "") {
                // Show restaurants in the selected city
                String selectedRes = showRestaurantsInCity(preparedStatement, cityName);

                // Insert customer data
                insertCustomerData(statement, selectedRes, cityName);
            }

            // Close the statement and connection
            preparedStatement.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showUniqueCities(Statement statement) throws SQLException {
        // Query to fetch unique cities with the count
        String query = "SELECT city, COUNT(*) FROM resttable GROUP BY city";
        ResultSet resultSet = statement.executeQuery(query);

        // Print unique cities with numbers
        System.out.println("Available cities:");
        int index = 1;
        while (resultSet.next()) {
            String city = resultSet.getString(1);
            int count = resultSet.getInt(2);
            System.out.println(index + ". " + city + " (" + count + ")");
            index++;
        }
    }

    private static String selectCity(Statement statement) throws SQLException {
        try {
            // Prompt user to select a city
            System.out.print("Enter the number of the city you want to explore: ");
            int cityIndex = Integer.parseInt(System.console().readLine());

            // Show restaurants in the selected city
            ResultSet resultSet = statement.executeQuery("SELECT city FROM resttable GROUP BY city");
            for (int i = 1; i <= cityIndex; i++) {
                resultSet.next();
            }
            String selectedCity = resultSet.getString(1);
            return selectedCity;
        } catch (NumberFormatException | NullPointerException e) {
            System.out.println("Invalid input.");
            return "";
        }
    }

    private static String showRestaurantsInCity(PreparedStatement preparedStatement, String cityName)
            throws SQLException {
        preparedStatement.setString(1, cityName); // Set the value for the parameter
        ResultSet resultSet = preparedStatement.executeQuery();

        // Print restaurant names in the selected city
        System.out.println("Restaurants in " + cityName + ":");
        int index = 1;
        while (resultSet.next()) {
            String restaurantName = resultSet.getString("name");
            System.out.println(index + ". " + restaurantName);
            index++;
        }
        System.out.print("Enter the number of the city you want to explore: ");
        int resIndex = Integer.parseInt(System.console().readLine());
        resultSet = preparedStatement.executeQuery();
        for (int i = 1; i <= resIndex; i++) {
            resultSet.next();
        }
        String selectedRes = resultSet.getString(1);
        return selectedRes;
    }

    private static boolean validation(Statement statement, int count, String selectedRes, String cityName) {
        try {
            String req1 = "";
            String req2 = "";
            if (count == 2) {
                req1 = "no_of_2_mem_table";
                req2 = "no_of_2_mem_table_occu";
            } else if (count == 3) {
                req1 = "no_of_3_mem_table";
                req2 = "no_of_3_mem_table_occu";
            } else if (count == 4) {
                req1 = "no_of_4_mem_table";
                req2 = "no_of_4_mem_occu";
            } else {
                System.out.println("Select either 2, 3, or 4 people tables");
                return false;
            }

            String insertQuery = "SELECT " + req1 + ", " + req2 + " FROM resttable WHERE city= ? AND name= ?";
            PreparedStatement preparedStatement = statement.getConnection().prepareStatement(insertQuery);
            preparedStatement.setString(1, cityName);
            preparedStatement.setString(2, selectedRes);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int availableTables = resultSet.getInt(req1);
                int occupiedTables = resultSet.getInt(req2);
                if (occupiedTables < availableTables) {
                    statement.executeUpdate(
                            "UPDATE resttable SET " + req2 + " = " + req2 + " + 1 WHERE city = '" + cityName
                                    + "' AND name = '" + selectedRes + "'");
                    return true;
                } else {
                    System.out.println(
                            "No tables left for the selected count of people. Please revisit after some time.");
                    return false;
                }
            } else {
                System.out.println("No matching restaurant found.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void insertCustomerData(Statement statement, String selectedRes, String cityName) {
        try {
            System.out.print("Enter customer name: ");
            String name = System.console().readLine();

            System.out.print("Enter number of people: ");
            int numberOfPeople = Integer.parseInt(System.console().readLine());
            if (!validation(statement, numberOfPeople, selectedRes, cityName)) {
                return;
            }

            System.out.print("Enter amount: ");
            int amount = Integer.parseInt(System.console().readLine());

            System.out.print("Enter book ID: ");
            int bookId = Integer.parseInt(System.console().readLine());

            System.out.print("Enter transaction ID: ");
            int transactionId = Integer.parseInt(System.console().readLine());

            // Insert customer data into custo_table
            String insertQuery = "INSERT INTO custo_table (name, bookid, amount, no_of_people, transactionid) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = statement.getConnection().prepareStatement(insertQuery);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, bookId);
            preparedStatement.setInt(3, amount);
            preparedStatement.setInt(4, numberOfPeople);
            preparedStatement.setInt(5, transactionId);
            preparedStatement.executeUpdate();
            System.out.println("Data inserted successfully.");
        } catch (SQLException | NumberFormatException | NullPointerException e) {
            System.out.println("Invalid input or error occurred.");
        }
    }
}
