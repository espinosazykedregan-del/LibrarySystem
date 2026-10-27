import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

abstract class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String category;

    public Item(int id, String title, String category) {
        this.id = id;
        this.title = title;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public abstract void displayInfo();
}

class Book extends Item {
    private static final long serialVersionUID = 1L;

    private String author;
    private boolean borrowed;

    public Book(int id, String title, String author, String category) {
        super(id, title, category);
        this.author = author;
        this.borrowed = false;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isBorrowed() {
        return borrowed;
    }

    public void borrowBook() {
        borrowed = true;
    }

    public void returnBook() {
        borrowed = false;
    }

    @Override
    public void displayInfo() {
        System.out.println(
            "[BOOK] ID: " + getId()
            + " | Title: " + getTitle()
            + " | Author: " + getAuthor()
            + " | Category: " + getCategory()
            + " | Status: " + (isBorrowed() ? "Borrowed" : "Available")
        );
    }
}

class PrintedBook extends Book {
    private static final long serialVersionUID = 1L;

    private int pages;

    public PrintedBook(int id, String title, String author, int pages) {
        super(id, title, author, "Drama");
        this.pages = pages;
    }

    public int getPages() {
        return pages;
    }

    @Override
    public void displayInfo() {
        System.out.println(
            "[PRINTED] ID: " + getId()
            + " | Title: " + getTitle()
            + " | Author: " + getAuthor()
            + " | Category: " + getCategory()
            + " | Pages: " + pages
            + " | Status: " + (isBorrowed() ? "Borrowed" : "Available")
        );
    }
}

class Ebook extends Book {
    private static final long serialVersionUID = 1L;

    private String fileSize;

    public Ebook(int id, String title, String author, String fileSize) {
        super(id, title, author, "Drama");
        this.fileSize = fileSize;
    }

    public String getFileSize() {
        return fileSize;
    }

    @Override
    public void displayInfo() {
        System.out.println(
            "[EBOOK] ID: " + getId()
            + " | Title: " + getTitle()
            + " | Author: " + getAuthor()
            + " | Category: " + getCategory()
            + " | Size: " + fileSize
            + " | Status: " + (isBorrowed() ? "Borrowed" : "Available")
        );
    }
}

// 7. Create a class that implements Runnable
class LibraryTask implements Runnable {
    private String actionType;
    private int bookId;
    private JLabel statusLabel;
    private JTextArea outputArea;

    public LibraryTask(String actionType, int bookId, JLabel statusLabel, JTextArea outputArea) {
        this.actionType = actionType;
        this.bookId = bookId;
        this.statusLabel = statusLabel;
        this.outputArea = outputArea;
    }

    @Override
    public void run() {
        try {
            // 10. Use sleep() to simulate processing
            SwingUtilities.invokeLater(() -> statusLabel.setText("Status: Connecting to database..."));
            Thread.sleep(600);

            SwingUtilities.invokeLater(() -> statusLabel.setText("Status: Searching for Book ID " + bookId + "..."));
            Thread.sleep(800);

            Book b = Main.find(Main.books, bookId);

            if (b == null) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Status: Book ID " + bookId + " not found.");
                    outputArea.append("Operation failed: Book ID " + bookId + " does not exist.\n");
                });
            } else {
                if (actionType.equalsIgnoreCase("Borrow")) {
                    if (b.isBorrowed()) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Status: Book is already borrowed.");
                            outputArea.append("Book '" + b.getTitle() + "' is already borrowed.\n");
                        });
                    } else {
                        b.borrowBook();
                        Main.saveBooks();
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Status: Successfully borrowed '" + b.getTitle() + "'!");
                            outputArea.append("Borrowed: " + b.getTitle() + " (ID: " + b.getId() + ")\n");
                        });
                    }
                } else if (actionType.equalsIgnoreCase("Return")) {
                    if (!b.isBorrowed()) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Status: Book was not borrowed.");
                            outputArea.append("Book '" + b.getTitle() + "' was not borrowed.\n");
                        });
                    } else {
                        b.returnBook();
                        Main.saveBooks();
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Status: Successfully returned '" + b.getTitle() + "'!");
                            outputArea.append("Returned: " + b.getTitle() + " (ID: " + b.getId() + ")\n");
                        });
                    }
                }
            }
        } catch (InterruptedException e) {
            SwingUtilities.invokeLater(() -> statusLabel.setText("Status: Task was interrupted."));
        }
    }
}

public class Main {

    static final String TEXT_FILE = "book.txt";
    static final String SAVE_FILE = "books.dat";
    static ArrayList<Book> books = new ArrayList<>();

    public static void main(String[] args) {
        loadBooks();

        // Run GUI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // 2. Create a JFrame
            JFrame frame = new JFrame("DEIHS Library System - GUI & Threads");
            frame.setSize(500, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout(10, 10));

            // Input Panel
            JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // 3. Add a JLabel & 4. Add a JTextField
            JLabel lblInput = new JLabel("Enter Book ID:");
            JTextField txtBookId = new JTextField();

            // 5. Add Buttons
            JButton btnBorrow = new JButton("Borrow Book");
            JButton btnReturn = new JButton("Return Book");

            // Status display Label (11. Display task status)
            JLabel lblStatusTitle = new JLabel("Task Status:");
            JLabel lblStatus = new JLabel("Ready");

            panel.add(lblInput);
            panel.add(txtBookId);
            panel.add(btnBorrow);
            panel.add(btnReturn);
            panel.add(lblStatusTitle);
            panel.add(lblStatus);

            // Text area to show system logs
            JTextArea txtArea = new JTextArea();
            txtArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(txtArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Activity Log"));
            txtArea.append("System initialized. Total books loaded: " + books.size() + "\n");

            // 6. Add event handling to the buttons
            btnBorrow.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    processAction("Borrow", txtBookId, lblStatus, txtArea);
                }
            });

            btnReturn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    processAction("Return", txtBookId, lblStatus, txtArea);
                }
            });

            frame.add(panel, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Helper method to create and start the thread
    private static void processAction(String action, JTextField textField, JLabel statusLabel, JTextArea outputArea) {
        try {
            int id = Integer.parseInt(textField.getText().trim());
            
            // 7. Create Runnable object, 8. Create a thread using Runnable, 9. Use start()
            LibraryTask task = new LibraryTask(action, id, statusLabel, outputArea);
            Thread t = new Thread(task);
            t.start();

        } catch (NumberFormatException ex) {
            statusLabel.setText("Status: Invalid ID format. Enter a number.");
        }
    }

    @SuppressWarnings("unchecked")
    static void loadBooks() {
        File saved = new File(SAVE_FILE);

        if (saved.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saved))) {
                Object data = in.readObject();

                if (data instanceof ArrayList<?>) {
                    ArrayList<?> loaded = (ArrayList<?>) data;
                    boolean valid = true;

                    for (Object obj : loaded) {
                        if (!(obj instanceof Book)) {
                            valid = false;
                            break;
                        }
                    }

                    if (valid) {
                        books = (ArrayList<Book>) data;
                        return;
                    }
                }
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                // Fallback to text file
            }
        }

        File file = new File(TEXT_FILE);

        if (file.exists()) {
            books = readFile(file);
        } else {
            books.add(new PrintedBook(1, "Manman the User", "Espinosa", 250));
            books.add(new PrintedBook(2, "Manman the Druglord", "Espinosa", 300));
            books.add(new Ebook(3, "Manman the Badjao", "Espinosa", "10MB"));

            writeTextFile();
        }

        saveBooks();
    }

    static ArrayList<Book> readFile(File file) {
        ArrayList<Book> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] p = line.split(",", -1);
                if (p.length != 5) continue;

                try {
                    int id = Integer.parseInt(p[0].trim());
                    String title = p[1].trim();
                    String author = p[2].trim();
                    String type = p[3].trim();
                    String extra = p[4].trim();

                    Book b = createBook(id, title, author, type, extra);
                    if (b != null) result.add(b);
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException ignored) {}

        return result;
    }

    static Book createBook(int id, String title, String author, String type, String extra) {
        if (type.equalsIgnoreCase("PrintedBook") || type.equalsIgnoreCase("Printed")) {
            try {
                int pages = Integer.parseInt(extra);
                return pages > 0 ? new PrintedBook(id, title, author, pages) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (type.equalsIgnoreCase("Ebook")) {
            return new Ebook(id, title, author, extra);
        }

        return null;
    }

    static void writeTextFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TEXT_FILE))) {
            for (Book b : books) {
                bw.write(toRecord(b));
                bw.newLine();
            }
        } catch (IOException ignored) {}
    }

    static void saveBooks() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(books);
        } catch (IOException ignored) {}
    }

    static String toRecord(Book b) {
        if (b instanceof PrintedBook) {
            PrintedBook pb = (PrintedBook) b;
            return b.getId() + "," + b.getTitle() + "," + b.getAuthor() + ",PrintedBook," + pb.getPages();
        }
        if (b instanceof Ebook) {
            Ebook eb = (Ebook) b;
            return b.getId() + "," + b.getTitle() + "," + b.getAuthor() + ",Ebook," + eb.getFileSize();
        }
        throw new IllegalArgumentException("Unknown book type.");
    }

    static Book find(ArrayList<Book> list, int id) {
        for (Book b : list) {
            if (b.getId() == id) return b;
        }
        return null;
    }
}